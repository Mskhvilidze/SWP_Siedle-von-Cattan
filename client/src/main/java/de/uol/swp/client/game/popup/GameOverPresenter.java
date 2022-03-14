package de.uol.swp.client.game.popup;

import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.common.game.dto.PlayerDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

/**
 * Presents a Game Over screen at the end of a game
 */

public class GameOverPresenter extends AbstractGamePresenter {


    @FXML
    private Label secondPositionPoints;
    @FXML
    private Label secondPositionName;
    @FXML
    private Label thirdPositionPoints;
    @FXML
    private Label thirdPositionName;
    @FXML
    private Label fourthPositionName;
    @FXML
    private Label fourthPositionPoints;
    @FXML
    private Label fourthPosition;
    @FXML
    private Label firstPositionPoints;
    @FXML
    private Label firstPositionName;


    @FXML
    private void onLeaveButtonPressed() {
        gameSessionService.leaveGameAndLobby(gameSessionName);
    }

    /**
     * Displays the standings, standings is an ordered List
     *
     * @param standings a List of the players in order of VP descending
     */
    public void displayStandings(List<PlayerDTO> standings) {


        for (int i = 0; i < standings.size(); i++) {
            switch (i) {
                case 0:
                    firstPositionName.setText(standings.get(i).getPlayerName());
                    firstPositionPoints.setText(Integer.toString(standings.get(i).getVictoryPoints()));
                    break;
                case 1:
                    secondPositionName.setText(standings.get(i).getPlayerName());
                    secondPositionPoints.setText(Integer.toString(standings.get(i).getVictoryPoints()));
                    break;
                case 2:
                    thirdPositionName.setText(standings.get(i).getPlayerName());
                    thirdPositionPoints.setText(Integer.toString(standings.get(i).getVictoryPoints()));
                    break;
                case 3:
                    fourthPositionName.setText(standings.get(i).getPlayerName());
                    fourthPositionPoints.setText(Integer.toString(standings.get(i).getVictoryPoints()));
                    break;
                default:
                    throw new IllegalArgumentException("The given list of standings should not be longer than 4");
            }
        }

        // remove last entry
        if (standings.size() == 3) {
            fourthPositionPoints.setText("");
            fourthPositionName.setText("");
            fourthPosition.setText("");
        }
    }
}
