package de.uol.swp.client.game.popup;

import com.jfoenix.controls.JFXButton;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceTile;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.request.UseCardRequest;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The presenter for the Monopoly Card action.
 *
 * @since 2021-06-13
 */
public class MonopolyPresenter extends AbstractGamePresenter {

    private final Deque<ResourceType> selectedResources = new ArrayDeque<>();
    private final PseudoClass selected = PseudoClass.getPseudoClass("selected");

    @FXML
    private Pane monopolyLumber;
    @FXML
    private Pane monopolyBrick;
    @FXML
    private Pane monopolyGrain;
    @FXML
    private Pane monopolyWool;
    @FXML
    private Pane monopolyOre;
    @FXML
    private JFXButton playMonopolyCard;

    /**
     * Deselects a resource card in the MonopolyView when another card is selected.
     *
     * @param resourceType - The resources that a hex tile can have. Desert tiles do not have a resource.
     * @see ResourceTile
     */
    private void deselect(ResourceType resourceType) {
        switch (resourceType) {
            case LUMBER:
                monopolyLumber.pseudoClassStateChanged(selected, false);
                break;
            case BRICK:
                monopolyBrick.pseudoClassStateChanged(selected, false);
                break;
            case GRAIN:
                monopolyGrain.pseudoClassStateChanged(selected, false);
                break;
            case WOOL:
                monopolyWool.pseudoClassStateChanged(selected, false);
                break;
            case ORE:
                monopolyOre.pseudoClassStateChanged(selected, false);
                break;
            default:
                throw new IllegalStateException("ResourceTile not accepted.");
        }
    }

    /**
     * Called when Player selects in the MonopolyView the lumber card.
     */
    @FXML
    public void onSelectMonopolyLumber() {
        if (!selectedResources.isEmpty()) {
            deselect(selectedResources.pop());
        }
        monopolyLumber.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.LUMBER);
        playMonopolyCard.setDisable(false);
    }

    /**
     * Called when Player selects in the MonopolyView the brick card.
     */
    @FXML
    public void onSelectMonopolyBrick() {
        if (!selectedResources.isEmpty()) {
            deselect(selectedResources.pop());
        }
        monopolyBrick.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.BRICK);
        playMonopolyCard.setDisable(false);
    }

    /**
     * Called when Player selects in the MonopolyView the grain card.
     */
    @FXML
    public void onSelectMonopolyGrain() {
        if (!selectedResources.isEmpty()) {
            deselect(selectedResources.pop());
        }
        monopolyGrain.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.GRAIN);
        playMonopolyCard.setDisable(false);
    }

    /**
     * Called when Player selects in the MonopolyView the wool card.
     */
    @FXML
    public void onSelectMonopolyWool() {
        if (!selectedResources.isEmpty()) {
            deselect(selectedResources.pop());
        }
        monopolyWool.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.WOOL);
        playMonopolyCard.setDisable(false);
    }

    /**
     * Called when Player selects in the MonopolyView the ore card.
     */
    @FXML
    public void onSelectMonopolyOre() {
        if (!selectedResources.isEmpty()) {
            deselect(selectedResources.pop());
        }
        monopolyOre.pseudoClassStateChanged(selected, true);
        selectedResources.add(ResourceType.ORE);
        playMonopolyCard.setDisable(false);
    }

    /**
     * Called when Player wants to play the monopoly card, after selecting a resource card.
     */
    @FXML
    public void onPlayMonopolyCard() {
        ResourceEnumMap resourceEnumMap = new ResourceEnumMap();
        ResourceType deselectedResource = selectedResources.pop();
        deselect(deselectedResource);
        resourceEnumMap.put(deselectedResource, 1);
        gameSessionPresenter.onToggleMonopolyCard();
        playMonopolyCard.setDisable(true);
        eventBus.post(new UseCardRequest(gameSessionName, DevCard.MONOPOLY, resourceEnumMap));
    }
}