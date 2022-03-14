package de.uol.swp.client.game.popup;

import com.jfoenix.controls.JFXButton;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.trade.ResourceControl;
import de.uol.swp.common.game.board.ResourceEnumMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Presenter presenting a resource picker to select resources upon a DiscardResourceNotificationMessage
 */
public class DiscardCardsPresenter extends AbstractGamePresenter {

    @FXML
    public JFXButton acceptDiscard;
    @FXML
    public ResourceControl discardLumber;
    @FXML
    public ResourceControl discardWool;
    @FXML
    public ResourceControl discardGrain;
    @FXML
    public ResourceControl discardOre;
    @FXML
    public ResourceControl discardBrick;
    @FXML
    public Label discardMessage;
    @FXML
    public Label responseMessage;
    private int amount;
    private ResourceEnumMap resourcesToDiscard;


    // TODO should no be able to select more than in inventory

    /**
     * Check if the amount of selected resource is equal to the needed amount and
     * if the player has the clicked resources in his/her inventory
     *
     * @return true if enough resources are selected
     */
    public boolean checkCondition() {
        ResourceEnumMap selected = new ResourceEnumMap(discardLumber.getCounter(), discardWool.getCounter(), discardGrain.getCounter(),
                discardOre.getCounter(), discardBrick.getCounter());
        discardMessage.setText("Sie wurden ausgeraubt! Abzugebende Rohstoffe: " + (amount - selected.sumOfResources()));
        if (selected.sumOfResources() == amount && playerInfo.getResources().hasResources(selected)) {
            resourcesToDiscard = new ResourceEnumMap(discardLumber.getCounter(), discardWool.getCounter(), discardGrain.getCounter(),
                    discardOre.getCounter(), discardBrick.getCounter());
            acceptDiscard.setDisable(false);
            return true;
        } else {
            acceptDiscard.setDisable(true);
            return false;
        }

    }

    /**
     * Sets the reponse Label to the given String
     *
     * @param message response from the resourceDiscardResponse
     */
    public void setResponseMessage(String message) {
        responseMessage.setText(message);
    }

    /**
     * On accept button clicked
     */
    @FXML
    public void onAcceptDiscardButtonPressed() {
        if (checkCondition()) {
            discardLumber.setCounter(0);
            discardWool.setCounter(0);
            discardGrain.setCounter(0);
            discardOre.setCounter(0);
            discardBrick.setCounter(0);
            acceptDiscard.setDisable(true);
            gameSessionService.postDiscardRequest(gameSessionName, resourcesToDiscard);
        }
    }

    /**
     * Getter for the selected Resources
     *
     * @return a ResouceEnum of the selected Resources
     */
    public ResourceEnumMap getResourcesToDiscard() {
        return resourcesToDiscard;
    }

    /**
     * Setter for the amount of cards to discard
     *
     * @param amount number of cards
     */
    public void setAmountToDiscard(Integer amount) {
        this.amount = amount;
    }
}
