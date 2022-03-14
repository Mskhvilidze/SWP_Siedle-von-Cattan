package de.uol.swp.client.game.popup;

import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.common.game.board.DevCard;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class CardPresenter extends AbstractPresenter {

    private final StringProperty playerName = new SimpleStringProperty();
    private final ObjectProperty<DevCard> devCard = new SimpleObjectProperty<>();
    @FXML
    private ImageView knightImage;
    @FXML
    private ImageView roadImage;
    @FXML
    private ImageView yearOfPlentyImage;
    @FXML
    private ImageView monopolyImage;
    @FXML
    private Label label;

    public void initialize() {
        label.textProperty().bind(playerName.concat(" benutzt"));
        knightImage.visibleProperty().bind(devCard.isEqualTo(DevCard.KNIGHT));
        roadImage.visibleProperty().bind(devCard.isEqualTo(DevCard.ROAD_BUILDING));
        yearOfPlentyImage.visibleProperty().bind(devCard.isEqualTo(DevCard.YEAR_OF_PLENTY));
        monopolyImage.visibleProperty().bind(devCard.isEqualTo(DevCard.MONOPOLY));
    }

    public void updateInfo(String playerName, DevCard devCard) {
        Platform.runLater(() -> {
            this.playerName.set(playerName);
            this.devCard.set(devCard);
        });
    }
}
