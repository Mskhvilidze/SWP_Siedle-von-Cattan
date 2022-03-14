package de.uol.swp.client.game.trade;

import com.jfoenix.controls.JFXButton;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.client.game.GameSessionService;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * The presenter for creating a new trade offer. Is included in the {@link GameSessionPresenter}
 */
public class TradePresenter extends AbstractGamePresenter {

    @FXML
    private AnchorPane root;
    @FXML
    private ResourceControl requestedLumber;
    @FXML
    private ResourceControl requestedWool;
    @FXML
    private ResourceControl requestedGrain;
    @FXML
    private ResourceControl requestedOre;
    @FXML
    private ResourceControl requestedBrick;
    @FXML
    private ResourceControl offeredLumber;
    @FXML
    private ResourceControl offeredWool;
    @FXML
    private ResourceControl offeredGrain;
    @FXML
    private ResourceControl offeredOre;
    @FXML
    private ResourceControl offeredBrick;
    @FXML
    private JFXButton startTradeButton;
    @FXML
    private Label informationLabel;

    private TradeOffer currentTradeOffer;
    private boolean edit;

    /**
     * This method updates the gui and enables/disables the trade button, depending on the offered resources and
     * the resources in the playerinfo
     */
    @FXML
    private void updateGui() {
        ResourceEnumMap offered = new ResourceEnumMap();
        ResourceEnumMap requested = new ResourceEnumMap();
        putRss(offered, offeredLumber, offeredWool, offeredGrain, offeredOre, offeredBrick);
        putRss(requested, requestedLumber, requestedWool, requestedGrain, requestedOre, requestedBrick);
        if (playerInfo.getResources().hasResources(offered) && offered.sumOfResources() + requested.sumOfResources() != 0) {
            startTradeButton.setDisable(false);
            informationLabel.setVisible(false);
        } else {
            startTradeButton.setDisable(true);
            informationLabel.setVisible(true);
        }
    }

    /**
     * This method is called when the close button is pressed
     */
    @FXML
    private void onCloseButton() {
        close();
    }

    public void close() {
        root.getParent().setVisible(false);
        offeredLumber.counterProperty().set(0);
        offeredWool.counterProperty().set(0);
        offeredGrain.counterProperty().set(0);
        offeredOre.counterProperty().set(0);
        offeredBrick.counterProperty().set(0);
        requestedLumber.counterProperty().set(0);
        requestedWool.counterProperty().set(0);
        requestedGrain.counterProperty().set(0);
        requestedOre.counterProperty().set(0);
        requestedBrick.counterProperty().set(0);
    }

    /**
     * Setter for the current trade offer
     *
     * @param currentTradeOffer the current trade offer which should be set
     */
    public void setCurrentTradeOffer(TradeOffer currentTradeOffer) {
        this.currentTradeOffer = currentTradeOffer;
    }

    /**
     * Setter for the boolean edit
     *
     * @param edit the boolean which should be set
     */
    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    /**
     * Method called when the trade button is pressed
     * <p>
     * If the trade button is pressed, this method requests the gameSessionService
     * to create a new trade offer. It uses the amount of resources to offer and requested with the name of
     * the gameSession (lobby) as well the user who wants to trade.
     *
     * @param event the ActionEvent created by pressing the trade button
     * @see GameSessionService
     * @since 2021-04-27
     */
    @FXML
    public void onStartTradeButton(ActionEvent event) {
        ResourceEnumMap offered = new ResourceEnumMap();
        ResourceEnumMap requested = new ResourceEnumMap();
        putRss(offered, offeredLumber, offeredWool, offeredGrain, offeredOre, offeredBrick);
        putRss(requested, requestedLumber, requestedWool, requestedGrain, requestedOre, requestedBrick);
        TradeOffer tradeOffer = new TradeOffer(offered, requested);
        if (edit) {
            gameSessionService.startCounterTradeOffer(gameSessionName, currentTradeOffer, tradeOffer);
            edit = false;
        } else {
            gameSessionService.startTrade(gameSessionName, tradeOffer);
        }
        close();
    }

    /**
     * Add resources to a ResourceEnumMap
     *
     * @param map    the map where the resources should be put in
     * @param lumber lumber resource amount
     * @param wool   wool resource amount
     * @param grain  grain resource amount
     * @param ore    ore resource amount
     * @param brick  brick resource amount
     */
    private void putRss(ResourceEnumMap map, ResourceControl lumber, ResourceControl wool, ResourceControl grain, ResourceControl ore,
                        ResourceControl brick) {
        map.put(ResourceType.LUMBER, lumber.getCounter());
        map.put(ResourceType.WOOL, wool.getCounter());
        map.put(ResourceType.GRAIN, grain.getCounter());
        map.put(ResourceType.ORE, ore.getCounter());
        map.put(ResourceType.BRICK, brick.getCounter());
    }
}
