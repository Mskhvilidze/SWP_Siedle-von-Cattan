package de.uol.swp.client.game.player;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This class is used to represent player info
 */
public class ProfileElement extends Pane {

    public static final String DEV_CARDS = "Entwicklungskarten: ";
    public static final String RESOURCES = "Ressourcen: ";
    public static final String VICTORY_POINTS = "SP: ";
    private static final Logger LOG = LogManager.getLogger(ProfileElement.class);
    @FXML
    private Rectangle playerColor;
    @FXML
    private Label playerName;
    @FXML
    private HBox container2;
    @FXML
    private Label victoryPointLabel;
    @FXML
    private ImageView knightBonusDisplay;
    @FXML
    private ImageView longestRoadBonusDisplay;
    @FXML
    private Label resourceAmountLabel;
    @FXML
    private Label devCardAmountLabel;

    /**
     * Constructor
     */
    public ProfileElement() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game/ProfileElement.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            LOG.error("ResourceControl loading failed at {} because of {}", exception.getMessage(), exception.getCause());
        }
    }

    /**
     * Sets the player color of the profile
     *
     * @param color the player color of the profile
     */
    public void setPlayerColor(Color color) {
        this.playerColor.setFill(color);
    }

    /**
     * Sets whether the knight bonus should be displayed for the player
     *
     * @param visible whether the knight bonus should be displayed for the player
     */
    public void setKnightBonusDisplayVisible(boolean visible) {
        this.knightBonusDisplay.setVisible(visible);
    }

    /**
     * Sets the amount of victory points that should be displayed for the player
     *
     * @param victoryPoints the amount of victory points that should be displayed for the player
     */
    public void setVictoryPointLabel(int victoryPoints) {
        this.victoryPointLabel.setText(VICTORY_POINTS + victoryPoints);
    }

    /**
     * Sets the amount of resources that should be displayed for the player
     *
     * @param resourceAmount the amount of resources that should be displayed for the player
     */
    public void setResourceAmountLabel(int resourceAmount) {
        this.resourceAmountLabel.setText(RESOURCES + resourceAmount);
    }

    /**
     * Sets the amount of dev cards that should be displayed for the player
     *
     * @param devCardAmount the amount of dev cards that should be displayed for the player
     */
    public void setDevCardAmountLabel(int devCardAmount) {
        this.devCardAmountLabel.setText(DEV_CARDS + devCardAmount);
    }

    /**
     * Returns the name of the player
     *
     * @return the name of the player
     */
    public String getPlayerName() {
        return playerName.getText();
    }

    /**
     * Sets the name that should be displayed for the player
     */
    public void setPlayerName(String playerName) {
        Platform.runLater(() -> this.playerName.setText(playerName));
    }

    /**
     * Sets whether the longest road bonus should be displayed for the player
     *
     * @param visible whether the longest road bonus should be displayed for the player
     */
    public void setLongestRoadBonusVisible(boolean visible) {
        this.longestRoadBonusDisplay.setVisible(visible);
    }
}
