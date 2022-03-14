package de.uol.swp.client.game.popup;

import com.jfoenix.controls.JFXButton;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.request.UseCardRequest;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The presenter for the Year Of Plenty Card action.
 *
 * @since 2021-06-13
 */

public class YearOfPlentyPresenter extends AbstractGamePresenter {

    private final Deque<ResourceType> selectedResources = new ArrayDeque<>();
    private final PseudoClass selected = PseudoClass.getPseudoClass("selected");

    @FXML
    private Pane yearOfPlentyLumber;
    @FXML
    private Pane yearOfPlentyBrick;
    @FXML
    private Pane yearOfPlentyGrain;
    @FXML
    private Pane yearOfPlentyWool;
    @FXML
    private Pane yearOfPlentyOre;
    @FXML
    private JFXButton playYearOfPlentyCard;

    /**
     * Deselects a resource card in the YearOfPlentyView when another card is selected.
     *
     * @param resourceType - The resources that a hex tile can have. Desert tiles do not have a resource.
     * @see ResourceType
     */
    private void deselect(ResourceType resourceType) {
        switch (resourceType) {
            case LUMBER:
                yearOfPlentyLumber.pseudoClassStateChanged(selected, false);
                break;
            case BRICK:
                yearOfPlentyBrick.pseudoClassStateChanged(selected, false);
                break;
            case GRAIN:
                yearOfPlentyGrain.pseudoClassStateChanged(selected, false);
                break;
            case WOOL:
                yearOfPlentyWool.pseudoClassStateChanged(selected, false);
                break;
            case ORE:
                yearOfPlentyOre.pseudoClassStateChanged(selected, false);
                break;
            default:
                throw new IllegalStateException("ResourceTile not accepted.");
        }
    }

    /**
     * Called when Player selects in the YearOfPlentyView the lumber card.
     */
    @FXML
    public void onSelectYearOfPlentyLumber() {
        if (selectedResources.size() == 1) {
            playYearOfPlentyCard.setDisable(false);
        }
        if (selectedResources.size() == 2) {
            deselect(selectedResources.pop());
        }
        yearOfPlentyLumber.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.LUMBER);
    }

    /**
     * Called when Player selects in the YearOfPlentyView the brick card.
     */
    @FXML
    public void onSelectYearOfPlentyBrick() {
        if (selectedResources.size() == 1) {
            playYearOfPlentyCard.setDisable(false);
        }
        if (selectedResources.size() == 2) {
            deselect(selectedResources.pop());
        }
        yearOfPlentyBrick.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.BRICK);
    }

    /**
     * Called when Player selects in the YearOfPlentyView the grain card.
     */
    @FXML
    public void onSelectYearOfPlentyGrain() {
        if (selectedResources.size() == 1) {
            playYearOfPlentyCard.setDisable(false);
        }
        if (selectedResources.size() == 2) {
            deselect(selectedResources.pop());
        }
        yearOfPlentyGrain.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.GRAIN);
    }

    /**
     * Called when Player selects in the YearOfPlentyView the wool card.
     */
    @FXML
    public void onSelectYearOfPlentyWool() {
        if (selectedResources.size() == 1) {
            playYearOfPlentyCard.setDisable(false);
        }
        if (selectedResources.size() == 2) {
            deselect(selectedResources.pop());
        }
        yearOfPlentyWool.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.WOOL);
    }

    /**
     * Called when Player selects in the YearOfPlentyView the ore card.
     */
    @FXML
    public void onSelectYearOfPlentyOre() {
        if (selectedResources.size() == 1) {
            playYearOfPlentyCard.setDisable(false);
        }
        if (selectedResources.size() == 2) {
            deselect(selectedResources.pop());
        }
        yearOfPlentyOre.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.ORE);
    }

    /**
     * Called when Player wants to play the year of plenty card, after selecting two resource cards.
     */
    @FXML
    public void onPlayYearOfPlentyCard() {
        ResourceEnumMap resourceEnumMap = new ResourceEnumMap();
        ResourceType deselectedResource1 = selectedResources.pop();
        ResourceType deselectedResource2 = selectedResources.pop();
        deselect(deselectedResource1);
        deselect(deselectedResource2);
        if (deselectedResource1 == deselectedResource2) {
            resourceEnumMap.put(deselectedResource1, 2);
        } else {
            resourceEnumMap.put(deselectedResource1, 1);
            resourceEnumMap.put(deselectedResource2, 1);
        }
        gameSessionPresenter.onToggleYearOfPlentyCard();
        playYearOfPlentyCard.setDisable(true);
        eventBus.post(new UseCardRequest(gameSessionName, DevCard.YEAR_OF_PLENTY, resourceEnumMap));
    }
}