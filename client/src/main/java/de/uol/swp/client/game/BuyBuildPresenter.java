package de.uol.swp.client.game;

import com.jfoenix.controls.JFXButton;
import de.uol.swp.client.ImageCache;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.request.BuyDevCardRequest;
import de.uol.swp.common.game.request.build.StartBuildRequest;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.Map;


/**
 * The presenter for buy or building objects. Is included in the {@link GameSessionPresenter}
 */
@SuppressWarnings("UnstableApiUsage")
public class BuyBuildPresenter extends AbstractGamePresenter {

    private final BooleanProperty menuDisabled = new SimpleBooleanProperty();
    private final IntegerProperty roadAmount = new SimpleIntegerProperty(15);
    private final IntegerProperty settlementAmount = new SimpleIntegerProperty(5);
    private final IntegerProperty cityAmount = new SimpleIntegerProperty(4);
    private final IntegerProperty cardAmount = new SimpleIntegerProperty(25);

    @FXML
    private JFXButton buildRoadButton;
    @FXML
    private JFXButton buildSettlementButton;
    @FXML
    private JFXButton buildCityButton;
    @FXML
    private JFXButton buyDevelopmentButton;
    @FXML
    private ImageView roadImageView;
    @FXML
    private ImageView settlementImageView;
    @FXML
    private ImageView cityImageView;

    @FXML
    private Label cityLabel;
    @FXML
    private Label settlementLabel;
    @FXML
    private Label roadLabel;
    @FXML
    private Label devCardLabel;

    /**
     * Sets info about the game session
     *
     * @param inventory the {@code InventoryPresenter} of this game session
     */
    public void setInfo(InventoryPresenter inventory) {
        BooleanBinding canNotBuildRoad = inventory.lumberProperty().lessThan(1).or(inventory.brickProperty().lessThan(1))
                .or(roadAmount.lessThan(1)).or(menuDisabled);
        buildRoadButton.disableProperty().bind(canNotBuildRoad);
        BooleanBinding canNotBuildSettlement = inventory.lumberProperty().lessThan(1).or(inventory.brickProperty().lessThan(1))
                .or(inventory.grainProperty().lessThan(1)).or(inventory.woolProperty().lessThan(1)).or(settlementAmount.lessThan(1)).or(menuDisabled);
        buildSettlementButton.disableProperty().bind(canNotBuildSettlement);
        BooleanBinding canNotBuildCity = inventory.grainProperty().lessThan(2).or(inventory.oreProperty().lessThan(3)).or(cityAmount.lessThan(1)).or(
                menuDisabled);
        buildCityButton.disableProperty().bind(canNotBuildCity);
        BooleanBinding canNotBuyCard = inventory.grainProperty().lessThan(1).or(inventory.woolProperty().lessThan(1))
                .or(inventory.oreProperty().lessThan(1)).or(cardAmount.lessThan(1)).or(menuDisabled);
        buyDevelopmentButton.disableProperty().bind(canNotBuyCard);
        String suffix = " verfügbar.";
        roadLabel.textProperty().bind(Bindings.concat("Straße, Sie haben noch ", roadAmount, suffix));
        settlementLabel.textProperty().bind(Bindings.concat("Siedlung, Sie haben noch ", settlementAmount, suffix));
        cityLabel.textProperty().bind(Bindings.concat("Stadt, Sie haben noch ", cityAmount, suffix));
        devCardLabel.textProperty().bind(Bindings.concat("Entwicklungskarte, es gibt noch ", cardAmount));

        PlayerColor color = PlayerColor.BLUE;
        for (PlayerDTO playerDTO : players) {
            if (playerDTO.getPlayerName().equals(userInfo.getLoggedInUser().getUsername())) {
                color = playerDTO.getColor();
            }
        }
        roadImageView.setImage(ImageCache.getImage("objects/road_" + color.name().toLowerCase() + ".png"));
        settlementImageView.setImage(ImageCache.getImage("objects/settlement_" + color.name().toLowerCase() + ".png"));
        cityImageView.setImage(ImageCache.getImage("objects/city_" + color.name().toLowerCase() + ".png"));
    }

    /**
     * Method called when the build road button is pressed
     * <p>
     * This methods posts a new {@link StartBuildRequest} to the EventBus
     */
    @FXML
    public void onBuildRoadButton() {
        eventBus.post(new StartBuildRequest(gameSessionName, PieceType.ROAD));
    }

    /**
     * Method called when the build settlement button is pressed
     * <p>
     * This methods posts a new {@link StartBuildRequest} to the EventBus
     */
    @FXML
    public void onBuildSettlementButton() {
        eventBus.post(new StartBuildRequest(gameSessionName, PieceType.SETTLEMENT));
    }

    /**
     * Method called when the build city button is pressed
     * <p>
     * This methods posts a new {@link StartBuildRequest} to the EventBus
     */
    @FXML
    public void onBuildCityButton() {
        eventBus.post(new StartBuildRequest(gameSessionName, PieceType.CITY));
    }

    /**
     * Sets whether all buy and build buttons in this menu should be disabled
     *
     * @param disable whether all buy and build buttons in this menu should be disabled
     */
    public void setMenuDisable(boolean disable) {
        menuDisabled.set(disable);
    }

    /**
     * Method called when the buy development button is pressed
     * <p>
     * This methods posts a new {@link BuyDevCardRequest} to the EventBus
     */
    @FXML
    private void onBuyDevelopmentButton() {
        eventBus.post(new BuyDevCardRequest(gameSessionName));
    }

    /**
     * Sets the amount of DevCardsRemaining
     *
     * @param amount amount of remaining in the game
     */
    public void setDevCardRemaining(int amount) {
        Platform.runLater(() -> cardAmount.set(amount));
    }

    /**
     * Sets the amount of remaining buildable pieces
     *
     * @param pieceType map containing the updated information
     */
    public void setRemainingBuildables(Map<PieceType, Integer> pieceType) {
        Platform.runLater(() -> {
            roadAmount.set(pieceType.get(PieceType.ROAD));
            settlementAmount.set(pieceType.get(PieceType.SETTLEMENT));
            cityAmount.set(pieceType.get(PieceType.CITY));
        });
    }
}
