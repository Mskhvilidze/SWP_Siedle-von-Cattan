package de.uol.swp.server.game.state;

import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.request.IsUsingCardAllowedRequest;
import de.uol.swp.common.game.request.RollDiceRequest;
import de.uol.swp.common.game.request.UseCardRequest;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.InvalidGameStateException;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.board.BoardUtils;
import de.uol.swp.server.game.mapobject.CornerPiece;
import de.uol.swp.server.game.session.GameSession;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This state is called after the EndState and is the first move of every Player
 */
public enum DiceState implements GameState {

    INSTANCE;

    private static final Random RANDOM = new Random();

    @Override
    public void next(GameSession gameSession, StateContext context) {
        gameSession.setCurrentState(PlayState.INSTANCE);
    }

    @Override
    public void action(GameSession gameSession, StateContext context) throws GameStateException {
        if (context.getLastRequest() instanceof RollDiceRequest) {
            RollDiceRequest request = (RollDiceRequest) context.getLastRequest();
            if (!gameSession.getWhoseTurn().getPlayerName().equals(request.getUserNameFromSender())) {
                return;
            }
            int diceResult = context.getNextDiceResult();
            if (diceResult == -1) {
                diceResult = RANDOM.nextInt(6) + 1 + RANDOM.nextInt(6) + 1;
            }
            processDiceResult(gameSession, context, diceResult);
        } else if (context.getLastRequest() instanceof IsUsingCardAllowedRequest) {
            if (!gameSession.getWhoseTurn().getPlayerName().equals(context.getLastRequest().getUserNameFromSender())) {
                return;
            }
            PlayState.INSTANCE.action(gameSession, context);
        } else if (context.getLastRequest() instanceof UseCardRequest) {
            if (!gameSession.getWhoseTurn().getPlayerName().equals(context.getLastRequest().getUserNameFromSender())) {
                return;
            }
            gameSession.setPlayedDevCardInDiceState(true);
            context.setDevCardPlayedBeforeDice(true);
            PlayState.INSTANCE.action(gameSession, context);
        } else {
            throw new InvalidGameStateException();
        }
    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        processDiceResult(gameSession, context, RANDOM.nextInt(6) + 1 + RANDOM.nextInt(6) + 1);
    }

    @Override
    public int getTimer() {
        return 20;
    }

    @Override
    public void beginState(GameSession gameSession, StateContext context) {
        if (!context.isDevCardPlayedBeforeDice()) {
            gameSession.startNewSubTimer(getTimer(), TimeUnit.SECONDS);
        }
    }

    private void next(GameSession gameSession, GameState gameState) {
        gameSession.setCurrentState(gameState);
    }

    /**
     * Distributes the resources to players or moves to the robber states if a seven was rolled
     *
     * @param gameSession the gameSession the dice was rolled in
     * @param context     the state context of the game session
     * @param diceResult  the result of the dice throw
     */
    private void processDiceResult(GameSession gameSession, StateContext context, int diceResult) {
        if (diceResult != 7) {
            distributeResources(gameSession, diceResult);
            next(gameSession, context);
            gameSession.startNewFullTimer();
        } else {
            boolean discard = Arrays.stream(gameSession.getPlayers()).anyMatch(player -> player.getNumOfResourcesToDiscardFromInventory() > 0);
            if (discard) {
                next(gameSession, RobberDiscardState.INSTANCE);
            } else {
                gameSession.startNewFullTimer();
                next(gameSession, RobberPlacingState.INSTANCE);
            }
        }
        gameSession.sendDiceResultToAllPlayers(gameSession.getWhoseTurn().getPlayerName(), diceResult);
        gameSession.sendLogMessage(gameSession.getWhoseTurn().getPlayerName() + " hat eine " + diceResult + " gewürfelt!");
    }

    private void distributeResources(GameSession gameSession, int diceResult) {
        Board board = gameSession.getBoard();
        Collection<Coord> hexagonCoords = board.getHexagonNumbers().get(diceResult);
        Map<Player, ResourceEnumMap> playerResources = new HashMap<>();
        for (Coord coord : hexagonCoords) {
            if (coord.equals(board.getRobber().getCoord())) {
                continue;
            }
            ResourceType resource = ResourceTile.toResourceType(board.getResourceTileFromCoord(coord));
            Set<CornerPiece> pieces = BoardUtils.getCornerPiecesAdjacentToHex(board, coord);
            for (CornerPiece piece : pieces) {
                playerResources.putIfAbsent(piece.getPlayer(), new ResourceEnumMap());
                int amount = (piece.getPieceType() == PieceType.SETTLEMENT) ? 1 : 2;
                playerResources.get(piece.getPlayer()).computeIfPresent(resource, (resourceType, integer) -> integer += amount);
            }
        }
        for (Map.Entry<Player, ResourceEnumMap> entry : playerResources.entrySet()) {
            gameSession.getInventoryService().increaseResources(entry.getKey(), entry.getValue());
        }
    }
}
