package de.uol.swp.server.game.session;

import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.debug.SetDevCardsRequest;
import de.uol.swp.common.game.debug.SetNextDiceRequest;
import de.uol.swp.common.game.debug.SetResourcesRequest;
import de.uol.swp.common.game.debug.SetStateRequest;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.*;
import de.uol.swp.common.game.message.inventory.BuildableObjectMessage;
import de.uol.swp.common.game.message.inventory.DevCardCountMessage;
import de.uol.swp.common.game.message.inventory.DevCardDetailedCountMessage;
import de.uol.swp.common.game.message.inventory.ResourceCardCountMessage;
import de.uol.swp.common.game.request.*;
import de.uol.swp.common.game.request.build.CancelBuildRequest;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import de.uol.swp.common.game.request.build.StartBuildRequest;
import de.uol.swp.common.game.request.trade.AbstractTradeRequest;
import de.uol.swp.common.game.response.CanNotLeaveGameResponse;
import de.uol.swp.common.game.response.CanNotRejoinGameResponse;
import de.uol.swp.common.game.response.GameSessionLeftResponse;
import de.uol.swp.common.game.response.RejoinGameResponse;
import de.uol.swp.common.game.response.build.PlaceObjectResponse;
import de.uol.swp.common.game.response.trade.NotEnoughResourcesResponse;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.exception.*;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.ai.BotUser;
import de.uol.swp.server.message.SendSystemMessage;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Handles Game Requests
 */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class GameSessionService extends AbstractService {
    private static final Logger LOG = LogManager.getLogger(GameSessionService.class);
    private final GameSessionManagement gameSessionManagement;
    private final AuthenticationService authenticationService;

    /**
     * Constructor
     *
     * @param bus the EvenBus used throughout the server
     */
    @Inject
    public GameSessionService(GameSessionManagement gameSessionManagement, EventBus bus, AuthenticationService authenticationService) {
        super(bus);
        this.gameSessionManagement = gameSessionManagement;
        this.authenticationService = authenticationService;
    }


    /**
     * Create a game session in SessionManagement
     *
     * @param lobby GameLobby used for GameSession
     */
    public GameSession createGameSession(GameLobby lobby) {
        return gameSessionManagement.createGameSession(lobby);
    }

    /**
     * Handles PlayerFinishedLoadingRequest found on the EventBus
     * <p>
     * Used to send information the players that they can only make use of when their game view has loaded
     *
     * @param request the PlayerFinishedLoadingRequest found on the EventBus
     */
    @Subscribe
    public void onPlayerFinishedLoadingRequest(PlayerFinishedLoadingRequest request) {
        Optional<Session> userSession = request.getSession();
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (gameSession.isPresent() && userSession.isPresent()) {
            gameSession.get().playerReady(userSession.get().getUser().getUsername());
        }
    }

    /**
     * Reacts to a new PlaceObjectRequest found on eventbus
     * First check if the player is allowed to build and answer them.
     * If the request was approved message all with a new building placement
     *
     * @param request the request
     */
    @Subscribe
    public void onPlaceObjectRequest(PlaceObjectRequest request) {
        LOG.debug("onPlaceObjectRequest {}", request);
        Optional<Session> userSession = request.getSession();
        Optional<GameSession> optional = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (optional.isPresent() && userSession.isPresent()) {
            GameSession gameSession = optional.get();

            PieceType type = request.getObjectToPlace();
            Coord coord = request.getCoord();
            try {
                gameSession.userInput(request);
            } catch (GameStateException gameStateException) {
                PlaceObjectResponse response = new PlaceObjectResponse(gameSession.getGameSessionName(), type, false, coord,
                        gameStateException.getMessage());
                sendResponseToPlayer(gameSession.getGameSessionName(), request, response, false);
                LOG.debug(gameStateException.getMessage());
            }

        } else {
            LOG.error("Session does not exist {}", request.getGameSessionName());
        }
    }


    /**
     * Gets the correct GameSession based on name and send a new initial field values to every connected Player
     *
     * @param name GameSessionName
     */
    public void sendInitialBoard(String name, String rejoiningUser) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(name);
        if (gameSession.isPresent()) {
            Multimap<Integer, Coord> numbersFromBoard = gameSession.get().getBoard().getHexagonNumbers();
            Map<Coord, ResourceTile> resourceTileMap = gameSession.get().getBoard().getResourceTileMap();
            Map<PortCoord, Port> ports = gameSession.get().getBoard().getPorts();
            InitialBoardMessage initMessage = new InitialBoardMessage(name, numbersFromBoard, resourceTileMap, ports);
            if (rejoiningUser == null) {
                sendToAllInGameSession(name, initMessage);
            } else {
                sendToPlayerInGameSession(name, initMessage, rejoiningUser, false);
            }
        }
    }

    /**
     * Handles TurnEndRequest found on the EventBus
     * <p>
     * If a TurnEndRequest is detected on the EventBus, this method is called.
     * It checks is the gameSession is present and after that it calls the passTheMoveOn method
     *
     * @param turnEndRequest the StartGameSessionRequest found on the EventBus
     * @see GameSession
     * @see de.uol.swp.common.game.request.TurnEndRequest
     */
    @Subscribe
    public void onTurnEndRequest(TurnEndRequest turnEndRequest) {

        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(turnEndRequest.getGameSessionName());
        gameSession.ifPresent(session -> session.endTurn(session.getWhoseTurn()));
    }

    /**
     * Handles GameLeaveUserRequests found on the EventBus
     * <p>
     * If a GameLeaveUserRequest is detected on the EventBus, this method is called.
     * It removes a user from a GameSession stored in the GameSessionManagement.
     * If the last real user has left the GameSession, it deletes the GameSession and cancels the timer.
     * if the user who leaves is on, the passTheMoveOn method is called
     * If there are still users left in the GameSession it instead sends a UserLeftGameSessionMessage to them
     *
     * @param leaveGameRequest the GameLeaveUserRequest found on the eventbus
     * @see GameSession
     * @see LeaveGameRequest
     */
    @Subscribe
    public void onGameLeaveUserRequest(LeaveGameRequest leaveGameRequest) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(leaveGameRequest.getGameSessionName());
        Optional<Session> session = leaveGameRequest.getSession();
        if (gameSession.isPresent() && session.isPresent()) {
            GameSession game = gameSession.get();
            String gameName = game.getGameSessionName();
            boolean debugEnabled = game.isDebugEnabled();
            Session userSession = session.get();
            if (game.getPlayers().length - game.getAmountOfBots() == 1) {
                endGame(gameName);
            } else {
                try {
                    game.leaveGameSession(userSession);
                    PlayerDTO[] playerDTOS = Arrays.stream(game.getPlayers()).map(Player::createDTO).toArray(PlayerDTO[]::new);
                    sendToAllInGameSession(gameName, new UpdateGameSessionMessage(gameName, new GameDTO(gameName, debugEnabled, playerDTOS)));
                } catch (UserIsNotPartOfGameSessionException exception) {
                    CanNotLeaveGameResponse response = new CanNotLeaveGameResponse(gameName, "Sie sind nicht Teil dieser Sitzung");
                    sendResponseToPlayer(gameName, leaveGameRequest, response, false);
                }
            }
            GameSessionLeftResponse response = new GameSessionLeftResponse(leaveGameRequest.getGameSessionName());
            sendResponseToPlayer(gameName, leaveGameRequest, response, false);
        } else {
            CanNotLeaveGameResponse response = new CanNotLeaveGameResponse(leaveGameRequest.getGameSessionName(),
                    "Die Spielsitzung existiert nicht.");
            sendResponseToPlayer(leaveGameRequest.getGameSessionName(), leaveGameRequest, response, false);
        }
    }

    /**
     * Handles RejoinGameSessionRequests found on the EventBus
     * <p>
     * if the game session is present and the user can rejoin the game session without an Exception being thrown a rejoinGameResponse is sent back.
     * if the game session is not present or an exception is thrown, a CanNotRejoinResponse is sent back
     *
     * @param request the RejoinGameSessionRequest that is sent
     */
    @Subscribe
    public void onRejoinGameSessionRequest(RejoinGameSessionRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getGameSessionName());
        Optional<Session> session = request.getSession();
        ResponseMessage response;
        boolean userCanRejoin = false;
        if (gameSession.isPresent() && session.isPresent()) {
            GameSession game = gameSession.get();
            String gameName = request.getGameSessionName();
            try {
                gameSession.get().rejoinGameSession(session.get().getUser());
                Player[] players = gameSession.get().getPlayers();
                PlayerDTO[] playerDTOS = Arrays.stream(players).map(Player::createDTO).toArray(PlayerDTO[]::new);
                boolean debugEnabled = gameSession.get().isDebugEnabled();
                if (game.isGameStarted()) {
                    response = new RejoinGameResponse(gameName, new GameDTO(gameName, debugEnabled, playerDTOS), game.getWhoseTurn().createDTO());
                } else {
                    response = new RejoinGameResponse(gameName, new GameDTO(gameName, debugEnabled, playerDTOS), null);
                }
                userCanRejoin = true;

            } catch (CanNotRejoinException exception) {
                response = new CanNotRejoinGameResponse(gameName, exception.getReason());
            }
        } else {
            response = new CanNotRejoinGameResponse(request.getGameSessionName(), "Spielsitzung existiert nicht");
        }
        sendResponseToPlayer(request.getGameSessionName(), request, response, false);
        if (userCanRejoin) {
            setGameSessionViewForRejoinedUser(gameSession.get(), session.get().getUser());
        }
    }

    private void setGameSessionViewForRejoinedUser(GameSession game, User user) {
        Player[] players = game.getPlayers();
        PlayerDTO[] playerDTOS = Arrays.stream(players).map(Player::createDTO).toArray(PlayerDTO[]::new);
        String gameName = game.getGameSessionName();
        String userName = user.getUsername();
        Player player = game.getPlayer(user.getUsername());
        boolean debugEnabled = game.isDebugEnabled();
        sendToAllInGameSession(gameName, new UpdateGameSessionMessage(gameName, new GameDTO(gameName, debugEnabled, playerDTOS)));
        sendInitialBoard(gameName, userName);
        initPlacedObjects(gameName, userName);
        for (Player p : game.getPlayers()) {
            ResourceCardCountMessage message = new ResourceCardCountMessage(p.getGameSessionName(), p.createDTO(),
                    p.getInventory().getResources());
            sendToPlayerInGameSession(gameName, message, userName, false);
            VPUpdateMessage vpUpdateMessage = new VPUpdateMessage(p.getGameSessionName(), p.createDTO(), p.getNumOfPublicVP());
            sendToPlayerInGameSession(gameName, vpUpdateMessage, userName, false);
            int count = 0;
            for (Map.Entry<DevCard, Integer> entry : p.getInventory().getDevCards().entrySet()) {
                count += entry.getValue();
            }
            DevCardCountMessage devCardCountMessage = new DevCardCountMessage(gameName, p.createDTO(), count);
            sendToPlayerInGameSession(gameName, devCardCountMessage, userName, false);
        }
        DevCardDetailedCountMessage devCardDetailedCountMessage = new DevCardDetailedCountMessage(gameName, player.getInventory().getDevCards());
        sendToPlayerInGameSession(gameName, devCardDetailedCountMessage, userName, false);
        sendToPlayerInGameSession(gameName, new RobberPositionUpdateMessage(gameName, game.getBoard().getRobber().getCoord()), userName, false);
        if (game.getContext().getPlayerWithLargestArmy() != null) {
            KnightUpdateMessage knightUpdateMessage = new KnightUpdateMessage(gameName, game.getContext().getPlayerWithLargestArmy().createDTO());
            sendToPlayerInGameSession(gameName, knightUpdateMessage, userName, false);
        }
        if (game.getBoard().getPlayerWithLongestRoad() != null) {
            LongestRoadUpdateMessage longestRoadUpdateMessage = new LongestRoadUpdateMessage(gameName,
                    game.getBoard().getPlayerWithLongestRoad().createDTO());
            sendToPlayerInGameSession(gameName, longestRoadUpdateMessage, userName, false);
        }
        BuildableObjectMessage buildableObjectMessage = new BuildableObjectMessage(gameName, player.createDTO(),
                player.getInventory().getAvailablePieces());
        sendToPlayerInGameSession(gameName, buildableObjectMessage, userName, false);
        DevCardRemainingMessage devCardRemainingMessage = new DevCardRemainingMessage(gameName, game.getDevCardsRemaining());
        sendToPlayerInGameSession(gameName, devCardRemainingMessage, userName, false);


    }

    /**
     * initiates the placed objects for a rejoining user, to be displayed
     *
     * @param name of the gameSession
     */

    private void initPlacedObjects(String name, String userName) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(name);
        if (gameSession.isPresent()) {
            GameSession game = gameSession.get();

            RejoinBoardMessage message = new RejoinBoardMessage(game.getGameSessionName(), game.getBoard().getAllPlacedPieces());
            sendToPlayerInGameSession(game.getGameSessionName(), message, userName, false);
        }
    }


    /**
     * Handles GameEndedRequestGameSession found on the EventBus
     * <p>
     * If a GameEndedRequestGameSession is detected on the EventBus, this method is called.
     * It deletes the GameSession
     *
     * @param gameEndedRequestGameSession the GameEndedRequestGameSession found on the eventbus
     * @see GameSession
     * @see GameEndedRequestGameSession
     */
    @Subscribe
    public void onGameEndedRequestGameSession(GameEndedRequestGameSession gameEndedRequestGameSession) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession((gameEndedRequestGameSession.getGameSessionName()));
        if (gameSession.isPresent() && gameSession.get().isDebugEnabled()) {
            gameSession.get().stopGame();
            gameSessionManagement.dropGameSession(gameEndedRequestGameSession.getGameSessionName());
            sendToAllInGameSession(gameEndedRequestGameSession.getGameSessionName(),
                    new GameSessionDroppedMessage(gameEndedRequestGameSession.getGameSessionName()));
        }
    }

    /**
     * Send the end game message to all players
     */
    public void endGame(String lobbyName) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(lobbyName);
        if (gameSession.isPresent()) {
            var standings = gameSession.get().getStandings();
            sendToAllInGameSession(lobbyName, new GameOverMessage(lobbyName, standings));
            gameSession.get().stopGame();
            gameSessionManagement.dropGameSession(lobbyName);
        } else {
            LOG.error("Game not found {}", lobbyName);
        }

//
    }

    /**
     * Handles BuyDevCardRequests found on the EventBus
     * <p>
     * If a BuyDevCardRequest is detected on the EventBus, this method is called.
     * It checks is the gameSession is present and then calls {@link GameSession#userInput(RequestMessage)}
     * expecting the {@link de.uol.swp.server.game.state.PlayState} to be the current game state
     *
     * @param request the BuyDevCardRequest found on the EventBus
     */
    @Subscribe
    public void onBuyDevCardRequest(BuyDevCardRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (gameSession.isPresent()) {
            try {
                gameSession.get().userInput(request);
            } catch (InvalidGameStateException e) {
                LOG.debug("The game {} is currently not in the play state", gameSession.get());
            } catch (GameStateException e) {
                LOG.debug(e.getMessage());
            }
        } else {
            LOG.debug("The game {} does not exist", request.getGameSessionName());
        }
    }

    /**
     * Handles StartBuildRequests found on the EventBus
     * <p>
     * If a StartBuildRequest is detected on the EventBus, this method is called.
     * It checks is the gameSession is present and then calls {@link GameSession#userInput(RequestMessage)}
     *
     * @param request the StartBuildRequest found on the EventBus
     */
    @Subscribe
    public void onStartBuildRequest(StartBuildRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (gameSession.isPresent()) {
            try {
                gameSession.get().userInput(request);
            } catch (GameStateException e) {
                LOG.debug(e);
            }
        }
    }

    /**
     * Handles CancelBuildRequests found on the EventBus
     * <p>
     * If a CancelBuildRequest is detected on the EventBus, this method is called.
     * It checks is the gameSession is present and then calls {@link GameSession#userInput(RequestMessage)}
     *
     * @param request the CancelBuildRequest found on the EventBus
     */
    @Subscribe
    public void onCancelBuildRequest(CancelBuildRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (gameSession.isPresent()) {
            try {
                gameSession.get().userInput(request);
            } catch (GameStateException e) {
                LOG.debug(e);
            }
        }
    }

    // -------------------------------------------------------------------------------
    // Trade Messages
    // -------------------------------------------------------------------------------

    /**
     * Handles TradeRequests found on the EventBus
     * <p>
     * If a subclass of the AbstractTradeRequest is detected on the EventBus, this method is called.
     * It checks is the gameSession is present and then calls {@link GameSession#userInput(RequestMessage)}
     *
     * @param request the subclass of AbstractTradeRequest found on the EventBus
     */
    @Subscribe
    private void onTradeRequest(AbstractTradeRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (gameSession.isPresent()) {
            try {
                gameSession.get().userInput(request);
            } catch (TradeNotEnoughResourcesException e) {
                NotEnoughResourcesResponse response = new NotEnoughResourcesResponse(gameSession.get().getGameSessionName());
                sendResponseToPlayer(request.getGameSessionName(), request, response, false);
                LOG.debug(e.getMessage());
            } catch (InvalidGameStateException e) {
                LOG.debug("The game {} is currently not in the trade state", gameSession.get());
            } catch (GameStateException e) {
                LOG.debug(e.getMessage());
            }
        } else {
            LOG.debug("The game {} does not exist", request.getGameSessionName());
        }
    }

    /**
     * Handles Discard Requests found on Eventbus
     *
     * @param request the request
     */
    @Subscribe
    private void onDiscardCardsRequest(DiscardCardsRequest request) {
        Optional<GameSession> optional = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (optional.isPresent()) {
            GameSession gameSession = optional.get();
            try {
                gameSession.userInput(request);
            } catch (InvalidGameStateException e) {
                LOG.debug("The game {} is currently not in Robber Discard", gameSession);
            } catch (GameStateException e) {
                LOG.debug(e.getMessage());
            }
        } else {
            LOG.error("The game {} does not exist", request.getGameSessionName());
        }

    }

    /**
     * Handles debug set state requests without validating the user and lobby that it came from
     *
     * @param request the {@code SetStateRequest} found on the EventBus
     */
    @Subscribe
    public void onSetStateRequest(SetStateRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getSessionName());
        gameSession.ifPresent(session -> session.debugSetState(request.getState()));
    }

    /**
     * Method subscribes to the RobberPlacingRequest wich contains the new position for the
     * robber handles the robberstate / sends a RobberPlacingResponse
     *
     * @param request send by the Client with the new coordinations for the robber
     */
    @Subscribe
    public void onRobberPlacingRequest(RobberPlacingRequest request) {
        Optional<GameSession> optional = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (optional.isPresent()) {
            GameSession gameSession = optional.get();
            try {
                gameSession.userInput(request);
            } catch (GameStateException e) {
                LOG.debug(e);
            }

        }

    }

    /**
     * @param request
     */
    @Subscribe
    public void onPlayerPickerRequest(PlayerPickerRequest request) {
        Optional<GameSession> optional = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (optional.isPresent()) {
            GameSession gameSession = optional.get();
            try {
                gameSession.userInput(request);
            } catch (GameStateException e) {
                LOG.debug(e);
            }
        }
    }

    /**
     * Handles roll dice  requests by generating a random number between 1 and 12 and sending
     * a RollDiceResponse and a DistributeResourceMessage or a DropCardsMessage
     *
     * @param request the RollDiceRequest send from the Client
     */
    @Subscribe
    public void onRollDiceRequest(RollDiceRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (gameSession.isPresent()) {
            try {
                gameSession.get().userInput(request);
            } catch (InvalidGameStateException e) {
                LOG.debug("The game {} is currently not in the dice state", gameSession.get());
            } catch (GameStateException e) {
                LOG.debug(e.getMessage());
            }
        } else {
            LOG.debug("The game {} does not exist", request.getGameSessionName());
        }
    }

    @Subscribe
    public void onSetNextDiceRequest(SetNextDiceRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getSessionName());
        if (gameSession.isPresent()) {
            gameSession.get().getContext().setNextDiceResult(request.getDiceValue());
        } else {
            LOG.debug("The game {} does not exist", request.getSessionName());
        }
    }

    /**
     * Handles debug set resources requests without validating the user and lobby that it came from
     *
     * @param request the {@code SetResourcesRequest} found on the EventBus
     */
    @Subscribe
    public void onSetResourcesRequest(SetResourcesRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getLobbyName());
        gameSession.ifPresent(session -> session.debugSetResources(request.getResourceMap(), request.getPlayerName()));
    }

    /**
     * Handles debug set dev cards requests without validating the user and lobby that it came from
     *
     * @param request the {@code SetDevCardsRequest} found on the EventBus
     */
    @Subscribe
    public void onSetDevCardsRequest(SetDevCardsRequest request) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(request.getSessionName());
        gameSession.ifPresent(session -> session.debugSetDevCards(request.getDevCards(), request.getPlayerName()));
    }

    /**
     * Handles the UseCardRequest requests.
     *
     * @param request the {@code UseCardRequest} found on the EventBus
     * @since 2021-06-16
     */
    @Subscribe
    public void onUseCardRequest(UseCardRequest request) {
        Optional<GameSession> optional = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (optional.isPresent()) {
            GameSession gameSession = optional.get();
            try {
                gameSession.userInput(request);
            } catch (GameStateException e) {
                LOG.debug(e);
            }
        }
    }

    @Subscribe
    public void onIsUsingCardAllowedRequest(IsUsingCardAllowedRequest request) {
        Optional<GameSession> optional = gameSessionManagement.getGameSession(request.getGameSessionName());
        if (optional.isPresent()) {
            GameSession gameSession = optional.get();
            try {
                gameSession.userInput(request);
            } catch (GameStateException e) {
                LOG.debug(e);
            }
        }
    }

    /**
     * Prepares a given ServerMessage to be send to a specific player in the game session and
     * posts it on the EventBus
     *
     * @param gameSessionName the name of the gameSession the player is in
     * @param message         the message to be sent to the player
     * @param playerName      the name of the player who will receive the message
     */
    public void sendToPlayerInGameSession(String gameSessionName, ServerMessage message, String playerName, boolean bot) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(gameSessionName);
        if (gameSession.isPresent()) {
            if (bot) {
                gameSession.get().sendMessageToBot(message, playerName);
            } else {
                Set<User> receiver = new HashSet<>();
                Set<User> users = gameSession.get().getLobby().getUsers();
                for (User user : users) {
                    if (user.getUsername().equals(playerName)) {
                        receiver.add(user);
                    }
                }
                if (receiver.isEmpty()) return;
                message.setReceiver(authenticationService.getSessions(receiver));
                post(message);
            }
        }
    }

    /**
     * Prepares a given ResponseMessage to be send to a specific player in the game session and
     * posts it on the EventBus
     *
     * @param gameSessionName the name of the gameSession the player is in
     * @param request         the request that was sent by the player
     * @param response        the response to be sent to the player
     * @param bot             whether the player is a bot
     */
    public void sendResponseToPlayer(String gameSessionName, RequestMessage request, ResponseMessage response, boolean bot) {
        response.initWithMessage(request);
        if (bot) {
            Optional<GameSession> gameSession = gameSessionManagement.getGameSession(gameSessionName);
            gameSession.ifPresent(session -> session.sendMessageToBots(response));
        } else {
            post(response);
        }
    }

    /**
     * Prepares a given ServerMessage to be send to all players in the game session and
     * posts it on the EventBus
     *
     * @param gameSessionName the name of the gameSession the players are in
     * @param message         the message to be sent to the users
     * @see de.uol.swp.common.message.ServerMessage
     */
    public void sendToAllInGameSession(String gameSessionName, ServerMessage message) {
        Optional<GameSession> gameSession = gameSessionManagement.getGameSession(gameSessionName);

        if (gameSession.isPresent()) {
            List<Session> usersAndBots = authenticationService.getSessions(gameSession.get().getUsers());
            usersAndBots.addAll(gameSession.get().getBots().stream().map(BotUser::getSession).collect(Collectors.toSet()));
            message.setReceiver(usersAndBots);
            post(message);
            gameSession.get().sendMessageToBots(message);
        } else {
            LOG.error("The game session {} couldn't be found for ServerMessage {}", gameSessionName, message);
        }
    }

    /**
     * Send a Log Message to the Chatwindow  of a given lobby
     *
     * @param gameSessionName the gameSession
     * @param message         message
     */
    public void sendLogMessage(String gameSessionName, String message) {
        SendSystemMessage msg = new SendSystemMessage(gameSessionName, message);
        post(msg);
    }

    /**
     * Send a VPUpdateMessage for a given player to everyone in the gameSession
     *
     * @param gameSessionName the gameSession
     * @param player          the player whose victory poinss will be updated
     * @param vps             the amount of victory points the player has
     */
    public void sendVPUpdateMessage(String gameSessionName, PlayerDTO player, int vps) {
        VPUpdateMessage message = new VPUpdateMessage(gameSessionName, player, vps);
        sendToAllInGameSession(gameSessionName, message);
    }

    /**
     * Send PlayerPickerMessage to Clients via GameSession
     *
     * @param gameSessionName      current gameSessionName
     * @param playersAdjacentToHex neighbouring players
     * @param playerWhoRolledSeven player who rolled seven
     */
    public void sendPlayerPickerMessage(String gameSessionName, List<PlayerDTO> playersAdjacentToHex, Player playerWhoRolledSeven) {
        var message = new PlayerPickerMessage(gameSessionName, playersAdjacentToHex);
        sendToPlayerInGameSession(gameSessionName, message, playerWhoRolledSeven.getPlayerName(), playerWhoRolledSeven.isBot());
    }


    /**
     * Send Knight Update to Clients via GameSession
     *
     * @param gameSession           current gameSession
     * @param playerWithLargestArmy player with largest Army
     */
    public void sendKnighUpdateMessage(GameSession gameSession, Player playerWithLargestArmy) {
        var message = new KnightUpdateMessage(gameSession.getGameSessionName(), playerWithLargestArmy.createDTO());
        sendToAllInGameSession(gameSession.getGameSessionName(), message);
    }

    /**
     * Send Longest Road Update via Gamesession
     *
     * @param gameSessionName current GameSession
     * @param dto             player with the longest Road
     */
    public void sendLongestRoadUpdateMessage(String gameSessionName, PlayerDTO dto) {
        var message = new LongestRoadUpdateMessage(gameSessionName, dto);
        sendToAllInGameSession(gameSessionName, message);

    }


    /**
     * Sends the DevCardRemainingMessage to all players via gemesesion
     *
     * @param gameSessionName current gameSession
     * @param amountRemaining amount of cards remaining
     */
    public void sendDevCardUpdateMessage(String gameSessionName, int amountRemaining) {
        var message = new DevCardRemainingMessage(gameSessionName, amountRemaining);
        sendToAllInGameSession(gameSessionName, message);
    }
}