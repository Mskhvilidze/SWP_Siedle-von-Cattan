package de.uol.swp.client.game.popup;

import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.GameSessionService;
import javafx.fxml.FXML;

/**
 * Presenter to ask the player, if he/she really wants to leave the game
 */
public class LeaveGamePresenter extends AbstractGamePresenter {

    /**
     * Method called when the leave game button is pressed
     *
     * @see GameSessionService
     */
    @FXML
    private void onLeaveGame() {
        gameSessionService.leaveGameAndLobby(gameSessionName);
    }

    /**
     * Method called when the dont leave game button is pressed
     */
    @FXML
    private void onDontLeaveGame() {
        gameSessionPresenter.onToggleLeaveGame();
    }
}

