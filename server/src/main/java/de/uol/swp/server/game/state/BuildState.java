package de.uol.swp.server.game.state;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.message.build.CancelBuildMessage;
import de.uol.swp.common.game.message.build.ObjectWasPlacedMessage;
import de.uol.swp.common.game.request.build.CancelBuildRequest;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import de.uol.swp.common.game.request.build.StartBuildRequest;
import de.uol.swp.common.game.response.build.PlaceObjectResponse;
import de.uol.swp.common.game.response.build.StartBuildResponse;
import de.uol.swp.server.exception.BuildException;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.game.InventoryService;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.board.BoardUtils;
import de.uol.swp.server.game.mapobject.BuildablePiece;
import de.uol.swp.server.game.session.GameSession;

import java.util.ArrayList;
import java.util.Set;

/**
 * This state is called during the PlayState and handles all building requests
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public enum BuildState implements GameState {

    INSTANCE;

    @Override
    public void next(GameSession gameSession, StateContext context) {
        if (gameSession.isPlayedDevCardInDiceState() && context.getFreeBuildableRoads() == 0) {
            gameSession.setCurrentState(DiceState.INSTANCE);
            gameSession.setPlayedDevCardInDiceState(false);
        } else {
            gameSession.setCurrentState(PlayState.INSTANCE);
        }
    }


    @Override
    public void action(GameSession gameSession, StateContext context) throws GameStateException {

        if (context.getLastRequest() instanceof StartBuildRequest) {
            StartBuildRequest request = (StartBuildRequest) context.getLastRequest();
            Player player = gameSession.getPlayer(request.getUserNameFromSender());

            if (!gameSession.getWhoseTurn().equals(player)) {
                handleExceptions(gameSession, context, "It is not the players turn");
            }
            Set<Coord> legalNodes = player.getLegalNodes(request.getPieceType());
            if (!legalNodes.isEmpty()) {
                if (request.getPieceType() == PieceType.ROAD && context.getFreeBuildableRoads() > 0) {
                    gameSession.sendSetupLocation(player, request.getPieceType());
                } else {
                    StartBuildResponse response = new StartBuildResponse(gameSession.getGameSessionName(), legalNodes, request.getPieceType());
                    gameSession.getGameSessionService().sendResponseToPlayer(gameSession.getGameSessionName(), request, response, player.isBot());
                }
            } else {
                if (request.getPieceType() == PieceType.ROAD && context.getFreeBuildableRoads() > 0) {
                    //TODO: Cancel Road Karte da keine Straßen mehr gibt
                }
                next(gameSession, context);
            }
        } else if (context.getLastRequest() instanceof CancelBuildRequest) {
            CancelBuildRequest request = (CancelBuildRequest) context.getLastRequest();
            Player player = gameSession.getPlayer(request.getUserNameFromSender());

            if (!gameSession.getWhoseTurn().equals(player)) {
                handleExceptions(gameSession, context, "It is not the players turn");
            }
            if (context.getFreeBuildableRoads() > 0) {
                handleExceptions(gameSession, context, "Player cant cancel free roads");
            }
            CancelBuildMessage message = new CancelBuildMessage(gameSession.getGameSessionName());
            gameSession.getGameSessionService().sendToPlayerInGameSession(gameSession.getGameSessionName(), message, player.getPlayerName(),
                    player.isBot());
            next(gameSession, context);
        } else if (context.getLastRequest() instanceof PlaceObjectRequest) {
            PlaceObjectRequest request = (PlaceObjectRequest) context.getLastRequest();
            Player player = gameSession.getPlayer(request.getUserNameFromSender());


            //check turn
            if (!gameSession.getWhoseTurn().equals(player)) {
                handleExceptions(gameSession, context, "It is not the players turn");
            }
            PieceType piece = request.getObjectToPlace();
            //  check inventory
            if (!(player.hasEnoughResources(piece) || (piece == PieceType.ROAD && context.getFreeBuildableRoads() > 0))) {
                handleExceptions(gameSession, context, "Not enough resources to build and no free roads to place");
            }
            Coord coord = request.getCoord();
            // check for rules
            if (player.checkBoardRules(coord, piece)) {
                placePiece(gameSession, context, player, coord, piece);
                next(gameSession, context);
            } else {
                gameSession.sendLogMessage(player.getPlayerName() + "! Sie können hier nichts bauen.");
                throw new BuildException("Rules do not allow an object here.");
            }
        }
    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        Player player = gameSession.getWhoseTurn();
        int noOfFreeRoads = context.getFreeBuildableRoads();
        for (int i = 0; i < noOfFreeRoads; i++) {
            Coord coord = player.getRandomLegalRoad();
            try {
                placePiece(gameSession, context, player, coord, PieceType.ROAD);
            } catch (BuildException e) {
                e.printStackTrace();
            }
        }
        CancelBuildMessage message = new CancelBuildMessage(gameSession.getGameSessionName());
        gameSession.getGameSessionService().sendToPlayerInGameSession(gameSession.getGameSessionName(), message, player.getPlayerName(),
                player.isBot());
        GameState.super.endTurn(gameSession, context);
    }

    /**
     * Places the given piece type at the given position and updates the board and player information
     * <p>
     * Removes the necessary resources from the player if no {@link DevCard#ROAD_BUILDING} was used
     * <p>
     * Sends a system log to the chat
     *
     * @param gameSession the gameSession the piece was placed in
     * @param context     the state context of the game session
     * @param player      the player who placed the piece
     * @param coord       the position of the piece
     * @param piece       the piece type that should be placed
     * @throws BuildException if the player doesn't have enough resources
     */
    private void placePiece(GameSession gameSession, StateContext context, Player player, Coord coord, PieceType piece) throws BuildException {
        InventoryService inventoryService = gameSession.getInventoryService();
        BuildablePiece buildablePiece = gameSession.pieceTypeToBuildable(piece, player);
        if (context.getFreeBuildableRoads() > 0 && piece == PieceType.ROAD) {
            context.setFreeBuildableRoads(context.getFreeBuildableRoads() - 1);
            inventoryService.decreaseObjects(player, piece);
        } else {
            try {
                inventoryService.decreaseResources(player, piece);
            } catch (OverDrawException exception) {
                handleExceptions(gameSession, context, "Not enough resources to build");
            }
        }
        gameSession.getBoard().addPiece(coord, buildablePiece);
        //update legal moves for every player
        for (Player p : gameSession.getPlayers()) {
            p.updateLegalNodes(coord, buildablePiece);
        }

        switch (piece) {
            case ROAD: {
                Player oldPlayerWithLongestRoad = gameSession.getPlayerWithLongestRoad();
                gameSession.getBoard().findLongestRoadFromEdge(coord, player);
                updateLongestRoad(gameSession, oldPlayerWithLongestRoad);
                gameSession.sendLogMessage(player.getPlayerName() + " hat eine Straße gebaut.");
                break;
            }
            case SETTLEMENT:
                for (Player otherPlayer : gameSession.getPlayers()) {
                    if (!otherPlayer.equals(player)) {
                        var roadCoords = new ArrayList<>(BoardUtils.getConnectedPlayerRoadsFromCorner(gameSession.getBoard(), coord, otherPlayer));
                        var longestPath = gameSession.getBoard().getLongestPlayerRoad(otherPlayer).getVisited();
                        int count = 0;
                        for (Coord roadCoord : roadCoords) {
                            if (longestPath.contains(roadCoord)) count++;
                        }
                        if (count >= 2) {
                            Player oldPlayerWithLongestRoad = gameSession.getPlayerWithLongestRoad();
                            gameSession.getBoard().findLongestRoadAfterBreak(otherPlayer);
                            updateLongestRoad(gameSession, oldPlayerWithLongestRoad);
                        }
                    }
                }
                gameSession.sendLogMessage(player.getPlayerName() + " hat eine Siedlung gebaut.");
                player.updateBuildingVictoryPoints(1);
                gameSession.sendVPUpdateMessage(player);
                break;
            case CITY:
                gameSession.sendLogMessage(player.getPlayerName() + " hat eine Stadt gebaut.");
                player.updateBuildingVictoryPoints(1);
                gameSession.sendVPUpdateMessage(player);
                break;
            default:
                throw new IllegalStateException("Piece Type sollte behandelt werden");
        }
        PlaceObjectResponse response = new PlaceObjectResponse(gameSession.getGameSessionName(), piece, true, coord, "");
        gameSession.getGameSessionService().sendResponseToPlayer(gameSession.getGameSessionName(), context.getLastRequest(), response,
                player.isBot());
        ObjectWasPlacedMessage message = new ObjectWasPlacedMessage(gameSession.getGameSessionName(), player.createDTO(), piece, coord);
        gameSession.getGameSessionService().sendToAllInGameSession(gameSession.getGameSessionName(), message);
    }

    private void updateLongestRoad(GameSession gameSession, Player oldPlayerWithLongestRoad) {
        Player newPlayerWithLongestRoad = gameSession.getPlayerWithLongestRoad();
        if (newPlayerWithLongestRoad != null) {
            if (oldPlayerWithLongestRoad == null) {
                gameSession.sendLogMessage(newPlayerWithLongestRoad.getPlayerName() + " hat das größte Handelsnetzwerk.");
                gameSession.sendLongestRoadUpdate(gameSession, newPlayerWithLongestRoad.createDTO());
                gameSession.sendVPUpdateMessage(newPlayerWithLongestRoad);
            } else if (!oldPlayerWithLongestRoad.equals(newPlayerWithLongestRoad)) {
                gameSession.sendLogMessage(newPlayerWithLongestRoad.getPlayerName() + " hat das größte Handelsnetzwerk.");
                gameSession.sendLongestRoadUpdate(gameSession, newPlayerWithLongestRoad.createDTO());
                gameSession.sendVPUpdateMessage(newPlayerWithLongestRoad);
                gameSession.sendVPUpdateMessage(oldPlayerWithLongestRoad);
            }
        } else {
            if (oldPlayerWithLongestRoad != null) {
                gameSession.sendLogMessage(oldPlayerWithLongestRoad.getPlayerName() + " hat das größte Handelsnetzwerk verloren.");
                gameSession.sendLongestRoadUpdate(gameSession, null);
                gameSession.sendVPUpdateMessage(oldPlayerWithLongestRoad);
            }
        }
    }

    private void handleExceptions(GameSession gameSession, StateContext context, String message) throws BuildException {
        next(gameSession, context);
        throw new BuildException(message);
    }
}
