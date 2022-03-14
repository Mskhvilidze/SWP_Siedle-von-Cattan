package de.uol.swp.client.lobby;

import com.google.common.eventbus.Subscribe;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.main.event.ReturnToMainMenuViewEvent;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.message.LobbyCreatedMessage;
import de.uol.swp.common.lobby.message.LobbyDroppedMessage;
import de.uol.swp.common.lobby.message.LobbyListUpdatedMessage;
import de.uol.swp.common.lobby.request.LobbyInformationRequest;
import de.uol.swp.common.lobby.response.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages the select lobby Menu
 *
 * @see de.uol.swp.client.AbstractPresenter
 * @since 2020-11-26
 */
@SuppressWarnings("UnstableApiUsage")
public class SelectLobbyPresenter extends AbstractPresenter {

    public static final String FXML = "/fxml/SelectLobbyView.fxml";
    private static final Logger LOG = LogManager.getLogger(SelectLobbyPresenter.class);

    private ObservableList<LobbyData> lobbies;
    private ObservableList<String> lobbyUsers;

    @FXML
    private TableView<LobbyData> lobbyTableView;
    @FXML
    private TableColumn<LobbyData, String> lobbyViewColumn;
    @FXML
    private TableColumn<LobbyData, String> lobbyPlayerColumn;
    @FXML
    private TableColumn<LobbyData, String> lobbyJoinableColumn;
    @FXML
    private TextField createLobbyField;
    @FXML
    private CheckBox privateCheckbox;
    @FXML
    private TextField joinLobbyField;
    @FXML
    private Pane lobbyInformationPane;
    @FXML
    private JFXTextField lobbyNameTextField;
    @FXML
    private JFXTextField lobbyOwnerTextLabel;
    @FXML
    private JFXListView<String> lobbyPlayersListView;
    @FXML
    private JFXTextField lobbyReadyTextField;
    @FXML
    private JFXTextField lobbyDebugTextField;
    @FXML
    private JFXTextField lobbyTimerTextField;
    @FXML
    private JFXTextField lobbyVictoryPointsTextField;
    @FXML
    private JFXButton joinClickedLobbyButton;

    /**
     * Initializes the lobbies list
     */
    public void initialize() {
        lobbies = FXCollections.observableArrayList();
        lobbyTableView.setItems(lobbies);
        lobbyViewColumn.setCellValueFactory(lobby -> new SimpleStringProperty(lobby.getValue().getName()));
        lobbyPlayerColumn.setCellValueFactory(
                lobby -> new SimpleStringProperty(lobby.getValue().getNumberOfPlayers() + " / " + lobby.getValue().getLobbySize()));
        lobbyJoinableColumn.setCellValueFactory(lobby -> new SimpleStringProperty(lobby.getValue().isLobbyJoinableString()));
    }

    /**
     * Method called when the create lobby button is pressed
     * <p>
     * If the create lobby button is pressed, this method requests the lobby service
     * to create a new lobby. It uses the lobby name from the textfield createLobbyField and the loggedInUser.
     * It also sets the boolean isPrivate in LobbyService to true or false, depending on the checkbox privateCheckbox.
     *
     * @param event the ActionEvent created by pressing the create lobby button
     * @see LobbyService
     * @since 2019-11-20
     */
    @FXML
    private void onCreateLobby(ActionEvent event) {
        if (!createLobbyField.getText().isBlank()) {
            lobbyService.createNewLobby(createLobbyField.getText(), UserDTO.create(userInfo.getLoggedInUser()), privateCheckbox.isSelected());
        }
    }

    /**
     * Method called when the join lobby button is pressed
     * <p>
     * If the join lobby button is pressed, this method requests the lobby service
     * to join a specified lobby. Therefore it currently uses the lobby name "test"
     * and an user called "ich"
     *
     * @param event the ActionEvent created by pressing the join lobby button
     * @see LobbyService
     * @since 2019-11-20
     */
    @FXML
    private void onJoinLobby(ActionEvent event) {
        lobbyService.joinLobby(joinLobbyField.getText());
    }

    /**
     * Method called when clicked with left mouse button on the lobbyTableView
     * <p>
     * If clicked once with the left mouse button on the lobbyTableView,
     * this method calls the lobbyInformationRequest method with the name of the clicked lobby
     * <p>
     * If double clicked with the left mouse button on the lobbyTableView,
     * this method calls the onJoinLobby method with the name of the clicked lobby
     *
     * @param event the MouseEvent created by clicking on the lobbyTableView
     * @since 2021-05-22
     */
    @FXML
    public void joinSelectedLobbyClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && lobbyTableView.getSelectionModel().getSelectedItem() != null) {
            if (event.getClickCount() == 1) {
                LobbyInformationRequest lobbyInformationRequest = new LobbyInformationRequest(
                        lobbyTableView.getSelectionModel().getSelectedItem().getName());
                eventBus.post(lobbyInformationRequest);
            } else if (event.getClickCount() == 2) {
                lobbyService.joinLobby(lobbyTableView.getSelectionModel().getSelectedItem().getName());
            }
        }
    }

    /**
     * Method called when the join lobby button is pressed
     *
     * @param event the MouseEvent created by clicking on the JoinClickedLobbyButton
     */
    @FXML
    public void onJoinClickedLobby(ActionEvent event) {
        lobbyService.joinLobby(lobbyNameTextField.getText());
    }

    /**
     * Shows the user an information pane to the clicked lobby if a new LobbyInformationResponse object
     * is posted to the EventBus.
     *
     * @param response the LobbyInformationResponse object seen on the EventBus
     */
    @Subscribe
    public void onLobbyInformation(LobbyInformationResponse response) {
        Lobby lobby = response.getLobby();
        Platform.runLater(() -> {
            lobbyNameTextField.setText(lobby.getName());
            lobbyOwnerTextLabel.setText(lobby.getOwner().getUsername());
            if (lobbyUsers == null) {
                lobbyUsers = FXCollections.observableArrayList();
                lobbyPlayersListView.setItems(lobbyUsers);
            }
            lobbyUsers.clear();
            lobby.getUsers().forEach(u -> lobbyUsers.add(u.getUsername()));
            for (int i = 0; i < lobby.getNoOfBots(); i++) {
                lobbyUsers.add("Bot");
            }
            if (lobby.getReadyUsers().size() == 1) {
                lobbyReadyTextField.setText(lobby.getReadyUsers().size() + " Spieler ist bereit.");
            } else {
                lobbyReadyTextField.setText(lobby.getReadyUsers().size() + " Spieler sind bereit.");
            }
            if (lobby.isDebugEnabled()) {
                lobbyDebugTextField.setText("aktiviert");
            } else {
                lobbyDebugTextField.setText("deaktiviert");
            }
            lobbyTimerTextField.setText((lobby.getTimerDuration() + " Sekunden"));
            lobbyVictoryPointsTextField.setText(lobby.getNumVP() + " SiegPunkte");
            joinClickedLobbyButton.setText(lobbyNameTextField.getText() + " beitreten");
            lobbyInformationPane.setVisible(true);
        });
    }

    /**
     * Handles if a Lobby is not found
     * <p>
     * If a new LobbyNotFoundResponse object is posted to the EventBus and its userName attribute
     * equals the userName attribute of the loggedInUser object, an error message that the lobby
     * is not found will be displayed on the console and the user will also get a graphical error message.
     *
     * @param lobbyNotFoundResponse the LobbyNotFoundResponse object seen on the EventBus
     * @author Jannes Weyher
     * @see LobbyNotFoundResponse
     * @since 2020-11-15
     */
    @Subscribe
    public void onLobbyNotFoundResponse(LobbyNotFoundResponse lobbyNotFoundResponse) {
        Platform.runLater(() -> {
            LOG.debug("Lobby not found");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lobby konnte nicht gefunden werden.");
            alert.showAndWait();
        });
    }

    /**
     * Handles if Lobby if full
     * <p>
     * If a new LobbyFullResponse object is posted to the EventBus and its userName attribute
     * equals the userName attribute of the loggedInUser object, an error message that the lobby
     * is full will be displayed on the console and the user will also get a graphical error message.
     *
     * @param lobbyFullResponse the LobbyFullResponse object seen on the Eventbus
     * @see LobbyFullResponse
     */
    @Subscribe
    public void onLobbyFullResponse(LobbyFullResponse lobbyFullResponse) {
        Platform.runLater(() -> {
            LOG.debug("Lobby is full");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Die Lobby ist voll. ");
            alert.showAndWait();
        });
    }

    /**
     * Handles if LobbyName is already taken
     * <p>
     * If a new LobbyNameAlreadyTakenResponse object is posted to the EventBus
     * an error message that the lobbyName is already taken will be displayed on the console
     * and the user will also get a graphical error message.
     *
     * @param lobbyNameAlreadyTakenResponse the LobbyNameAlreadyTakenResponse object seen on the Eventbus
     * @see LobbyNameAlreadyTakenResponse
     */
    @Subscribe
    public void onLobbyNameAlreadyTakenResponse(LobbyNameAlreadyTakenResponse lobbyNameAlreadyTakenResponse) {
        Platform.runLater(() -> {
            LOG.debug("Lobbyname {} is already taken", lobbyNameAlreadyTakenResponse.getLobbyName());
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Lobbyname " + lobbyNameAlreadyTakenResponse.getLobbyName() + " ist bereits vergeben. ");
            alert.showAndWait();
        });
    }

    /**
     * This Method is called when the main menu button is pressed.
     * It changes the scene to the main menu presenter
     */
    @FXML
    private void onMainMenuButtonPressed(ActionEvent event) {
        ReturnToMainMenuViewEvent returnToMainMenuViewEvent = new ReturnToMainMenuViewEvent();
        eventBus.post(returnToMainMenuViewEvent);
    }

    /**
     * When a new lobby is created this method adds the new lobby to the existing list of lobbies
     * <p>
     * Posts a RetrieveLobbyListRequest on the bus to update the lobbies list
     *
     * @param lobbyCreatedMessage the LobbyCreatedMessage which is send when a new Lobby is created.
     */
    @Subscribe
    public void onLobbyCreatedMessage(LobbyCreatedMessage lobbyCreatedMessage) {
        lobbies.add(create(lobbyCreatedMessage.getLobby()));
    }

    /**
     * When a new lobby is dropped this method deletes the lobby in the list of lobbies
     * <p>
     * Posts a RetrieveLobbyListRequest on the bus to update the lobbies list
     *
     * @param lobbyDroppedMessage the LobbyDroppedMessage which is send when a Lobby is dropped
     */
    @Subscribe
    public void onLobbyDroppedMessage(LobbyDroppedMessage lobbyDroppedMessage) {
        lobbies.remove(create(lobbyDroppedMessage.getLobby()));
    }

    /**
     * Handles if the User already joined this lobby
     * <p>
     * If a new LobbyAlreadyJoinedResponse object is posted to the EventBus, an error message that the user
     * already joined this Lobby will be displayed on the console and the user will also get a graphical error message.
     *
     * @param lobbyAlreadyJoinedResponse the LobbyAlreadyJoinedResponse object seen on the Eventbus
     * @see LobbyAlreadyJoinedResponse
     */
    @Subscribe
    public void onLobbyAlreadyJoinedResponse(LobbyAlreadyJoinedResponse lobbyAlreadyJoinedResponse) {
        Platform.runLater(() -> {
            LOG.debug("Lobby already joined");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Sie sind dieser Lobby bereits beigetreten.");
            alert.showAndWait();
        });
    }

    /**
     * Handles if the Game already started
     * <p>
     * If a new GameAlreadyStartedResponse object is posted to the EventBus, an error message that the game
     * already started will be displayed on the console and the user will also get a graphical error message.
     *
     * @param gameAlreadyStartedResponse the GameAlreadyStartedResponse object seen on the Eventbus
     * @see GameAlreadyStartedResponse
     */

    @Subscribe
    public void onGameAlreadyStartedResponse(GameAlreadyStartedResponse gameAlreadyStartedResponse) {
        Platform.runLater(() -> {
            LOG.debug("Game already started");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Das Spiel ist bereits gestartet.");
            alert.showAndWait();
        });
    }

    /**
     * If a LobbyListUpdatedMessage is posted on the bus this method reacts
     * <p>
     * The method receives the list of lobbies in the message and updates the
     * lists of lobbies for the SelectLobbyPresenter.
     *
     * @param message a LobbyListUpdatedMessage which includes the list of Lobbies from the server
     */
    @Subscribe
    public void onLobbyListUpdatedMessage(LobbyListUpdatedMessage message) {
        List<LobbyData> lobbyList = message.getLobbies().stream().map(this::create).collect(Collectors.toList());
        updateLobbiesList(lobbyList);
    }

    /**
     * If a RetrieveLobbyListResponse is posted on the bus this method reacts
     * <p>
     * The method receives the list of lobbies in the message and updates the
     * lists of lobbies for the SelectLobbyPresenter.
     *
     * @param response a RetrieveLobbyListResponse which includes the list of Lobbies from the server
     */
    @Subscribe
    public void onRetrieveLobbyListResponse(RetrieveLobbyListResponse response) {
        List<LobbyData> lobbyList = response.getLobbies().stream().map(this::create).collect(Collectors.toList());
        updateLobbiesList(lobbyList);
    }

    /**
     * Updates the list of lobbies for the SelectLobbyPresenter according
     * to the list given
     * <p>
     * This method clears the entire lobbies list and then adds the name of each
     * lobby in the list given to the SelectLobbyPresenter lobbies list.
     * If there is no lobbies list this will create one.
     *
     * @param lobbyList a List of Strings which is used to update the "lobbies" list.
     */
    private void updateLobbiesList(List<LobbyData> lobbyList) {
        Platform.runLater(() -> {
            lobbies.clear();
            lobbies.addAll(lobbyList);
            lobbies.sort(LobbyData::compareTo);
            if (lobbyList.stream().noneMatch(o -> o.getName().equals(this.lobbyNameTextField.getText()))) {
                this.lobbyInformationPane.setVisible(false);
            }
        });
    }

    /**
     * Method to transform lobbies in a LobbyData Object.
     *
     * @param lobby the lobby which is transformed into a LobbyData Object
     * @return a LobbyData Object
     */
    private LobbyData create(Lobby lobby) {
        return new LobbyData(lobby.getName(), lobby.getUsers().size() + lobby.getNoOfBots(), lobby.getLobbySize(), lobby.isLobbyJoinable());
    }

    /**
     * This is a help-class which is used to show the Lobbies and Players
     * in the Table View.
     */
    private static class LobbyData implements Comparable<LobbyData> {

        private final String name;
        private final int numberOfPlayers;
        private final int lobbySize;
        private final boolean lobbyJoinable;

        /**
         * Constructor
         *
         * @param name            name of the lobby
         * @param numberOfPlayers number of Players in the lobby
         * @param lobbySize       number of max Players in the lobby
         */
        public LobbyData(String name, int numberOfPlayers, int lobbySize, boolean lobbyJoinable) {
            this.name = name;
            this.numberOfPlayers = numberOfPlayers;
            this.lobbySize = lobbySize;
            this.lobbyJoinable = lobbyJoinable;
        }

        public String getName() {
            return name;
        }

        public int getNumberOfPlayers() {
            return numberOfPlayers;
        }

        public int getLobbySize() {
            return lobbySize;
        }

        public boolean isLobbyJoinable() {
            return lobbyJoinable;
        }

        /**
         * if a player is able to join the lobby, this method returns "ja" if lobbyJoinable is true
         *
         * @return "ja" when the lobby is joinable, "nein" when the lobby is full
         */
        public String isLobbyJoinableString() {
            if (isLobbyJoinable()) {
                return "ja";
            }
            return "nein";
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            LobbyData lobbyData = (LobbyData) object;
            return Objects.equals(name, lobbyData.name);
        }

        @Override
        public int compareTo(LobbyData lobbyData) {
            return this.name.compareTo(lobbyData.getName());
        }
    }

}

