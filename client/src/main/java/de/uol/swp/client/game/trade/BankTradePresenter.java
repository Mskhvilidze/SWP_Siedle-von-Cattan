package de.uol.swp.client.game.trade;

import com.google.common.eventbus.Subscribe;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.client.game.GameSessionService;
import de.uol.swp.client.game.player.PlayerInfo;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.Port;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * The presenter for creating a new bank trade offer. Is included in the {@link GameSessionPresenter}
 */
public class BankTradePresenter extends AbstractGamePresenter {

    private final IntegerProperty lumberResources = new SimpleIntegerProperty(4);
    private final IntegerProperty woolResources = new SimpleIntegerProperty(4);
    private final IntegerProperty grainResources = new SimpleIntegerProperty(4);
    private final IntegerProperty oreResources = new SimpleIntegerProperty(4);
    private final IntegerProperty brickResources = new SimpleIntegerProperty(4);

    @FXML
    private AnchorPane root;
    @FXML
    private ResourceControl bankLumber;
    @FXML
    private ResourceControl bankWool;
    @FXML
    private ResourceControl bankGrain;
    @FXML
    private ResourceControl bankOre;
    @FXML
    private ResourceControl bankBrick;
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
    private JFXTextField lumberTradeTextField;
    @FXML
    private JFXTextField woolTradeTextField;
    @FXML
    private JFXTextField grainTradeTextField;
    @FXML
    private JFXTextField brickTradeTextField;
    @FXML
    private JFXTextField oreTradeTextField;
    @FXML
    private JFXButton startBankTradeButton;
    @FXML
    private Label lumberTradeResourcesLabel;
    @FXML
    private Label woolTradeResourcesLabel;
    @FXML
    private Label grainTradeResourcesLabel;
    @FXML
    private Label oreTradeResourcesLabel;
    @FXML
    private Label brickTradeResourcesLabel;
    @FXML
    private Label informationLabel;

    @Subscribe
    public void onPlayerInfo(PlayerInfo playerInfo) {
        var ports = playerInfo.getPorts();
        playerInfo.portUpdateProperty().addListener((observable, oldValue, newValue) -> {
            if (ports.contains(Port.ANY)) {
                lumberResources.set(3);
                woolResources.set(3);
                grainResources.set(3);
                oreResources.set(3);
                brickResources.set(3);
            }
            if (ports.contains(Port.LUMBER)) {
                lumberResources.set(2);
            }
            if (ports.contains(Port.WOOL)) {
                woolResources.set(2);
            }
            if (ports.contains(Port.GRAIN)) {
                grainResources.set(2);
            }
            if (ports.contains(Port.ORE)) {
                oreResources.set(2);
            }
            if (ports.contains(Port.BRICK)) {
                brickResources.set(2);
            }
        });
        String suffix = " benötigt";
        lumberTradeTextField.textProperty().bind(lumberResources.asString().concat(":1"));
        lumberTradeResourcesLabel.textProperty().bind(lumberResources.asString().concat(suffix));
        woolTradeTextField.textProperty().bind(woolResources.asString().concat(":1"));
        woolTradeResourcesLabel.textProperty().bind(woolResources.asString().concat(suffix));
        grainTradeTextField.textProperty().bind(grainResources.asString().concat(":1"));
        grainTradeResourcesLabel.textProperty().bind(grainResources.asString().concat(suffix));
        oreTradeTextField.textProperty().bind(oreResources.asString().concat(":1"));
        oreTradeResourcesLabel.textProperty().bind(oreResources.asString().concat(suffix));
        brickTradeTextField.textProperty().bind(brickResources.asString().concat(":1"));
        brickTradeResourcesLabel.textProperty().bind(brickResources.asString().concat(suffix));

        lumberTradeResourcesLabel.visibleProperty().bind(offeredLumber.counterProperty().greaterThan(0));
        woolTradeResourcesLabel.visibleProperty().bind(offeredWool.counterProperty().greaterThan(0));
        grainTradeResourcesLabel.visibleProperty().bind(offeredGrain.counterProperty().greaterThan(0));
        oreTradeResourcesLabel.visibleProperty().bind(offeredOre.counterProperty().greaterThan(0));
        brickTradeResourcesLabel.visibleProperty().bind(offeredBrick.counterProperty().greaterThan(0));
    }

    /**
     * Method called when the trade button is pressed
     * <p>
     * If the trade button is pressed, this method requests the gameSessionService
     * to create a new trade offer. It uses the amount of resources to offer and requested with the name of
     * the gameSession (lobby) as well the user who wants to trade.
     *
     * @see GameSessionService
     */
    @FXML
    public void onStartBankTradeButton() {
        ResourceEnumMap offered = new ResourceEnumMap();
        ResourceEnumMap bank = new ResourceEnumMap();
        putRss(offered, offeredLumber, offeredWool, offeredGrain, offeredOre, offeredBrick);
        putRss(bank, bankLumber, bankWool, bankGrain, bankOre, bankBrick);
        TradeOffer tradeOffer = new TradeOffer(offered, bank);
        gameSessionService.startBankTrade(gameSessionName, tradeOffer);
        close();
    }

    private void putRss(ResourceEnumMap map, ResourceControl lumber, ResourceControl wool, ResourceControl grain, ResourceControl ore,
                        ResourceControl brick) {
        map.put(ResourceType.LUMBER, lumber.getCounter());
        map.put(ResourceType.WOOL, wool.getCounter());
        map.put(ResourceType.GRAIN, grain.getCounter());
        map.put(ResourceType.ORE, ore.getCounter());
        map.put(ResourceType.BRICK, brick.getCounter());
    }

    /**
     * This method updates the gui for the bank trade view.
     * The player's ports are taken into account.
     * When the required resources match the offered resources, the trade button will be visible to start
     * the trade with the bank.
     * Besides the trade ratio is updated.
     */
    @FXML
    private void updateGui() {
        ResourceEnumMap offered = new ResourceEnumMap();
        ResourceEnumMap bank = new ResourceEnumMap();
        putRss(offered, offeredLumber, offeredWool, offeredGrain, offeredOre, offeredBrick);
        putRss(bank, bankLumber, bankWool, bankGrain, bankOre, bankBrick);
        informationLabel.setVisible(true);

        int offeredResources = 0;
        int requiredResources = 0;
        int bankTradeChecker = 0;

        offeredResources += (offeredLumber.getCounter() / lumberResources.get());
        offeredResources += offeredWool.getCounter() / woolResources.get();
        offeredResources += offeredGrain.getCounter() / grainResources.get();
        offeredResources += offeredOre.getCounter() / oreResources.get();
        offeredResources += offeredBrick.getCounter() / brickResources.get();

        requiredResources += bankLumber.getCounter() + bankWool.getCounter() + bankGrain.getCounter() + bankOre.getCounter() + bankBrick.getCounter();
        bankTradeChecker += offeredLumber.getCounter() % lumberResources.get();

        bankTradeChecker += offeredWool.getCounter() % woolResources.get();

        bankTradeChecker += offeredGrain.getCounter() % grainResources.get();

        bankTradeChecker += offeredOre.getCounter() % oreResources.get();

        bankTradeChecker += offeredBrick.getCounter() % brickResources.get();

        if (offeredResources == requiredResources && bankTradeChecker == 0) {
            if (requiredResources == 0) {
                startBankTradeButton.setDisable(true);
            } else {
                if (playerInfo.getResources().hasResources(offered)) {
                    startBankTradeButton.setDisable(false);
                    informationLabel.setVisible(false);
                } else {
                    informationLabel.setText("Nicht ausreichend Ressourcen.");
                }
            }
        } else {
            startBankTradeButton.setDisable(true);
            informationLabel.setText("Falsches Austauschverhältnis.");
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
        bankLumber.counterProperty().set(0);
        bankWool.counterProperty().set(0);
        bankGrain.counterProperty().set(0);
        bankOre.counterProperty().set(0);
        bankBrick.counterProperty().set(0);
    }

    /**
     * Setter for the game session name
     *
     * @param gameSessionName name of the current game session
     */
    public void setGameSessionName(String gameSessionName) {
        this.gameSessionName = gameSessionName;
    }
}
