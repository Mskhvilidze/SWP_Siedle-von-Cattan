package de.uol.swp.server.game.state;

import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.message.CardUsedMessage;
import de.uol.swp.common.game.request.BuyDevCardRequest;
import de.uol.swp.common.game.request.IsUsingCardAllowedRequest;
import de.uol.swp.common.game.request.UseCardRequest;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import de.uol.swp.common.game.request.build.StartBuildRequest;
import de.uol.swp.common.game.request.trade.StartBankTradeRequest;
import de.uol.swp.common.game.request.trade.StartTradeRequest;
import de.uol.swp.common.game.response.IsUsingCardAllowedResponse;
import de.uol.swp.common.game.response.RoadBuildingCardResponse;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.exception.PlayStateException;
import de.uol.swp.server.game.InventoryService;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.session.GameSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public enum PlayState implements GameState {

    INSTANCE;
    private static final Logger LOG = LogManager.getLogger(PlayState.class);

    private void next(GameSession gameSession, GameState gameState) {
        gameSession.setCurrentState(gameState);
    }

    @Override
    public void next(GameSession gameSession, StateContext context) {
        gameSession.setCurrentState(EndState.INSTANCE);
    }

    @Override
    public void action(GameSession gameSession, StateContext context) throws GameStateException {
        InventoryService inventoryService = gameSession.getInventoryService();
        RequestMessage request = context.getLastRequest();
        if (request instanceof StartTradeRequest || request instanceof StartBankTradeRequest) {
            TradeState tradeState = TradeState.INSTANCE;
            next(gameSession, tradeState);
            tradeState.action(gameSession, context);
        } else if (request instanceof StartBuildRequest || request instanceof PlaceObjectRequest) {
            BuildState buildState = BuildState.INSTANCE;
            next(gameSession, buildState);
            try {
                buildState.action(gameSession, context);
            } catch (GameStateException exception) {
                next(gameSession, PlayState.INSTANCE);
            }
        } else if (request instanceof BuyDevCardRequest) {

            Player sender = gameSession.getPlayer(request.getUserNameFromSender());
            ResourceEnumMap cardCost = DevCard.getCost();
            if (!sender.hasResources(cardCost)) {
                return;
            }
            if (!gameSession.getWhoseTurn().equals(sender)) {
                return;
            }
            if (gameSession.getBankInventory().getAmountRemaining() <= 0) {
                return;
            }
            DevCard devCard = gameSession.getBankInventory().getAndRemoveRandomDevCard();
            gameSession.getGameSessionService().sendDevCardUpdateMessage(gameSession.getGameSessionName(), gameSession.getDevCardsRemaining());
            if (devCard == null) {
                throw new PlayStateException(gameSession.getGameSessionName() + ": " + sender + " tried to buy a dev card but none are remaining");
            }
            try {
                inventoryService.removeResources(sender, cardCost);
                inventoryService.addDevCard(sender, devCard);
            } catch (OverDrawException exception) {
                throw new PlayStateException();
            }
            if (devCard == DevCard.VP) {
                gameSession.checkVictory();
            }
            gameSession.sendLogMessage(sender.getPlayerName() + " hat eine Entwicklungskarte gekauft");

        } else if (request instanceof IsUsingCardAllowedRequest) {
            Player player = gameSession.getPlayer(request.getUserNameFromSender());
            if (!player.equals(gameSession.getWhoseTurn())) {
                return;
            }
            DevCard devCard = ((IsUsingCardAllowedRequest) request).getDevCard();
            IsUsingCardAllowedResponse response;
            if (player.getInventory().getDevCards().get(devCard) == 0) {
                return;
            } else if (player.getInventory().getPlayableCards().get(devCard) == 0) {
                response = new IsUsingCardAllowedResponse(gameSession.getGameSessionName(), false, null,
                        "Sie können keine Entwicklungskarte spielen die sie in dieser Runde gekauft haben");
            } else if (player.hasPlayedDevCardThisTurn()) {
                response = new IsUsingCardAllowedResponse(gameSession.getGameSessionName(), false, null,
                        "Sie können nicht zwei Entwicklungskarten in einer Runde spielen");
            } else {
                response = new IsUsingCardAllowedResponse(gameSession.getGameSessionName(), true, ((IsUsingCardAllowedRequest) request).getDevCard(),
                        null);
            }
            gameSession.getGameSessionService().sendResponseToPlayer(gameSession.getGameSessionName(), request, response, player.isBot());

        } else if (request instanceof UseCardRequest) {
            DevCard devCard = ((UseCardRequest) request).getDevCard();
            ResourceEnumMap resources = ((UseCardRequest) request).getResources();
            Player player = gameSession.getPlayer(request.getUserNameFromSender());
            player.setPlayedDevCardThisTurn(true);
            switch (devCard) {
                case VP:
                    // Can not be played.
                    break;
                case KNIGHT:
                    player.incrNumOfKnights();
                    awardKnightBonusToPlayer(gameSession, context);
                    gameSession.checkVictory();
                    gameSession.sendLogMessage(player.getPlayerName() + " benutzt Ritter und muss den Räuber platzieren.");
                    inventoryService.removeDevCard(player, DevCard.KNIGHT);
                    next(gameSession, RobberPlacingState.INSTANCE);
                    gameSession.sendStartRobber(gameSession.getWhoseTurn());
                    gameSession.getGameSessionService().sendToAllInGameSession(gameSession.getGameSessionName(),
                            new CardUsedMessage(gameSession.getGameSessionName(), player.createDTO(), DevCard.KNIGHT));
                    break;
                case YEAR_OF_PLENTY:
                    int countResourcesPlayerWants = resources.values().stream().reduce(0, Integer::sum);
                    if (countResourcesPlayerWants != 2) {
                        throw new PlayStateException("Player has not selected 2 resources for year of plenty.");
                    }
                    gameSession.sendLogMessage(player.getPlayerName() + " benutzt Erfindung und erhält " + resources.toChatFormat() + ".");
                    inventoryService.addResources(player, resources);
                    inventoryService.removeDevCard(player, DevCard.YEAR_OF_PLENTY);
                    gameSession.getGameSessionService().sendToAllInGameSession(gameSession.getGameSessionName(),
                            new CardUsedMessage(gameSession.getGameSessionName(), player.createDTO(), DevCard.YEAR_OF_PLENTY));
                    break;
                case MONOPOLY:
                    ResourceType resourceType;
                    var result = resources.entrySet().stream().filter(entry -> entry.getValue() == 1).findFirst();
                    if (result.isPresent()) {
                        resourceType = result.get().getKey();
                    } else {
                        throw new PlayStateException("Player has not selected 1 resource for monopoly.");
                    }
                    int countResourceOtherPlayersHave = 0;
                    for (Player gameSessionPlayer : gameSession.getPlayers()) {
                        if (player.equals(gameSessionPlayer)) {
                            continue;
                        }
                        int countResourceThisPlayerHas = gameSessionPlayer.getAmountOfResources(resourceType);
                        try {
                            inventoryService.decreaseResource(gameSessionPlayer, resourceType, countResourceThisPlayerHas);
                        } catch (OverDrawException exception) {
                            LOG.error("{} not enough resources for monopoly", gameSessionPlayer);
                            countResourceThisPlayerHas = 0;
                        }
                        countResourceOtherPlayersHave += countResourceThisPlayerHas;
                    }
                    ResourceEnumMap resourcesFromOtherPlayers = new ResourceEnumMap();
                    resourcesFromOtherPlayers.put(resourceType, countResourceOtherPlayersHave);
                    gameSession.sendLogMessage(
                            player.getPlayerName() + " benutzt Monopol und erhält " + resourcesFromOtherPlayers.toChatFormat() + ".");
                    inventoryService.addResources(player, resourcesFromOtherPlayers);
                    inventoryService.removeDevCard(player, DevCard.MONOPOLY);
                    gameSession.getGameSessionService().sendToAllInGameSession(gameSession.getGameSessionName(),
                            new CardUsedMessage(gameSession.getGameSessionName(), player.createDTO(), DevCard.MONOPOLY));
                    break;
                case ROAD_BUILDING:
                    int num = Math.min(player.getInventory().getNumOfAvailableRoads(), 2);
                    if (num > 0) {
                        context.setFreeBuildableRoads(num);
                        gameSession.getGameSessionService().sendResponseToPlayer(gameSession.getGameSessionName(), request,
                                new RoadBuildingCardResponse(gameSession.getGameSessionName(), num), player.isBot());
                        next(gameSession, BuildState.INSTANCE);
                    }
                    gameSession.sendLogMessage(player.getPlayerName() + " benutzt Straßenbau und muss zwei Straßen platzieren.");
                    inventoryService.removeDevCard(player, DevCard.ROAD_BUILDING);
                    gameSession.getGameSessionService().sendToAllInGameSession(gameSession.getGameSessionName(),
                            new CardUsedMessage(gameSession.getGameSessionName(), player.createDTO(), DevCard.ROAD_BUILDING));
                    break;
                default:
                    throw new PlayStateException("Enum is not accounted for.");
            }

        }
    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        GameState.super.endTurn(gameSession, context);
    }

    /**
     * Awards the knight bonus to the player with the most knights played
     *
     * @param gameSession current game session
     * @param context     current state context
     * @return the Player with the largest army or {@code null} if no player has played 3 or more cards and null if no new player has earned the reward.
     */
    private void awardKnightBonusToPlayer(GameSession gameSession, StateContext context) {
        Player largestArmy = context.getPlayerWithLargestArmy();
        int largestArmySize = (largestArmy == null) ? 2 : largestArmy.getNumOfKnights();
        for (Player player : gameSession.getPlayers()) {
            if (player.getNumOfKnights() > largestArmySize) {
                context.setPlayerWithLargestArmy(player);
                gameSession.sendLogMessage(player.getPlayerName() + " verfügt nun über die größte Rittermacht");
                gameSession.getGameSessionService().sendKnighUpdateMessage(gameSession, player);
                gameSession.sendVPUpdateMessage(player);
                if (largestArmy != null) {
                    gameSession.sendVPUpdateMessage(largestArmy);
                }
            }
        }
    }
}