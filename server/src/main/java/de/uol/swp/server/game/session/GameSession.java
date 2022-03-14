package de.uol.swp.server.game.session;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.DevCardEnumMap;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.debug.StateMessage;
import de.uol.swp.common.game.dto.PieceDTO;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.*;
import de.uol.swp.common.game.message.build.SetupLocationMessage;
import de.uol.swp.common.game.message.trade.*;
import de.uol.swp.common.game.request.trade.DeclineTradeOfferRequest;
import de.uol.swp.common.game.response.DiscardCardsResponse;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.message.Message;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.exception.CanNotRejoinException;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.UserIsNotPartOfGameSessionException;
import de.uol.swp.server.game.BankInventory;
import de.uol.swp.server.game.InventoryService;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.ai.BotUser;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.mapobject.BuildablePiece;
import de.uol.swp.server.game.mapobject.CityPiece;
import de.uol.swp.server.game.mapobject.RoadPiece;
import de.uol.swp.server.game.mapobject.SettlementPiece;
import de.uol.swp.server.game.state.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.GodClass", "PMD.ExcessivePublicCount"})
public class GameSession {

    private static final Logger LOG = LogManager.getLogger(GameSession.class);
    private static final Random RANDOM = new Random();
    private final Board board;
    private final Player[] players;
    private final Set<BotUser> bots = new HashSet<>();
    @SuppressWarnings("unused")
    private final UUID sessionID;
    private final String gameSessionName;
    private final Set<User> users = new TreeSet<>();
    private final Map<User, Player> leftUsers = new HashMap<>();
    private final StateContext context;
    private final GameSessionService gameSessionService;
    private final InventoryService inventoryService;
    private final GameLobby lobby;
    private final BankInventory bankInventory = new BankInventory();
    private final Queue<Integer> turnQueue = new ArrayDeque<>();
    private final EventBus eventBus;
    private final Set<Player> readyPlayers = new HashSet<>();
    private final TurnTimer timer;
    private boolean gameStarted = false;//TODO Wann game started?
    private GameState currentState;
    private int amountOfBots;
    private boolean reverseTurnQueue;
    private boolean playedDevCardInDiceState = false;
    private boolean debugEnabled;

    /**
     * Constructs a new game session for the given lobby. Should be constructed with {@link GameSessionFactory} via guice
     * <p>
     * The uuid of the session and the board will be instantiated here
     *
     * @param lobby              the lobby that has started the game session
     * @param gameSessionService the instance of {@code GameSessionService} injected by guice
     * @param inventoryService   the instance of {@code InventoryService} injected by guice
     * @param eventBus           the EvenBus used throughout the server
     */
    @Inject
    public GameSession(@Assisted GameLobby lobby, GameSessionService gameSessionService, InventoryService inventoryService, EventBus eventBus) {
        this.gameSessionService = gameSessionService;
        this.eventBus = eventBus;
        this.inventoryService = inventoryService;
        debugEnabled = lobby.isDebugEnabled();
        context = new StateContext();
        users.addAll(lobby.getUsers());
        this.lobby = lobby;
        gameSessionName = lobby.getName();
        sessionID = UUID.randomUUID();
        board = new Board(2);

        int userCount = lobby.getUsers().size();

        players = new Player[userCount + lobby.getNoOfBots()];
        List<User> tempUsers = new ArrayList<>(lobby.getUsers());

        for (int i = 0; i < userCount + lobby.getNoOfBots(); i++) {
            if (i < userCount) {
                players[i] = new Player(this, tempUsers.get(i).getUsername(), i, false);
            } else {
                createBot(i);
            }
            turnQueue.add(i);
        }
        assignColors();
        currentState = SetupState.INSTANCE;
        timer = new TurnTimer(lobby.getTimerDuration(), TimeUnit.SECONDS);

        for (Player p : getPlayers()) {
            context.getSetupInfos().putIfAbsent(p, new StateContext.SetupInfo());
        }
    }

    /**
     * A Function to generate a random permutation of arr[]
     * source: https://www.geeksforgeeks.org/shuffle-a-given-array-using-fisher-yates-shuffle-algorithm/
     *
     * @param arr    the array that will be shuffled
     * @param length the length of the array
     */
    private static void randomize(Player[] arr, int length) {

        // Start from the last element and swap one by one. We don't
        // need to run for the first element that's why i > 0
        for (int i = length - 1; i > 0; i--) {

            // Pick a random index from 0 to i
            int ijk = RANDOM.nextInt(i + 1);

            // Swap arr[i] with the element at random index
            Player temp = arr[i];
            arr[i] = arr[ijk];
            arr[ijk] = temp;
        }
    }

    private void createBot(int id) {
        amountOfBots++;
        players[id] = new Player(this, "Bot" + id, id, true);
        BotUser bot = new BotUser(this, players[id], eventBus);
        bots.add(bot);
        bot.start();
    }

    private void createBotAfterLeave(int id) {
        amountOfBots++;
        String playerName = players[id].getPlayerName();
        players[id].setPlayerToBot("Bot" + id);
        overwritePlacedPiecesOwner(players[id], playerName);
        BotUser bot = new BotUser(this, players[id], eventBus);
        bots.add(bot);
        bot.start();
    }

    private void overwritePlacedPiecesOwner(Player newOwner, String oldOwner) {
        Map<Coord, PieceDTO> placedPieces = board.getAllPlacedPieces();
        PlayerDTO playerDTO = newOwner.createDTO();
        for (Map.Entry<Coord, PieceDTO> entry : placedPieces.entrySet()) {
            if (entry.getValue().getOwner().getPlayerName().equals(oldOwner)) {
                entry.setValue(new PieceDTO(entry.getValue().getPieceType(), playerDTO));
            }
        }
    }

    /**
     * This method sends a given message to all bots who then decide if the message is for them
     *
     * @param message the Message that is sent to the bots
     */
    public void sendMessageToBots(Message message) {
        for (BotUser ai : bots) {
            ai.sendMessage(message);
        }
    }

    /**
     * This method sends a given message to a specific bot who then decide if the message is for them
     *
     * @param message the Message that is sent to the bot
     */
    public void sendMessageToBot(Message message, String botName) {
        for (BotUser ai : bots) {
            if (ai.getPlayer().getPlayerName().equals(botName)) {
                ai.sendMessage(message);
            }
        }
    }

    public Set<BotUser> getBots() {
        return bots;
    }

    public GameLobby getLobby() {
        return lobby;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the current GameState and calls it {@link GameState#beginState} method
     *
     * @param currentState the new GameState
     */
    public void setCurrentState(GameState currentState) {
        LOG.debug("{}: Set current state from {} to {}", gameSessionName, this.currentState.getClass().getSimpleName(),
                currentState.getClass().getSimpleName());
        this.currentState = currentState;
        context.getInputLock().lock();
        currentState.beginState(this, context);
        context.getInputLock().unlock();
        gameSessionService.sendToAllInGameSession(gameSessionName, new StateMessage(gameSessionName, currentState.getClass().getSimpleName()));
    }

    /**
     * This methods forwards the given {@code RequestMessage} to the current game state. Also locks
     *
     * @param message the {@code RequestMessage} that should be used by the current game state
     */
    public void userInput(RequestMessage message) throws GameStateException {
        context.getInputLock().lock();
        try {
            context.setLastRequest(message);
            currentState.action(this, context);
        } finally {
            context.getInputLock().unlock();
        }
    }

    /**
     * Builds a Buildable from a PieceType and Player. Buildable is used on the board map.
     *
     * @param piece  piece
     * @param player owner of the piece
     * @return Buildable object with player and pieceType as given.
     */
    public BuildablePiece pieceTypeToBuildable(PieceType piece, Player player) {
        switch (piece) {
            case CITY:
                return new CityPiece(player);
            case ROAD:
                return new RoadPiece(player);
            case SETTLEMENT:
                return new SettlementPiece(player);
            default:
                LOG.error("Wrong Type enum");
        }
        LOG.error("Wrong Type enum, returning null");
        return null;
    }

    /**
     * If a user wants to leave the GameSession, the user gets removed from the GameSession
     * and is replaced with a bot, who will take their position
     *
     * @param userSession the Session of the user who wants to leave the gameSession
     */
    public void leaveGameSession(Session userSession) throws UserIsNotPartOfGameSessionException {
        User user = userSession.getUser();
        if (users.contains(user)) {
            context.getInputLock().lock();
            this.users.remove(user);
            for (int i = 0; i < players.length; i++) {
                if (players[i].getPlayerName().equals(user.getUsername())) {
                    leftUsers.put(user, players[i]);
                    if (currentState == TradeState.INSTANCE) {
                        cancelUserTrades(userSession);
                    }
                    createBotAfterLeave(i);
                    break;
                }
            }
            context.getInputLock().unlock();
        } else {
            throw new UserIsNotPartOfGameSessionException();
        }
    }

    private void cancelUserTrades(Session userSession) {
        Player player = getPlayer(userSession.getUser().getUsername());
        for (TradeOffer interestTradeOffer : player.getTradeOffers()) {
            if (interestTradeOffer.getReceivingPlayer() != null) {
                getPlayer(interestTradeOffer.getReceivingPlayer()).removeTradeOffer(interestTradeOffer);
                player.removeTradeOffer(interestTradeOffer);
                sendTradeCanceledToAllPlayers(interestTradeOffer);
            } else {
                var request = new DeclineTradeOfferRequest(gameSessionName, interestTradeOffer);
                request.setSession(userSession);
                try {
                    userInput(request);
                } catch (GameStateException e) {
                    LOG.error("Trade decline after user left failed {}", interestTradeOffer);
                }
            }
        }
    }

    /**
     * this method is called if a user wants to rejoin a game session
     * <p>
     * if the user were never part of the lobby or the replacement of the user, who wants to join, is on, a CanNotRejoinException is thrown
     * else user is added to users, will replace the bot, who replaced them, is removed from map of the users who left
     * and the amountOfBots is decremented
     *
     * @param user the user who wants to rejoin
     * @throws CanNotRejoinException if the user is not part of the gameSession or his replacement is on
     */
    public void rejoinGameSession(User user) throws CanNotRejoinException {
        if (!leftUsers.containsKey(user)) {
            throw new CanNotRejoinException("Sie waren nie teil des Spiels.");
        } else if (leftUsers.get(user).getPlayerId() == turnQueue.peek()) {
            throw new CanNotRejoinException("Ihr Ersatz spielt gerade, bitte warten sie kurz und versuchen es dann noch mal.");
        } else {
            this.users.add(user);
            int id = leftUsers.get(user).getPlayerId();
            String botName = players[id].getPlayerName();
            bots.removeIf(botUser -> botUser.getPlayer().equals(players[id]));
            players[id].setBotToPlayer(user.getUsername());
            overwritePlacedPiecesOwner(players[id], botName);
            leftUsers.remove(user);
            amountOfBots--;
            LOG.debug("User {} rejoined the GameSession {}.", user.getUsername(), gameSessionName);
        }
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Returns the player in this game session with the given name if it exists, otherwise {@code null}
     *
     * @param name the name of the player
     * @return the player in this game session with the given name if it exists, otherwise {@code null}
     */
    public Player getPlayer(String name) {
        for (Player player : players) {
            if (player.getPlayerName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    public boolean hasUserLeftGameSession(User user) {
        return leftUsers.containsKey(user);
    }

    /**
     * Returns a copy of the array that stores all players in this game session
     *
     * @return a copy of the array that stores all players in this game session
     */
    public Player[] getPlayers() {
        return players.clone();
    }

    /**
     * Returns a copy of the array that stores all players in this game session as a list
     *
     * @return a copy of the array that stores all players in this game session as a list
     */
    public List<Player> getPlayersList() {
        return Arrays.asList(players.clone());
    }

    /**
     * Returns the player in this game session with the given id if it exists, otherwise {@code null}
     *
     * @param playerId the id of the player
     * @return the player in this game session with the given id if it exists, otherwise {@code null}
     */
    public Player getPlayer(int playerId) {
        try {
            return players[playerId];
        } catch (IndexOutOfBoundsException exception) {
            return null;
        }
    }

    /**
     * getter for the amountOfBots
     *
     * @return the amount of bots in this GameSession as int
     */
    public int getAmountOfBots() {
        return amountOfBots;
    }

    /**
     * getter for the users
     *
     * @return the users that are part of the gameSession
     */
    public Set<User> getUsers() {
        return users;
    }

    public String getGameSessionName() {
        return gameSessionName;
    }

    /**
     * Sets the given player to ready and starts the game timer if all players are ready
     *
     * @param playerName the name of the ready player
     */
    public void playerReady(String playerName) {
        readyPlayers.add(getPlayer(playerName));
        if (!gameStarted && readyPlayers.size() == lobby.getUsers().size()) {
            gameSessionService.sendToAllInGameSession(gameSessionName, new NextTurnMessage(gameSessionName, getWhoseTurn().createDTO()));
            gameStarted = true;
            startNewFullTimer();
            sendSetupLocation(getWhoseTurn(), PieceType.SETTLEMENT);
            gameSessionService.sendToAllInGameSession(gameSessionName, new StateMessage(gameSessionName, "SetupState"));
        } else if (gameStarted) {
            gameSessionService.sendToAllInGameSession(gameSessionName,
                    new StateMessage(gameSessionName, currentState.getClass().getSimpleName()));
        }
    }

    /**
     * Sends a {@link SetupLocationMessage} message to the given player
     *
     * @param player    the player who will be receiving their setup locations
     * @param pieceType the piece type that the setup locations will be for
     */
    public void sendSetupLocation(Player player, PieceType pieceType) {
        var message = new SetupLocationMessage(gameSessionName, player.getLegalNodes(pieceType), pieceType);
        gameSessionService.sendToPlayerInGameSession(gameSessionName, message, player.getPlayerName(), player.isBot());
    }

    /**
     * Sends a {@link RobberPlacingMessage} message to the given player
     *
     * @param player the player who will be receiving the message
     */
    public void sendStartRobber(Player player) {
        var message = new RobberPlacingMessage(gameSessionName);
        gameSessionService.sendToPlayerInGameSession(gameSessionName, message, player.getPlayerName(), player.isBot());
    }

    /**
     * This method tells the current state to end the current turn
     */
    public void endTurn(Player player) {
        context.getInputLock().lock();
        if (player.equals(getWhoseTurn())) {
            currentState.endTurn(this, context);
        }
        context.getInputLock().unlock();
    }

    /**
     * Starts a new timer with lobby default timer duration and advances the current turn
     */
    public void startNewTurnTimer() {
        LOG.debug("New Turn Timer started in {}", gameSessionName);
        gameSessionService.sendToAllInGameSession(gameSessionName, new NextTurnMessage(gameSessionName, advanceTurn().createDTO()));
        startNewFullTimer();
    }

    /**
     * Starts a new timer with lobby default timer duration
     */
    public void startNewFullTimer() {
        LOG.debug("New Timer started in {}", gameSessionName);
        startNewTimer(lobby.getTimerDuration(), TimeUnit.SECONDS);
    }

    /**
     * Starts a new sub timer with the given timer duration
     * <p>
     * This timer should be started by states whose endTurn method starts a new full timer and does not advance the turn
     *
     * @param timerDuration the duration of the sub timer
     * @param timeUnit      he time unit of the timerDuration parameter
     */
    public void startNewSubTimer(long timerDuration, TimeUnit timeUnit) {
        LOG.debug("New Sub Timer started in {}", gameSessionName);
        startNewTimer(timerDuration, timeUnit);
    }

    private void startNewTimer(long timerDuration, TimeUnit timeUnit) {
        gameSessionService.sendToAllInGameSession(gameSessionName, new TimerRestartMessage(gameSessionName, (int) timeUnit.toSeconds(timerDuration)));
        timer.restartTimer(new TimerTask() {
            @Override
            public void run() {
                endTurn(getWhoseTurn());
            }
        }, timerDuration, timeUnit);
    }

    /**
     * Cancels the active timer and terminates all bots
     */
    public void stopGame() {
        timer.stop();
        timer.kill();
        bots.forEach(BotUser::terminate);
    }

    /**
     * Checks if someone won the game
     * <p>
     * send an endGame Message if someone has reached the required amount of victory points
     */
    public void checkVictory() {
        for (Player player : players) {
            if (player.getNumOfTotalVP() >= lobby.getNumVP()) {
                gameSessionService.endGame(gameSessionName);
            }
        }
    }

    /**
     * Get the Player with the Longest Road
     *
     * @return player with the longest road in the current game
     */
    public Player getPlayerWithLongestRoad() {
        return board.getPlayerWithLongestRoad();
    }

    /**
     * Advance the turnQueue of the gameSession After every use, it returns the next player in the array.
     *
     * @return the player which turn it is next
     */
    public Player advanceTurn() {
        turnQueue.add(turnQueue.remove());
        if (reverseTurnQueue) {
            reverseTurnQueue = false;
            Deque<Integer> stack = new ArrayDeque<>();
            while (!turnQueue.isEmpty()) {
                stack.push(turnQueue.remove());
            }

            while (!stack.isEmpty()) {
                turnQueue.add(stack.pop());
            }
        }
        if (turnQueue.peek() != null) {
            Player player = getPlayer(turnQueue.peek());
            player.setPlayedDevCardThisTurn(false);
            player.getInventory().updatePlayableDevCards();
            return player;
        }
        return null;
    }

    /**
     * Reverses the turn queue
     */
    public void reverseTurnQueueNextAdvance() {
        reverseTurnQueue = true;
    }

    /**
     * Getter to see if the game already started
     *
     * @return the boolean that shows if the game started
     */
    public boolean isGameStarted() {
        return gameStarted;//TODO: Wann startet game? Nach setup oder vorher?
    }

    /**
     * Getter to see if the game has debug mode enabled
     *
     * @return the boolean that shows if the debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * getter for which player is on
     *
     * @return the player who is on
     */
    public Player getWhoseTurn() {
        if (turnQueue.peek() != null) {
            return getPlayer(turnQueue.peek());
        }
        return null;
    }

    /**
     * Returns the bank inventory
     *
     * @return the bank inventory
     */
    public BankInventory getBankInventory() {
        return bankInventory;
    }

    /**
     * Assigns every player a PlayerColor.
     * In order to that the assigning is random, the players-array gets copied and shuffled after.
     */
    private void assignColors() {
        Player[] playersCopy = new Player[players.length];
        System.arraycopy(players, 0, playersCopy, 0, players.length);
        randomize(playersCopy, playersCopy.length);
        for (int i = 0; i < players.length; i++) {
            Player player = playersCopy[i];
            player.setColor(PlayerColor.valueOf(i));
            LOG.debug(" Player {} is assigned to the color {}", player.getPlayerName(), player.getColor());
        }

    }

    /**
     * Sends a {@link NewTradeOfferMessage} message with the given trade offer to all players in this game session
     *
     * @param tradeOffer the trade offer that has been created
     */
    public void sendNewTradeOfferToAllPlayers(TradeOffer tradeOffer) {
        gameSessionService.sendToAllInGameSession(gameSessionName, new NewTradeOfferMessage(gameSessionName, tradeOffer));
    }

    /**
     * Sends a {@link NewTradeOfferMessage} message with the given trade offer to the receiving player
     *
     * @param tradeOffer the trade offer that has been created
     */
    public void sendCounterTradeOffer(TradeOffer tradeOffer) {
        gameSessionService.sendToPlayerInGameSession(gameSessionName, new NewTradeOfferMessage(gameSessionName, tradeOffer),
                tradeOffer.getReceivingPlayer(), getPlayer(tradeOffer.getReceivingPlayer()).isBot());
        gameSessionService.sendToPlayerInGameSession(gameSessionName, new NewTradeOfferMessage(gameSessionName, tradeOffer),
                tradeOffer.getOfferingPlayer(), getPlayer(tradeOffer.getOfferingPlayer()).isBot());
    }

    /**
     * Sends a log message, that the bank trade was invalid
     *
     * @param playerName the name of the player, that wants to trade with the bank
     */
    public void sendBankTradeInvalid(String playerName) {
        LOG.debug("Invalid bank trade from {} in game session {}", playerName, gameSessionName);
    }

    /**
     * Sends a {@link TradeOfferInterestMessage} message with the given trade offer and the interested player to the offering player
     *
     * @param tradeOffer       the trade offer that should be send to the offering player
     * @param interestedPlayer the receiving player that has accepted the trade offer
     */
    public void sendTradeInterestToOfferingPlayer(TradeOffer tradeOffer, String interestedPlayer) {
        TradeOfferInterestMessage message = new TradeOfferInterestMessage(gameSessionName, tradeOffer, getPlayer(interestedPlayer).createDTO());
        gameSessionService.sendToPlayerInGameSession(gameSessionName, message, tradeOffer.getOfferingPlayer(),
                getPlayer(tradeOffer.getOfferingPlayer()).isBot());
    }

    /**
     * Sends a {@link TradeOfferDeclinedMessage} message with the given trade offer and the player who has declined to the offering player
     *
     * @param tradeOffer the trade offer that has been declined
     * @param declined   the receiving player that has declined the trade offer
     */
    public void sendTradeDeclinedToOfferingPlayer(TradeOffer tradeOffer, String declined) {
        TradeOfferDeclinedMessage message = new TradeOfferDeclinedMessage(gameSessionName, tradeOffer, getPlayer(declined).createDTO());
        gameSessionService.sendToPlayerInGameSession(gameSessionName, message, tradeOffer.getOfferingPlayer(),
                getPlayer(tradeOffer.getOfferingPlayer()).isBot());
    }

    /**
     * Sends a {@link TradeOfferCanceledMessage} message with the given trade offer to all players in this game session
     *
     * @param tradeOffer the trade offer that has been canceled
     */
    public void sendTradeCanceledToAllPlayers(TradeOffer tradeOffer) {
        gameSessionService.sendToAllInGameSession(gameSessionName, new TradeOfferCanceledMessage(gameSessionName, tradeOffer));
    }

    /**
     * Sends a {@link TradeOfferAcceptedMessage} message with the given trade offer and the player who has been accepted by the offering player
     * to all players in the game session
     *
     * @param tradeOffer the trade offer that has been accepted
     * @param receiver   the receiving player that has been accepted by the offering player
     */
    public void sendTradeAcceptedToAllPlayers(TradeOffer tradeOffer, Player receiver) {
        gameSessionService.sendToAllInGameSession(gameSessionName, new TradeOfferAcceptedMessage(gameSessionName, tradeOffer, receiver.createDTO()));
    }

    /**
     * Sends a {@link DiceResultMessage} message with the given player name and the value of the dice roll to all players in the game session
     *
     * @param playerName name of the player
     * @param diceResult value of the dice roll
     */
    public void sendDiceResultToAllPlayers(String playerName, int diceResult) {
        gameSessionService.sendToAllInGameSession(gameSessionName, new DiceResultMessage(gameSessionName, playerName, diceResult));
    }


    /**
     * Sends a {@link DiscardNotificationMessage} message to the given payer
     *
     * @param player the player who needs to discard resources
     * @param amount the amount the player needs to discard
     */
    public void sendDiscardNotificationToPlayer(Player player, int amount) {
        gameSessionService.sendToPlayerInGameSession(gameSessionName, new DiscardNotificationMessage(gameSessionName, amount),
                player.getPlayerName(), player.isBot());
    }

    /**
     * Sends a {@link DiscardCardsResponse} message to the player to inform them of their discard-status
     *
     * @param request                  the request which is answered
     * @param hasSuccessfullyDiscarded true if the player has successfully discarded resources.
     */
    public void sendDiscardResponseToPlayer(AbstractRequestMessage request, boolean hasSuccessfullyDiscarded, String message, boolean bot) {
        gameSessionService.sendResponseToPlayer(gameSessionName, request,
                new DiscardCardsResponse(gameSessionName, hasSuccessfullyDiscarded, message), bot);
    }

    /**
     * Sends a {@link DiscardCardsResponse} message to the player to inform them of their discard-status
     *
     * @param hasSuccessfullyDiscarded true if the player has successfully discarded resources.
     * @param message                  a message for the client
     */
    public void sendDiscardResponseToAll(boolean hasSuccessfullyDiscarded, String message) {
        gameSessionService.sendToAllInGameSession(gameSessionName, new DiscardCardsMessage(gameSessionName, hasSuccessfullyDiscarded, message));
    }

    public GameSessionService getGameSessionService() {
        return gameSessionService;
    }

    /**
     * Sets the current game state of this game session
     *
     * @param state a String representing the state (eg "trade")
     */
    public void debugSetState(String state) {
        switch (state) {
            case "build":
                setCurrentState(BuildState.INSTANCE);
                break;
            case "dice":
                setCurrentState(DiceState.INSTANCE);
                break;
            case "end":
                setCurrentState(EndState.INSTANCE);
                break;
            case "play":
                setCurrentState(PlayState.INSTANCE);
                break;
            case "robberDiscard":
                setCurrentState(RobberDiscardState.INSTANCE);
                break;
            case "robberPlacing":
                setCurrentState(RobberPlacingState.INSTANCE);
                break;
            case "setup":
                setCurrentState(SetupState.INSTANCE);
                break;
            case "trade":
                setCurrentState(TradeState.INSTANCE);
                break;
            default:
        }
    }

    /**
     * DEBUG method that sets the resources of a given player
     *
     * @param resourceMap the resources
     * @param playerName  the player who will get the resources
     */
    public void debugSetResources(ResourceEnumMap resourceMap, String playerName) {
        inventoryService.setResources(getPlayer(playerName), resourceMap);
    }

    /**
     * DEBUG method that sets the dev cards of a given player
     *
     * @param devCards   the dev cards
     * @param playerName the player who will get the dev cards
     */
    public void debugSetDevCards(DevCardEnumMap devCards, String playerName) {
        inventoryService.updateDevCards(getPlayer(playerName), devCards);
    }

    /**
     * Send A chat Message with the given content as a System User
     *
     * @param message message
     */
    public void sendLogMessage(String message) {
        gameSessionService.sendLogMessage(gameSessionName, message);
    }

    /**
     * sends a VPUpdateMessage for the given player
     *
     * @param player the player whose victory points label will be updated
     */
    public void sendVPUpdateMessage(Player player) {
        gameSessionService.sendVPUpdateMessage(gameSessionName, player.createDTO(), player.getNumOfPublicVP());
    }


    /**
     * Send a player picker message to the given receiver
     *
     * @param playersToPick the players who the player will be able to pick from
     * @param receiver      the player who will be able to pick
     */
    public void sendPlayerPickerMessage(List<Player> playersToPick, Player receiver) {
        List<PlayerDTO> dtoList = playersToPick.stream().map(Player::createDTO).collect(Collectors.toList());
        gameSessionService.sendPlayerPickerMessage(gameSessionName, dtoList, receiver);
    }

    /**
     * Update the robber Position for every client in this Lobby
     *
     * @param newRobberPosition
     */
    public void sendUpdatedRobberPosition(Coord newRobberPosition) {
        gameSessionService.sendToAllInGameSession(gameSessionName, new RobberPositionUpdateMessage(gameSessionName, newRobberPosition));
    }

    /**
     * Return the InventoryService from the GameSession
     *
     * @return the InventoryService from the GameSession
     */
    public InventoryService getInventoryService() {
        return inventoryService;
    }


    /**
     * Return the amount of Development Cards remaining in the bank Inventory
     *
     * @return amount of cards remaining
     */
    public int getDevCardsRemaining() {
        return bankInventory.getAmountRemaining();
    }

    /**
     * Get Standings from the current game  including secret points
     *
     * @return sorted list of players
     */
    public List<PlayerDTO> getStandings() {
        ArrayList<PlayerDTO> dtoList = new ArrayList<>();
        getPlayersList().forEach(player -> {
            PlayerDTO dto = player.createDTO();
            dto.setVictoryPoints(player.getNumOfTotalVP());
            dtoList.add(dto);
        });
        dtoList.sort(Comparator.comparing(PlayerDTO::getVictoryPoints).reversed());
        return dtoList;
    }

    /**
     * Getter for the current StateContext
     *
     * @return current State Context of this session
     */
    public StateContext getContext() {
        return context;
    }

    /**
     * Sends a Message updating the longest Road info for the clients
     *
     * @param gameSession           gameSessionName
     * @param playerWithLongestRoad the player with the longest road
     */
    public void sendLongestRoadUpdate(GameSession gameSession, PlayerDTO playerWithLongestRoad) {
        gameSessionService.sendLongestRoadUpdateMessage(gameSession.getGameSessionName(), playerWithLongestRoad);
    }

    /**
     * Getter for the playedDevCardInDiceState
     *
     * @return playedDevCardInDiceState
     */
    public boolean isPlayedDevCardInDiceState() {
        return playedDevCardInDiceState;
    }

    /**
     * Setter for the playedDevCardInDiceState
     *
     * @param playedDevCardInDiceState the boolean thats shows if a devCard was played in DiceState
     */
    public void setPlayedDevCardInDiceState(boolean playedDevCardInDiceState) {
        this.playedDevCardInDiceState = playedDevCardInDiceState;
    }
}
