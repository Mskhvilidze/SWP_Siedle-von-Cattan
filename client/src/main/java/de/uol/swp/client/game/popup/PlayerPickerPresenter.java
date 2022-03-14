package de.uol.swp.client.game.popup;

import com.jfoenix.controls.JFXButton;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.common.game.dto.PlayerDTO;
import javafx.fxml.FXML;
import javafx.scene.shape.Rectangle;

import java.util.List;


/**
 * Main presenter for the Player Picker
 * Allows a player to pick a player from the given list
 */
public class PlayerPickerPresenter extends AbstractGamePresenter {

    public static final String FXML = "/fxml/PlayerPickerView.fxml";

    @FXML
    private Rectangle p1Color;
    @FXML
    private Rectangle p2Color;
    @FXML
    private Rectangle p3Color;
    @FXML
    private JFXButton p1Button;
    @FXML
    private JFXButton p2Button;
    @FXML
    private JFXButton p3Button;

    private List<PlayerDTO> players;

    /**
     * Setter for players
     * <p>
     */
    public void setPlayers(List<PlayerDTO> playersList) {
        this.players = playersList;
    }

    /**
     * Method called when p1 is pressed
     */
    @FXML
    public void onOneRobbButtonPressed() {

        gameSessionService.sendPlayerPickerRequest(gameSessionName, players.get(0));
    }

    /**
     * Method called when p2 is pressed
     */
    @FXML
    public void onTwoRobbButtonPressed() {

        gameSessionService.sendPlayerPickerRequest(gameSessionName, players.get(1));
    }

    /**
     * Method called when p3 is pressed
     */
    @FXML
    public void onThreeRobbButtonPressed() {

        gameSessionService.sendPlayerPickerRequest(gameSessionName, players.get(2));

    }

    /**
     * Method to set the VBox of the players to visible and set
     * the player name and the color of the name
     */
    public void initialisePlayers() {
        if (players.size() > 0) {
            p1Color.setVisible(true);
            p1Color.setFill(GameSessionPresenter.getFXColorFromPlayerColor(players.get(0).getColor()));
            p1Button.setVisible(true);
            p1Button.setText(players.get(0).getPlayerName());

        }
        if (players.size() > 1) {
            p2Color.setVisible(true);
            p2Color.setFill(GameSessionPresenter.getFXColorFromPlayerColor(players.get(1).getColor()));
            p2Button.setVisible(true);
            p2Button.setText(players.get(1).getPlayerName());
        }
        if (players.size() > 2) {
            p3Color.setVisible(true);
            p3Color.setFill(GameSessionPresenter.getFXColorFromPlayerColor(players.get(2).getColor()));
            p3Button.setVisible(true);
            p3Button.setText(players.get(2).getPlayerName());
        }
    }

}

