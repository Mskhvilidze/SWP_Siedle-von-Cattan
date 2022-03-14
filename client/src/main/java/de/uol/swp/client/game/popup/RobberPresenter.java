package de.uol.swp.client.game.popup;

import de.uol.swp.client.AbstractPresenter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Presenter presenting a robber view
 */
public class RobberPresenter extends AbstractPresenter {

    private final StringProperty robber = new SimpleStringProperty();
    private final StringProperty victim = new SimpleStringProperty();
    @FXML
    public Label label;

    /**
     * Initializes the robber bindings
     */
    public void initialize() {
        label.textProperty().bind(Bindings.concat(robber, "\nhat \n", victim, "\nberaubt"));
    }


    /**
     * Sets the right information into the label
     *
     * @param robber name of the player whos the robber
     * @param victim name of the player who is the victim
     */
    public void updateValues(String robber, String victim) {
        this.robber.set(robber);
        this.victim.set(victim);
    }
}

