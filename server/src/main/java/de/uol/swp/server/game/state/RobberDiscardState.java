package de.uol.swp.server.game.state;

import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.request.DiscardCardsRequest;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.InvalidGameStateException;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.game.InventoryService;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.session.GameSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is the Robber Discard State, on a 7 the DiceState calls this state as the next. All players
 * have to discard resources from their inventories. Then the RobberPlacing state is called
 */
public enum RobberDiscardState implements GameState {

    INSTANCE;

    private static final Logger LOG = LogManager.getLogger(RobberDiscardState.class);

    @Override
    public void next(GameSession gameSession, StateContext context) {
        gameSession.sendLogMessage("Räuber kann platziert werden.");
        gameSession.setCurrentState(RobberPlacingState.INSTANCE);
    }

    @Override
    public void action(GameSession gameSession, StateContext context) throws GameStateException {
        if (!(context.getLastRequest() instanceof DiscardCardsRequest)) {
            throw new InvalidGameStateException();
        }
        if (context.getLastRequest() instanceof DiscardCardsRequest) {
            DiscardCardsRequest request = (DiscardCardsRequest) context.getLastRequest();
            Player player = gameSession.getPlayer(request.getUserNameFromSender());
            try {
                processDiscard(gameSession, context, player, request.getToDiscard());
            } catch (OverDrawException e) {
                gameSession.sendDiscardResponseToPlayer(request, false, "Misserfolg! Sie besitzen nicht genügend Rohstoffe.", player.isBot());
            }
            gameSession.sendDiscardResponseToPlayer(request, true, "Erfolg!", player.isBot());
            boolean allPlayersDiscard = context.getDiscardedBefore().values().stream().noneMatch(Boolean.FALSE::equals);

            if (allPlayersDiscard) {
                next(gameSession, context);
            }
        }

    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        context.initDiscardMap(gameSession.getPlayers());
        for (Map.Entry<Player, Boolean> entry : context.getDiscardedBefore().entrySet()) {
            Player player = entry.getKey();
            if (Boolean.FALSE.equals(entry.getValue())) {
                ResourceEnumMap resourcesToDiscard = player.getRandomResources(player.getNumOfResourcesToDiscardFromInventory());
                try {
                    processDiscard(gameSession, context, player, resourcesToDiscard);
                } catch (OverDrawException e) {
                    LOG.error("Player {} with {} should have enough resources to discard {} at turn end", player,
                            player.getInventory().getResources(), resourcesToDiscard);
                }
            }
        }
        gameSession.sendDiscardResponseToAll(true, "Rohstoffe wurden für Sie abgelegt");
        next(gameSession, context);
        gameSession.startNewFullTimer();
    }

    @Override
    public int getTimer() {
        return 20;
    }

    @Override
    public void beginState(GameSession gameSession, StateContext context) {
        gameSession.startNewSubTimer(getTimer(), TimeUnit.SECONDS);
        for (Player player : gameSession.getPlayers()) {
            int amount = player.getNumOfResourcesToDiscardFromInventory();
            if (amount > 0) {
                gameSession.sendDiscardNotificationToPlayer(player, amount);
            }
        }
        context.initDiscardMap(gameSession.getPlayers());
    }

    /**
     * Discards the given resources from the given player and updates {@link StateContext#getDiscardedBefore()} ()}
     *
     * @param gameSession the gameSession the robber was moved in
     * @param context     the state context of the game session
     * @param player      the player who discards the resources
     * @param resources   the resources that the player wants to discard
     * @throws OverDrawException if the player does not have the resources to discard
     */
    private void processDiscard(GameSession gameSession, StateContext context, Player player, ResourceEnumMap resources) throws OverDrawException {
        InventoryService inventoryService = gameSession.getInventoryService();
        inventoryService.removeResources(player, resources);
        gameSession.sendLogMessage(player.getPlayerName() + " hat " + resources.toChatFormat() + " abgeworfen.");
        context.getDiscardedBefore().put(player, true);
    }
}
