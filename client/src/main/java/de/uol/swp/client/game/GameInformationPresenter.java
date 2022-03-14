package de.uol.swp.client.game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * The Presenter for the current game state information
 */
public class GameInformationPresenter extends AbstractGamePresenter {

    @FXML
    private Label informationTurnLabel;
    @FXML
    private Label informationStateLabel;
    @FXML
    private Label informationActionLabel;
    private String stateGerman;
    private String action;

    /**
     * This method updates the information pane depending which state and which players is on turn.
     *
     * @param state the current state of the game
     */
    public void updateInformationPane(String state) {
        Platform.runLater(() -> {
            if (gameSessionPresenter.getWhoseTurn().equals(userInfo.getLoggedInUser().getUsername())) {
                informationTurnLabel.setText("Sie sind am Zug.");
                switch (state) {
                    case "SetupState":
                        stateGerman = "Aufbau-Phase";
                        action = "Setzen Sie ein Dorf dann eine Straße.";
                        break;
                    case "EndState":
                        stateGerman = "End-Phase";
                        action = "Warten Sie auf Ihren Zug.";
                        break;
                    case "DiceState":
                        stateGerman = "Würfel-Phase";
                        action = "Würfeln oder Entwicklungskarte spielen.";
                        break;
                    case "BuildState":
                        stateGerman = "Bau-Phase";
                        break;
                    case "TradeState":
                        stateGerman = "Handel-Phase";
                        action = "Handeln Sie mit anderen Spielern.";
                        break;
                    case "PlayState":
                        stateGerman = "Spiel-Phase";
                        action = "Handeln, kaufen, bauen, spielen Sie.";
                        break;
                    case "RobberPlacingState":
                        stateGerman = "Räuber-Phase";
                        action = "Platzieren Sie den Räuber.";
                        break;
                    case "RobberDiscardState":
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } else {
                stateGerman = "";
                action = "Warten Sie auf Ihren Zug.";
                informationTurnLabel.setText(gameSessionPresenter.getWhoseTurn() + " ist am Zug.");
            }
            if ("RobberDiscardState".equals(state)) {
                action = "Ressourcen abgeben, bei 7 Ressourcen oder mehr.";
                stateGerman = "Räuber-Phase";
            }
            informationStateLabel.setText(stateGerman);
            informationActionLabel.setText(action);
        });
    }
}

