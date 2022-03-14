package de.uol.swp.server.game.state;

import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.message.build.CancelBuildMessage;
import de.uol.swp.common.game.message.build.ObjectWasPlacedMessage;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.InvalidGameStateException;
import de.uol.swp.server.exception.SetupException;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.board.BoardUtils;
import de.uol.swp.server.game.mapobject.BuildablePiece;
import de.uol.swp.server.game.mapobject.RoadPiece;
import de.uol.swp.server.game.mapobject.SettlementPiece;
import de.uol.swp.server.game.session.GameSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * This is the setup phase of the game.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public enum SetupState implements GameState {

    INSTANCE;

    private static final Random RANDOM = new Random();
    private static final int MAX_AMOUNT_OF_PLACEMENTS = 4;
    private static final Logger LOG = LogManager.getLogger(SetupState.class);

    @Override
    public void next(GameSession gameSession, StateContext context) {
        gameSession.setCurrentState(DiceState.INSTANCE);
    }

    @Override
    public void action(GameSession gameSession, StateContext context) throws GameStateException {

        // in setup state a player will have to send two objectPlacementRequests, one for a city and one for an
        // adjacent road. Each player has to do this twice. 4 requests per player

        if (context.getLastRequest() instanceof PlaceObjectRequest) {
            var setupInfos = context.getSetupInfos();
            PlaceObjectRequest request = (PlaceObjectRequest) context.getLastRequest();
            Player player = gameSession.getPlayer(request.getUserNameFromSender());

            if (setupInfos.get(player).isDone(MAX_AMOUNT_OF_PLACEMENTS)) {
                throw new SetupException("Player should not place more than 4 objects in setup phase.");
            }
            if (!gameSession.getWhoseTurn().equals(player)) {
                throw new SetupException("It is not " + player.getPlayerName() + " turn.");
            }

            processSetup(gameSession, context, player, request.getObjectToPlace(), request.getCoord());

            // if two placements were made the turn can be given to the next player
            if (setupInfos.get(player).nextPiece() == PieceType.SETTLEMENT) {
                gameSession.endTurn(gameSession.getWhoseTurn());
            }
        } else {
            throw new InvalidGameStateException();
        }
    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        var setupInfos = context.getSetupInfos();
        Player player = gameSession.getWhoseTurn();
        var setupInfo = setupInfos.get(player);
        while (setupInfo.isBehind(context)) {
            if (setupInfo.nextPiece() == PieceType.SETTLEMENT) {
                Coord randomSettlement = gameSession.getBoard().getFreeSettlementPos();
                try {
                    processSetup(gameSession, context, player, PieceType.SETTLEMENT, randomSettlement);
                } catch (SetupException e) {
                    LOG.error("Automatic setup end failed for Settlement: {}", e.getMessage());
                }
            } else if (setupInfo.nextPiece() == PieceType.ROAD) {
                var roads = setupInfo.getRoads(gameSession.getBoard());
                Coord randomRoad = roads.get(RANDOM.nextInt(roads.size()));
                try {
                    processSetup(gameSession, context, player, PieceType.ROAD, randomRoad);
                } catch (SetupException e) {
                    LOG.error("Automatic setup end failed for Road: {}", e.getMessage());
                }
            }
        }
        boolean noneBehind = context.getSetupInfos().values().stream().noneMatch(info -> info.isBehind(context));
        if (noneBehind) {
            context.setSetupPhase(context.getSetupPhase() + 1);
            if (context.getSetupPhase() == 1) gameSession.reverseTurnQueueNextAdvance();
        }
        CancelBuildMessage message = new CancelBuildMessage(gameSession.getGameSessionName());
        gameSession.getGameSessionService().sendToPlayerInGameSession(gameSession.getGameSessionName(), message, player.getPlayerName(),
                player.isBot());
        if (context.isSetupPhaseCompleted()) {
            gameSession.sendLogMessage("Die Anfangsphase wurde abgeschlossen.");
            context.getSetupInfos().clear();
            next(gameSession, context);
            initLegalRoadsForAllPlayers(gameSession);
        } else {
            GameState.super.endTurn(gameSession, context);
        }
    }

    @Override
    public void beginState(GameSession gameSession, StateContext context) {
        gameSession.sendSetupLocation(gameSession.getWhoseTurn(), PieceType.SETTLEMENT);
    }

    /**
     * Places the given piece at the given position, updates the SetupInfo and the legal nodes for the player
     * <p>
     * Also sends an ObjectWasPlacedMessage to all players
     *
     * @param gameSession the gameSession the piece was placed in
     * @param setupInfo   the setup info for the given player
     * @param piece       the piece that should be placed
     * @param coord       the position of the piece
     */
    private void placePiece(GameSession gameSession, StateContext.SetupInfo setupInfo, BuildablePiece piece, Coord coord) throws SetupException {
        gameSession.getBoard().addPiece(coord, piece);
        setupInfo.addPiece(piece.getPieceType(), coord);
        var message = new ObjectWasPlacedMessage(gameSession.getGameSessionName(), piece.getPlayer().createDTO(), piece.getPieceType(), coord);
        gameSession.getGameSessionService().sendToAllInGameSession(gameSession.getGameSessionName(), message);
        updateLegalSetupMovesForEveryPlayer(gameSession, coord, piece);
        gameSession.getInventoryService().decreaseObjects(piece.getPlayer(), piece.getPieceType());
    }

    /**
     * Places the given piece at the given position if the setup conditions are met {@link #checkBoardRuleForSetup}
     * <p>
     * If this is the second settlement the player places resources will be distributed
     *
     * @param gameSession the gameSession the piece was placed in
     * @param context     the state context of the game session
     * @param player      the player who placed the piece
     * @param piece       the piece that should be placed
     * @param coord       the position of the piece
     * @throws SetupException if the player cannot place that piece
     */
    private void processSetup(GameSession gameSession, StateContext context, Player player, PieceType piece, Coord coord) throws SetupException {
        //   0 and 2 are settlements
        //   1 and 3 are roads
        //   increment inside here to final value 4
        var setupInfo = context.getSetupInfos().get(player);
        if (setupInfo.nextPiece() == PieceType.SETTLEMENT) {
            if (piece != PieceType.SETTLEMENT) {
                throw new SetupException("Place Settlement instead");
            }
            if (!checkBoardRuleForSetup(gameSession, player, coord, piece, setupInfo)) {
                gameSession.sendLogMessage("Platzieren Sie ein Dorf");
                throw new SetupException("Invalid Piece");
            }
            if (setupInfo.isSecondSettlement()) {
                distributeResources(gameSession, player, coord);
            }
            placePiece(gameSession, setupInfo, new SettlementPiece(player), coord);

            player.updateBuildingVictoryPoints(1);
            gameSession.sendVPUpdateMessage(player);
            gameSession.sendLogMessage("Spieler " + player.getPlayerName() + " hat sein Dorf Platziert");
            gameSession.sendSetupLocation(player, PieceType.ROAD);
        } else if (setupInfo.nextPiece() == PieceType.ROAD) {
            if (piece != PieceType.ROAD) {
                throw new SetupException("Place Road instead");
            }
            if (!checkBoardRuleForSetup(gameSession, player, coord, piece, setupInfo)) {
                gameSession.sendLogMessage("Platzieren Sie eine Straße");
                throw new SetupException("Invalid Piece");
            }
            placePiece(gameSession, setupInfo, new RoadPiece(player), coord);
            gameSession.sendLogMessage("Spieler " + player.getPlayerName() + " hat seine Straße Platziert");
        }
        //save sate of setup in context
        context.setSetupPhaseCompleted(isSetupComplete(context));
    }

    /**
     * Checks Placement rules for a given piece at a given Coordinate, the setup phase follows
     * different rules than normal gameplay
     *
     * @param gameSession the gameSession the piece was placed in
     * @param player      the player who placed the piece
     * @param coord       the coordinate for the piece
     * @param piece       the piece at the coordinate
     * @param setupInfo   the SetupInfo of the player
     * @return {@code true} if the piece is allowed to be placed here
     */
    public boolean checkBoardRuleForSetup(GameSession gameSession, Player player, Coord coord, PieceType piece, StateContext.SetupInfo setupInfo) {
        switch (piece) {
            case CITY:
                // no cities in setup
                return false;
            case ROAD:
                return player.checkBoardRules(coord, piece) && setupInfo.getRoads(gameSession.getBoard()).contains(coord);
            case SETTLEMENT:
                return gameSession.getBoard().isEmptyCoord(coord) && !gameSession.getBoard().containsSettlementOnAdjacentCoords(coord);
            default:
        }
        return false;
    }

    /**
     * Distributes the resources of the hexagons next to the given Corner position to the given player
     *
     * @param gameSession the current gameSession
     * @param player      the player who will receive the resources
     * @param position    the position of the corner
     */
    private void distributeResources(GameSession gameSession, Player player, Coord position) {
        ResourceEnumMap resourceEnumMap = new ResourceEnumMap();
        Coord[] hexagonCoords = BoardUtils.getCornerHexagonsWithoutWater(gameSession.getBoard(), position);
        for (Coord coord : hexagonCoords) {
            ResourceType resource = ResourceTile.toResourceType(gameSession.getBoard().getResourceTileFromCoord(coord));
            resourceEnumMap.computeIfPresent(resource, (resourceType, amount) -> amount += 1);
        }
        gameSession.getInventoryService().increaseResources(player, resourceEnumMap);
    }

    /**
     * Updates the legal setup nodes of all players in the game session around the given position
     * <p>
     * The difference to the normal legal nodes are that the roads have to be next to the last placed settlement
     *
     * @param gameSession    the current gameSession
     * @param coord          the position of the placed piece
     * @param buildablePiece the piece that was placed
     */
    private void updateLegalSetupMovesForEveryPlayer(GameSession gameSession, Coord coord, BuildablePiece buildablePiece) {
        for (Player p : gameSession.getPlayers()) {
            p.updateLegalSetupNodes(coord, buildablePiece);
        }
    }

    /**
     * Initializes the legal roads for all players in the game session after the end of the setup phase
     *
     * @param gameSession the current gameSession
     */
    private void initLegalRoadsForAllPlayers(GameSession gameSession) {
        for (Player p : gameSession.getPlayers()) {
            p.initLegalRoadNodes();
        }
    }

    /**
     * Returns whether the setup state is done
     *
     * @param context the state context of the game session
     * @return {@code true} if the setup state is done
     */
    private boolean isSetupComplete(StateContext context) {
        for (StateContext.SetupInfo setupInfo : context.getSetupInfos().values()) {
            if (!setupInfo.isDone(MAX_AMOUNT_OF_PLACEMENTS)) {
                return false;
            }
        }
        return true;
    }
}
