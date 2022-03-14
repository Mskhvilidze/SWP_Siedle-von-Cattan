package de.uol.swp.client.game.debug;

import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.common.game.board.DevCardEnumMap;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.debug.SetDevCardsRequest;
import de.uol.swp.common.game.debug.SetNextDiceRequest;
import de.uol.swp.common.game.debug.SetResourcesRequest;
import de.uol.swp.common.game.debug.SetStateRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

/**
 * Debug Presenter for a game session to set player resources, game state, etc
 */
@SuppressWarnings("UnstableApiUsage")
public class DebugPresenter extends AbstractGamePresenter {

    private final ObservableList<String> data = FXCollections.observableArrayList();
    @FXML
    public ComboBox<String> playerPicker;
    @FXML
    public Spinner<Integer> lumber;
    @FXML
    public Spinner<Integer> wool;
    @FXML
    public Spinner<Integer> grain;
    @FXML
    public Spinner<Integer> ore;
    @FXML
    public Spinner<Integer> brick;
    @FXML
    public Spinner<Integer> vp;
    @FXML
    public Spinner<Integer> knight;
    @FXML
    public Spinner<Integer> yearOfPlenty;
    @FXML
    public Spinner<Integer> monopoly;
    @FXML
    public Spinner<Integer> road;
    @FXML
    private Label currentStateLabel;
    @FXML
    private Spinner<Integer> diceValue;

    @FXML
    private void onStateDebugSwitch(ActionEvent actionEvent) {
        String state = ((Node) actionEvent.getSource()).getId();
        eventBus.post(new SetStateRequest(gameSessionName, state));
    }

    /**
     * Sets the names of the players that can be selected in the debug picker
     *
     * @param players the names of the players that can be selected in the debug picker
     */
    public void setPlayers(String... players) {
        Platform.runLater(() -> {
            if (!data.isEmpty()) {
                data.clear();
            }
            playerPicker.setItems(data);
            data.addAll(players);
            playerPicker.getSelectionModel().selectFirst();
        });
    }

    @FXML
    private void setResources() {
        ResourceEnumMap map = new ResourceEnumMap(lumber.getValue(), wool.getValue(), grain.getValue(), ore.getValue(), brick.getValue());
        eventBus.post(new SetResourcesRequest(gameSessionName, map, playerPicker.getValue()));
    }

    @FXML
    private void setDevCards() {
        DevCardEnumMap devCards = new DevCardEnumMap(vp.getValue(), knight.getValue(), yearOfPlenty.getValue(), monopoly.getValue(), road.getValue());
        eventBus.post(new SetDevCardsRequest(gameSessionName, devCards, playerPicker.getValue()));
    }

    @FXML
    private void onMaxResources() {
        int val = Integer.MAX_VALUE / 2;
        ResourceEnumMap map = new ResourceEnumMap(val, val, val, val, val);
        eventBus.post(new SetResourcesRequest(gameSessionName, map, playerPicker.getValue()));
    }

    /**
     * Sets the text of the state label to the given state name
     *
     * @param state the name of the current state
     */
    public void setState(String state) {
        Platform.runLater(() -> currentStateLabel.setText("Current State: " + state));
    }

    @FXML
    private void setDiceValue() {
        eventBus.post(new SetNextDiceRequest(gameSessionName, diceValue.getValue()));
    }
}
