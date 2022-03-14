package de.uol.swp.client.game.popup;

import de.uol.swp.client.AbstractPresenter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 * Presenter presenting a dice view
 */
public class DicePresenter extends AbstractPresenter {

    private final StringProperty playerName = new SimpleStringProperty();
    private final IntegerProperty diceValue = new SimpleIntegerProperty();
    @FXML
    public Label label;
    @FXML
    public Label robberLabel;
    @FXML
    public ImageView robberImage;

    /**
     * Initializes the dice bindings
     */
    public void initialize() {
        label.textProperty().bind(Bindings.concat(playerName, "\nhat eine ", diceValue, " gewürfelt"));
    }

    /**
     * Sets the right information into the labels
     *
     * @param playerName name of the player whos on turn
     * @param diceValue  value of the dice roll
     */
    public void updateValues(String playerName, int diceValue) {
        this.playerName.set(playerName);
        this.diceValue.set(diceValue);
        if (diceValue == 7) {
            robberLabel.setVisible(true);
            robberImage.setVisible(true);
        } else {
            robberLabel.setVisible(false);
            robberImage.setVisible(false);
        }
    }
}

