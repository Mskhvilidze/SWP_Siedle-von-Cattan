package de.uol.swp.server.game.ai;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.debug.StateMessage;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.message.DiscardNotificationMessage;
import de.uol.swp.common.game.message.PlayerPickerMessage;
import de.uol.swp.common.game.message.RobberPlacingMessage;
import de.uol.swp.common.game.message.build.SetupLocationMessage;
import de.uol.swp.common.game.message.trade.NewTradeOfferMessage;
import de.uol.swp.common.game.message.trade.TradeOfferInterestMessage;
import de.uol.swp.common.game.request.*;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import de.uol.swp.common.game.request.trade.AcceptTradeOfferRequest;
import de.uol.swp.common.game.request.trade.DeclineTradeOfferRequest;
import de.uol.swp.common.game.request.trade.InterestTradeOfferRequest;
import de.uol.swp.common.game.request.trade.StartCounterTradeOffer;
import de.uol.swp.common.game.response.IsUsingCardAllowedResponse;
import de.uol.swp.common.game.response.RoadBuildingCardResponse;
import de.uol.swp.common.message.Message;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.server.communication.UUIDSession;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.board.BoardUtils;
import de.uol.swp.server.game.mapobject.CornerPiece;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.state.DiceState;
import de.uol.swp.server.game.state.PlayState;
import de.uol.swp.server.game.state.SetupState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class represents the "client" of a bot. It should be started after its creation and be stopped with {@link #terminate()}
 * <p>
 * The bot receives message via {@link #sendMessage(Message)} but only handles instances of {@link ResponseMessage} and {@link ServerMessage}
 */
@SuppressWarnings("UnstableApiUsage")
public class BotUser extends Thread {

    private static final Logger LOG = LogManager.getLogger(BotUser.class);
    private static final Random RANDOM = new Random();

    private final Player player;
    private final GameSession gameSession;
    private final EventBus eventBus;
    private final Session session;
    private final BlockingQueue<Message> taskQueue = new LinkedBlockingQueue<>();
    private boolean alive;
    private boolean isCard = false;

    /**
     * Constructs a new bot thread with a unique {@link Session}
     *
     * @param gameSession the game session this bot is created in
     * @param player      the player object that represents this bot
     * @param eventBus    the EvenBus used throughout the server
     */
    public BotUser(GameSession gameSession, Player player, EventBus eventBus) {
        this.gameSession = gameSession;
        this.player = player;
        this.eventBus = eventBus;
        this.session = UUIDSession.create(new UserDTO(player.getPlayerName(), "", ""));
        alive = true;
    }

    @Override
    public void run() {
        while (alive) {
            Message message = null;
            boolean endTurn = false;
            try {
                message = taskQueue.take();
            } catch (InterruptedException exception) {
                interrupt();
            }
            if (message instanceof SetupLocationMessage) {
                waitSomeTime(1, TimeUnit.SECONDS);
                if (gameSession.getCurrentState() == SetupState.INSTANCE) {
                    SetupLocationMessage response = (SetupLocationMessage) message;
                    Coord[] coords = response.getLegalNodes().toArray(Coord[]::new);//Works because same instance on server

                    Coord result;
                    if (response.getPieceType() == PieceType.SETTLEMENT) {
                        List<Coord> possible = Arrays.stream(coords).filter(coord -> {
                            List<Coord> hexagons = Arrays.asList(BoardUtils.getCornerHexagonsWithoutWater(gameSession.getBoard(), coord));
                            return hexagons.size() == 3 && hexagons.stream().noneMatch(
                                    hexCoord -> gameSession.getBoard().getRobber().getCoord().equals(hexCoord));
                        }).collect(Collectors.toList());
                        if (possible.isEmpty()) {
                            result = coords[RANDOM.nextInt(coords.length)];
                        } else {
                            result = possible.get(RANDOM.nextInt(possible.size()));
                        }
                    } else {
                        result = coords[RANDOM.nextInt(coords.length)];
                    }
                    post(new PlaceObjectRequest(gameSession.getGameSessionName(), response.getPieceType(), result));
                }
            } else if (message instanceof RoadBuildingCardResponse) {
                for (int i = 1; i <= ((RoadBuildingCardResponse) message).getNumOfFreeRoads(); i++) {
                    waitSomeTime(2, TimeUnit.SECONDS);
                    post(new PlaceObjectRequest(gameSession.getGameSessionName(), PieceType.ROAD, player.getRandomLegalRoad()));
                }
                endTurn = true;
            } else if (message instanceof DiscardNotificationMessage) {
                waitSomeTime(1, TimeUnit.SECONDS);
                int amount = ((DiscardNotificationMessage) message).getAmount();
                post(new DiscardCardsRequest(gameSession.getGameSessionName(), player.getRandomResources(amount)));
            } else if (message instanceof RobberPlacingMessage) {
                waitSomeTime(2, TimeUnit.SECONDS);
                Map<Coord, Set<CornerPiece>> possibleHexagons = new HashMap<>();
                Board board = gameSession.getBoard();
                Set<Coord> hexagonCoords = board.getAllHexagonCoords();
                for (Coord hexagonCoord : hexagonCoords) {
                    Set<CornerPiece> pieces = BoardUtils.getCornerPiecesAdjacentToHex(board, hexagonCoord);
                    if (pieces.stream().noneMatch(cornerPiece -> cornerPiece.getPlayer().equals(player))) {
                        possibleHexagons.put(hexagonCoord, pieces);
                    }
                }
                possibleHexagons.remove(board.getRobber().getCoord());
                var map = possibleHexagons.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> calcValue(entry.getValue())));
                var opt = map.entrySet().stream().max(Map.Entry.comparingByValue());
                Coord robberPos;
                if (opt.isPresent()) {
                    robberPos = opt.get().getKey();
                } else {
                    robberPos = board.getFreeRobberPos();
                }
                post(new RobberPlacingRequest(gameSession.getGameSessionName(), robberPos));
                if (isCard) {
                    endTurn = true;
                    isCard = false;
                }
            } else if (message instanceof PlayerPickerMessage) {
                waitSomeTime(1, TimeUnit.SECONDS);
                var list = ((PlayerPickerMessage) message).getPlayersGettingRobbed();
                var playerToPick = list.stream().max(
                        Comparator.comparingInt(playerDTO -> gameSession.getPlayer(playerDTO.getPlayerId()).getInventory().getNumOfResourceCards()));
                playerToPick.ifPresent(playerDTO -> post(new PlayerPickerRequest(gameSession.getGameSessionName(), playerDTO)));
            } else if (message instanceof StateMessage) {
                if (gameSession.getWhoseTurn().getPlayerId() == player.getPlayerId()) {
                    if (gameSession.getCurrentState() == PlayState.INSTANCE) {
                        tryToBuyAndPlace();
                        List<DevCard> devCards = new ArrayList<>();
                        for (Map.Entry<DevCard, Integer> entry : player.getInventory().getPlayableCards().entrySet()) {
                            if (entry.getValue() > 0) {
                                devCards.add(entry.getKey());
                            }
                        }
                        if (!devCards.isEmpty() && RANDOM.nextInt(10) > 6) {
                            post(new IsUsingCardAllowedRequest(gameSession.getGameSessionName(), devCards.get(RANDOM.nextInt(devCards.size()))));
                        } else {
                            endTurn = true;
                        }
                    } else if (gameSession.getCurrentState() == DiceState.INSTANCE) {
                        waitSomeTime(2, TimeUnit.SECONDS);
                        post(new RollDiceRequest(gameSession.getGameSessionName()));
                    }
                }
            } else if (message instanceof IsUsingCardAllowedResponse) {
                waitSomeTime(3, TimeUnit.SECONDS);
                endTurn = useDevCard(((IsUsingCardAllowedResponse) message).getDevCard());
            } else if (message instanceof NewTradeOfferMessage) {
                trade(message);
            } else if (message instanceof TradeOfferInterestMessage) {
                TradeOffer tradeOffer = ((TradeOfferInterestMessage) message).getTradeOffer();
                //Nicht warten damit andere Spieler nicht Interesse zeigen können
                post(new AcceptTradeOfferRequest(gameSession.getGameSessionName(), tradeOffer, tradeOffer.getReceivingPlayer()));
                LOG.debug(gameSession.getGameSessionName() + ": " + player.getPlayerName() + " accepted the trade offer.");
            }
            if (endTurn) {
                waitSomeTime(2, TimeUnit.SECONDS);
                post(new TurnEndRequest(gameSession.getGameSessionName()));
            }
        }
    }

    private void tryToBuyAndPlace() {
        int numberOfAttempts = 0;   //if something goes wrong, this int makes sure, the bot won't be in an endless loop
        boolean canBuy = true;
        while (canBuy && numberOfAttempts < 15) {
            canBuy = false;
            //Buy Priority
            if (player.hasEnoughResources(PieceType.CITY) && player.getRandomLegalCity() != null) {
                post(new PlaceObjectRequest(gameSession.getGameSessionName(), PieceType.CITY, player.getRandomLegalCity()));
                canBuy = true;
                waitSomeTime(1, TimeUnit.SECONDS);
            }
            if (player.hasEnoughResources(PieceType.SETTLEMENT) && player.getRandomLegalSettlement() != null) {
                post(new PlaceObjectRequest(gameSession.getGameSessionName(), PieceType.SETTLEMENT,
                        player.getRandomLegalSettlement()));
                canBuy = true;
                waitSomeTime(1, TimeUnit.SECONDS);
            }
            if (player.hasResources(DevCard.getCost())) {
                post(new BuyDevCardRequest(gameSession.getGameSessionName()));
                canBuy = true;
                waitSomeTime(1, TimeUnit.SECONDS);
            }
            if (player.hasEnoughResources(PieceType.ROAD) && player.getRandomLegalRoad() != null) {
                post(new PlaceObjectRequest(gameSession.getGameSessionName(), PieceType.ROAD, player.getRandomLegalRoad()));
                canBuy = true;
                waitSomeTime(1, TimeUnit.SECONDS);
            }
            numberOfAttempts--;
        }
    }

    private boolean useDevCard(DevCard devCard) {
        boolean endTurn = false;
        switch (devCard) {
            case VP: {
                // cant be played
                endTurn = true;
                break;
            }
            case KNIGHT: {
                isCard = true;
                post(new UseCardRequest(gameSession.getGameSessionName(), DevCard.KNIGHT, null));
                break;
            }
            case MONOPOLY: {
                ResourceEnumMap resources = new ResourceEnumMap();
                resources.put(ResourceType.getRandom(), 1);
                post(new UseCardRequest(gameSession.getGameSessionName(), DevCard.MONOPOLY, resources));
                endTurn = true;
                break;
            }
            case ROAD_BUILDING: {
                post(new UseCardRequest(gameSession.getGameSessionName(), DevCard.ROAD_BUILDING, null));
                break;
            }
            case YEAR_OF_PLENTY: {
                ResourceEnumMap resources = new ResourceEnumMap();
                resources.put(ResourceType.getRandom(), 1);
                ResourceType resource = ResourceType.getRandom();
                resources.put(resource, resources.getOrDefault(resource, 0) + 1);
                post(new UseCardRequest(gameSession.getGameSessionName(), DevCard.YEAR_OF_PLENTY, resources));
                endTurn = true;
                break;
            }
            default:
                LOG.error("No Card selected");
        }
        return endTurn;
    }

    private long calcValue(Set<CornerPiece> cornerPieces) {
        return cornerPieces.stream().mapToInt(cornerPiece -> {
            if (cornerPiece.getPieceType() == PieceType.SETTLEMENT) {
                return 1;
            } else if (cornerPiece.getPieceType() == PieceType.CITY) {
                return 2;
            }
            return 0;
        }).sum();
    }

    private void waitSomeTime(long time, TimeUnit timeUnit) {
        long millis = timeUnit.toMillis(time);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            interrupt();
        }
    }

    private void post(RequestMessage request) {
        request.setSession(session);
        eventBus.post(request);
    }

    /**
     * This method handles how a bot should react, if it reserves a trade offer
     * The Bot only accepts a trade, when it wantsToTrade, it has enough resources and the amount he would get from the trade is at least the amount he has to give.
     *
     * @param message a InterestTradeOfferRequest, if the bot wants to trade or a DeclineTradeOfferRequest if the bot declines the trade
     */
    private void trade(Message message) {
        TradeOffer tradeOffer = ((NewTradeOfferMessage) message).getTradeOffer();
        waitSomeTime(3, TimeUnit.SECONDS);
        boolean wantToTrade = wantToTrade(tradeOffer);
        if (player.hasResources(tradeOffer.getWant()) && wantToTrade) {
            post(new InterestTradeOfferRequest(gameSession.getGameSessionName(), tradeOffer));
            LOG.debug("{}: {} is interested in a trade offer from {}", gameSession.getGameSessionName(), player.getPlayerName(),
                    tradeOffer.getOfferingPlayer());
        } else if (!player.hasResources(tradeOffer.getWant()) && wantToTrade) {
            post(new StartCounterTradeOffer(gameSession.getGameSessionName(), tradeOffer, counterTrade(player, tradeOffer)));
            LOG.debug("{}: {} creates a counter offer.", gameSession.getGameSessionName(), player.getPlayerName());
            post(new DeclineTradeOfferRequest(gameSession.getGameSessionName(), tradeOffer));
        } else {
            post(new DeclineTradeOfferRequest(gameSession.getGameSessionName(), tradeOffer));
            LOG.debug("{}: {} is not interested in a trade offer from {}", gameSession.getGameSessionName(), player, tradeOffer.getOfferingPlayer());
        }
    }

    /**
     * This method gives lets the bot 'decide' if it wants to trade
     *
     * @return true, if the chance is given
     */
    private boolean wantToTrade(TradeOffer tradeOffer) {
        if (tradeOffer.getOffer().sumOfResources() < tradeOffer.getWant().sumOfResources()) {
            return false;
        }
        int max = 100;
        int min = 0;
        int chance = 60; // chance the bot wants to trade in percentage
        int rand = ThreadLocalRandom.current().nextInt(min, max);
        return rand <= chance;
    }

    /**
     * This method creates a counter trade
     *
     * @return trade offer for a counter trade
     */
    private TradeOffer counterTrade(Player player, TradeOffer oldTradeOffer) {
        int wantedAmount = RANDOM.nextInt(oldTradeOffer.getWant().sumOfResources()) + 1;

        // trade want
        ResourceEnumMap want = new ResourceEnumMap();
        want.put(ResourceType.getRandom(), wantedAmount);

        // trade offer
        ResourceEnumMap offer = player.getRandomResources(wantedAmount);
        if (offer.sumOfResources() == 0) {
            offer = player.getRandomResources(1);
        }

        return new TradeOffer(offer, want);
    }

    /**
     * Adds the given {@code Message} to this bots task queue if it is named as a receiver
     *
     * @param message the {@code Message} that is sent to the bot
     */
    public void sendMessage(Message message) {
        if (message instanceof ResponseMessage) {
            message.getSession().ifPresent(msgSession -> {
                if (msgSession.equals(session)) {
                    taskQueue.add(message);
                }
            });
        } else if (message instanceof ServerMessage) {
            ServerMessage serverMessage = (ServerMessage) message;
            if (serverMessage.getReceiver().isEmpty() || serverMessage.getReceiver().contains(session)) {
                taskQueue.add(message);
            }
        }
    }

    /**
     * Terminates this bot
     *
     * @implNote it will only be terminated at the start of its next task cycle or if its currently waiting for a task
     */
    public void terminate() {
        alive = false;
        interrupt();
    }

    /**
     * Returns the Player object that represents this bot
     *
     * @return the Player object that represents this bot
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the {@code Session} object of this bot
     *
     * @return the {@code Session} object of this bot
     * @see Session
     */
    public Session getSession() {
        return session;
    }
}
