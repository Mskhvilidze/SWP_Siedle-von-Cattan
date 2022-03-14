package de.uol.swp.server.game.state;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.message.RobberDoneMessage;
import de.uol.swp.common.game.request.PlayerPickerRequest;
import de.uol.swp.common.game.request.RobberPlacingRequest;
import de.uol.swp.common.game.response.PlayerPickerResponse;
import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.InvalidGameStateException;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.exception.RobberPlacingException;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.board.BoardUtils;
import de.uol.swp.server.game.session.GameSession;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This state is called after the robberDiscard State and allows for a user to change the robber position
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public enum RobberPlacingState implements GameState {

    INSTANCE;

    // 1. Der platzierende Spieler darf sich einen Spieler aussuchen
    // 2. Dieser eine Spieler muss eine Random resource an den platzierenden Spieler abgeben

    @Override
    public void next(GameSession gameSession, StateContext context) {
        if (gameSession.isPlayedDevCardInDiceState()) {
            gameSession.setPlayedDevCardInDiceState(false);
            gameSession.setCurrentState(DiceState.INSTANCE);
        } else {
            gameSession.setCurrentState(PlayState.INSTANCE);
        }
        context.setRobberPlaced(false);
    }

    @Override
    public void action(GameSession gameSession, StateContext context) throws GameStateException {
        if (context.getLastRequest() instanceof PlayerPickerRequest) {
            PlayerPickerRequest request = (PlayerPickerRequest) context.getLastRequest();
            if (!gameSession.getWhoseTurn().getPlayerName().equals(request.getUserNameFromSender())) {
                return;
            }
            Player victim = gameSession.getPlayer(request.getSelectedPlayer().getPlayerId());
            Player player = gameSession.getWhoseTurn();
            processPick(gameSession, player, victim);
            PlayerPickerResponse response = new PlayerPickerResponse(gameSession.getGameSessionName());
            gameSession.getGameSessionService().sendResponseToPlayer(gameSession.getGameSessionName(), request, response, player.isBot());
            RobberDoneMessage message = new RobberDoneMessage(gameSession.getGameSessionName(), player.getPlayerName(), victim.getPlayerName());
            gameSession.getGameSessionService().sendToAllInGameSession(gameSession.getGameSessionName(), message);

            next(gameSession, context);
        } else if (context.getLastRequest() instanceof RobberPlacingRequest) {
            if (context.isRobberPlaced()) {
                throw new RobberPlacingException("Robber was already Placed");
            }
            RobberPlacingRequest request = (RobberPlacingRequest) context.getLastRequest();
            Player player = gameSession.getPlayer(request.getUserNameFromSender());
            if (!gameSession.getWhoseTurn().equals(player)) {
                throw new RobberPlacingException("Player is not allowed to place the robber");
            }
            processRobber(gameSession, context, request.getRobberPosition());
            List<Player> victims = getPossibleVictims(gameSession, player, request.getRobberPosition());
            if (victims.isEmpty()) {
                next(gameSession, context);
            } else {
                gameSession.sendPlayerPickerMessage(victims, player);
            }
        } else {
            throw new InvalidGameStateException();
        }
    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        AbstractRequestMessage request = (AbstractRequestMessage) context.getLastRequest();
        Player player = gameSession.getWhoseTurn();
        Coord robberPos = gameSession.getBoard().getRobber().getCoord();

        if (!context.isRobberPlaced()) {
            robberPos = gameSession.getBoard().getFreeRobberPos();
            gameSession.getBoard().updateRobberPos(robberPos);
            gameSession.sendUpdatedRobberPosition(robberPos);
        }
        var victims = getPossibleVictims(gameSession, player, robberPos);
        if (!victims.isEmpty()) {
            processPick(gameSession, player, victims.get(0));
            PlayerPickerResponse response = new PlayerPickerResponse(gameSession.getGameSessionName());
            gameSession.getGameSessionService().sendResponseToPlayer(gameSession.getGameSessionName(), request, response, player.isBot());
        }

        context.setRobberPlaced(false);
        GameState.super.endTurn(gameSession, context);
    }

    @Override
    public void beginState(GameSession gameSession, StateContext context) {
        gameSession.sendStartRobber(gameSession.getWhoseTurn());
    }

    /**
     * Places the robber at the given position and updates {@link StateContext#isRobberPlaced()}
     *
     * @param gameSession the gameSession the robber was moved in
     * @param context     the state context of the game session
     * @param robberPos   the position the robber was moved to
     */
    private void processRobber(GameSession gameSession, StateContext context, Coord robberPos) {
        gameSession.getBoard().updateRobberPos(robberPos);
        gameSession.sendUpdatedRobberPosition(robberPos);
        context.setRobberPlaced(true);
    }

    /**
     * Transfers a random resource from the given victim to the given player and sends a system log to the chat
     *
     * @param gameSession the gameSession the robber was moved in
     * @param player      the player who is receiving the resource
     * @param victim      the player who is giving the resource
     */
    private void processPick(GameSession gameSession, Player player, Player victim) {
        try {
            ResourceType removedResource = gameSession.getInventoryService().removeRandomResources(victim, 1).get(0);
            gameSession.getInventoryService().increaseResource(player, removedResource, 1);
            gameSession.sendLogMessage(player.getPlayerName() + " hat den Räuber bewegt und  von " + victim.getPlayerName() + " gestohlen.");
        } catch (OverDrawException e) {
            gameSession.sendLogMessage(player.getPlayerName() + " hat den Räuber bewegt aber " + victim.getPlayerName() + " hatte keine Ressourcen.");
        }
    }


    /**
     * Returns a shuffled list filled with unique instances of all victims that are adjacent to the given hex
     *
     * @param gameSession current gameSession
     * @param player      excluded player
     * @param pos         hex coord players surround
     */
    private List<Player> getPossibleVictims(GameSession gameSession, Player player, Coord pos) {
        Set<Player> playersAdjacentToHex = BoardUtils.getPlayersAdjacentToHex(gameSession.getBoard(), pos);
        var uniqueList = playersAdjacentToHex.stream().filter(p -> !p.equals(player)).collect(Collectors.toList());
        Collections.shuffle(uniqueList);
        return uniqueList;
    }
}
