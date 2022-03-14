package de.uol.swp.client.lobby;

import com.google.common.eventbus.Subscribe;
import com.jfoenix.controls.*;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.chat.ChatPresenter;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyOption;
import de.uol.swp.common.lobby.dto.LobbyOptions;
import de.uol.swp.common.lobby.request.AddBotToLobbyRequest;
import de.uol.swp.common.lobby.request.RemoveBotFromLobbyRequest;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.message.ChangedUserInfoMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the lobby menu
 *
 * @see de.uol.swp.client.AbstractPresenter
 */
public class LobbyPresenter extends AbstractPresenter {
    public static final String FXML = "/fxml/LobbyView.fxml";
    private static final Logger LOG = LogManager.getLogger(LobbyPresenter.class);

    private GameLobby lobby;

    /**
     * Used to sort the lobby list: Owner at the beginning then alphabetical order
     */
    private final Comparator<String> usernameComparator = (s1, s2) -> {
        if (s1.equals(lobby.getOwner().getUsername())) {
            return -1;
        } else if (s2.equals(lobby.getOwner().getUsername())) {
            return 1;
        } else {
            return s1.compareTo(s2);
        }
    };

    private ObservableList<String> users;
    private User loggedInUser;
    private boolean adminRights;
    @FXML
    private Pane root;
    @FXML
    private Label lobbyNameLabel;
    @FXML
    private JFXListView<String> lobbyUsersView;
    @FXML
    private Label readyLabel;
    @FXML
    private JFXCheckBox readyCheckbox;
    @FXML
    private Pane invisibleScene;
    @FXML
    private JFXButton leaveButton;
    @FXML
    private ChatPresenter chatViewController;
    @FXML
    private JFXButton startGameButton;
    //Option Controls
    @FXML
    private Region optionVeil;
    @FXML
    private JFXToggleButton togglePrivateButton;
    @FXML
    private JFXToggleButton toggleDebugButton;
    @FXML
    private JFXComboBox<Integer> victoryPointsPicker;
    @FXML
    private JFXComboBox<Integer> lobbySizePicker;
    @FXML
    private JFXSlider timerSlider;
    @FXML
    private JFXButton addBotButton;
    @FXML
    private JFXButton rejoinGameButton;

    /**
     * Automatically called during initialization
     * <p>
     * Formats the time slider to min:sec and sets the entries of the victory points combo box.
     */
    public void initialize() {
        timerSlider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double object) {
                return String.format("%02d:%02d", object.intValue() * 5 / 60, object.intValue() * 5 % 60);
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });

        timerSlider.setOnMouseReleased(event -> changeTimer());

        List<Integer> list = victoryPointsPicker.getItems();
        for (int i = LobbyOptions.NUM_VICTORY_POINTS.getMin(); i <= LobbyOptions.NUM_VICTORY_POINTS.getMax(); i++) {
            list.add(i);
        }
        list = lobbySizePicker.getItems();
        for (int i = LobbyOptions.LOBBY_SIZE.getMin(); i <= LobbyOptions.LOBBY_SIZE.getMax(); i++) {
            list.add(i);
        }
        Platform.runLater(() -> root.requestFocus());
        root.setOnMousePressed(e -> root.requestFocus());
        lobbyUsersView.setCellFactory(lv -> new ListCellWithCtxMenu());
    }

    /**
     * Initializes the option nodes
     */
    private void initOptions() {
        victoryPointsPicker.setValue(lobby.getNumVP());
        togglePrivateButton.setSelected(lobby.isPrivateLobby());
        toggleDebugButton.setSelected(lobby.isDebugEnabled());
        lobbySizePicker.setValue(lobby.getLobbySize());
        lobbyUsersView.setPrefHeight((double) lobby.getLobbySize() * 100 + 1);
        updateTimer(lobby.getTimerDuration());
    }

    /**
     * Sets the correct username and lobbyname in the embeddedChatPresenter.
     *
     * @param lobby the lobby the chat is in
     */
    public void setChatLobby(Lobby lobby) {
        chatViewController.setCurrentLobby(lobby.getName());
    }

    /**
     * Returns the ChatPresenter of this lobby. Used to forward the chat of this lobby to its game session
     *
     * @return the ChatPresenter of this lobby
     */
    public ChatPresenter getChatPresenter() {
        return chatViewController;
    }

    /**
     * Updates the lobby owner of the this lobby and sets whether this client has admin rights
     *
     * @param newOwner the new owner of the lobby
     */
    public void updateOwner(User newOwner) {
        if (newOwner.equals(loggedInUser)) {
            adminRights = true;
            optionVeil.setVisible(false);
            addBotButton.setVisible(true);
        } else {
            adminRights = false;
            optionVeil.setVisible(true);
            addBotButton.setVisible(false);
        }
        lobby.updateOwner(newOwner);

        lobbyUsersView.setCellFactory(lv -> new ListCellWithCtxMenu());
        if (users != null) {
            Platform.runLater(this::sortLobbyUserList);
        }
    }

    /**
     * One-time initializer for the lobby object and the option nodes
     *
     * @param lobby the lobby this presenter is connected to
     */
    public void setLobby(Lobby lobby) {
        if (this.lobby == null) {
            this.lobby = GameLobby.create(lobby);
            initOptions();
            updateOwner(lobby.getOwner());
            updateLobbyReadyStatus();
            initiateRejoinButton(lobby.isGameStarted());
        } else {
            throw new UnsupportedOperationException("Lobby has already been set");
        }
    }

    /**
     * Sets the currently logged in user
     *
     * @param user the user that is currently logged in
     */
    public void setUser(User user) {
        this.loggedInUser = user;
    }

    /**
     * if the checkbox is clicked the setUserReady method of lobbyService is called, which marks the user as ready
     * if the checkbox is unclicked the setUserNotReady method of lobbyService is called, which marks the user as NOT ready
     */
    @FXML
    private void onCheckboxChanged() {
        lobbyService.setUserReady(lobby.getName(), readyCheckbox.isSelected());
    }

    // -------------------------------------------------------------------------------
    // Lobby list Methods
    // -------------------------------------------------------------------------------

    /**
     * Initializes the user list view according to the {@link #lobby} user list
     * <p>
     * This method adds the name of each user in the {@link #lobby} user list to the user list view and then sorts
     * the list view.
     */
    public void initLobbyUserList() {
        if (users == null) {
            Platform.runLater(() -> {
                users = FXCollections.observableArrayList(
                        lobby.getUsers().stream().map(User::getUsername).collect(Collectors.toList()));
                for (int i = 0; i < lobby.getNoOfBots(); i++) {
                    users.add("Bot");
                }
                users.sort(usernameComparator);
                lobbyUsersView.setItems(users);
            });
        }
    }

    /**
     * This method controls the lobbySizePicker
     */
    public void setLobbySizePicker() {
        lobbySizePicker.setDisable(lobby.getLobbySize() == 4 && lobby.getLobbySize() == lobby.getUsers().size() + lobby.getNoOfBots());
    }

    /**
     * Adds a bot to the lobby user list and updates the lobby list in SelectLobbyPresenter
     * This method also checks if the lobby can be started
     */
    public void addBot() {
        Platform.runLater(() -> {
            users.add("Bot");
            lobby.addBot();
            setLobbySizePicker();
            updateLobbyReadyStatus();
        });
    }

    /**
     * Removes a bot from the lobby user list and updates the lobby list in SelectLobbyPresenter
     * This method also checks if the lobby can be started
     */
    public void removeBot() {
        Platform.runLater(() -> {
            users.remove("Bot");
            lobby.removeBot();
            setLobbySizePicker();
            updateLobbyReadyStatus();
        });
    }

    /**
     * Adds a given user to the {@link Lobby#getUsers() lobby user list} and the user list view.
     * Sorts the list view.
     *
     * @param user the user to be added to the lobby
     */
    public void addLobbyUser(User user) {
        lobby.joinUser(user);
        Platform.runLater(() -> {
            users.add(user.getUsername());
            sortLobbyUserList();
            setLobbySizePicker();
        });
        updateLobbyReadyStatus();
    }

    /**
     * Removes a given user from the {@link Lobby#getUsers() lobby user list} and the user list view.
     * Sorts the list view.
     *
     * @param user the user to be removed from the lobby
     */
    public void removeLobbyUser(User user) {
        LOG.debug("LobbyUser: {} left lobby {}", user.getUsername(), lobby.getName());
        lobby.leaveUser(user);
        if (lobby.getOwner().equals(loggedInUser)) {
            adminRights = true;
            optionVeil.setVisible(false);
            addBotButton.setVisible(true);
        }
        Platform.runLater(() -> {
            users.remove(user.getUsername());
            sortLobbyUserList();
            setLobbySizePicker();
        });
        updateLobbyReadyStatus();
    }

    /**
     * Sets a user to ready or not, depending on the given boolean ready
     * updates the ready status of the lobby
     *
     * @param user the user to be removed from the lobby
     */
    public void setUserReady(User user, boolean ready) {
        lobby.setUserReady(user, ready);
        updateLobbyReadyStatus();
    }

    /**
     * Sorts the list view with {@link #usernameComparator}.
     */
    private void sortLobbyUserList() {
        users.sort(usernameComparator);
    }

    // -------------------------------------------------------------------------------
    // Update Methods
    // -------------------------------------------------------------------------------

    /**
     * Updates the given lobby option with the given value
     * If the lobbySizePicker is changed, this method also updates if the lobby can be started
     *
     * @param option   the {@code LobbyOption} that should be updated
     * @param newValue the value that the option should be set to
     * @param <T>      the type of the value, specified by the {@code LobbyOption}
     * @see LobbyOption
     */
    public <T extends Serializable> void updateLobby(LobbyOption<T> option, T newValue) {
        lobby.updateLobby(option, newValue);
        if (option.equals(LobbyOptions.PRIVATE_LOBBY)) {
            LOG.debug("Lobby privacy changed");
            togglePrivateButton.setSelected((Boolean) newValue);
        } else if (option.equals(LobbyOptions.DEBUG)) {
            LOG.debug("Lobby debug changed");
            toggleDebugButton.setSelected((Boolean) newValue);
        } else if (option.equals(LobbyOptions.LOBBY_SIZE)) {
            LOG.debug("Lobby size changed");
            Platform.runLater(() -> lobbySizePicker.setValue((Integer) newValue));
            lobbyUsersView.setPrefHeight(Double.valueOf((Integer) newValue) * 100 + 1);
            updateLobbyReadyStatus();
        } else if (option.equals(LobbyOptions.NUM_VICTORY_POINTS)) {
            LOG.debug("Number of Victory Points changed");
            Platform.runLater(() -> victoryPointsPicker.setValue((Integer) newValue));
        } else if (option.equals(LobbyOptions.TIMER_DURATION)) {
            LOG.debug("Timer duration changed");
            updateTimer((Integer) newValue);
        } else {
            throw new IllegalStateException("If statement should account for " + option);
        }
    }

    /**
     * Updates the timer by formatting the given duration in seconds
     *
     * @param value the duration of the timer in seconds
     */
    private void updateTimer(Integer value) {
        double timer = value.doubleValue() / 5;
        timerSlider.setValue(timer);
    }

    /**
     * initiates the rejoin button. Sets the visibility of the rejoin-button depending on if the game has already started
     *
     * @param gameHasStarted the boolean that show if the game has already started
     */
    public void initiateRejoinButton(boolean gameHasStarted) {
        rejoinGameButton.setVisible(gameHasStarted);
    }

    // -------------------------------------------------------------------------------
    // FXML Methods
    // -------------------------------------------------------------------------------

    /**
     * Method called when an entry from the victory points combo box has been selected
     */
    @FXML
    private void selectVictoryPoints() {
        lobbyService.updateLobby(lobby.getName(), LobbyOptions.NUM_VICTORY_POINTS, victoryPointsPicker.getValue());
    }

    /**
     * Method called when an entry from the lobby size combo box has been selected
     */
    @FXML
    private void selectLobbySize() {
        lobbyService.updateLobby(lobby.getName(), LobbyOptions.LOBBY_SIZE, lobbySizePicker.getValue());
    }

    /**
     * Method called when the private button has been toggled
     */
    @FXML
    private void togglePrivate() {
        lobbyService.updateLobby(lobby.getName(), LobbyOptions.PRIVATE_LOBBY, togglePrivateButton.isSelected());
    }

    /**
     * Method called when the debug button has been toggled
     */
    @FXML
    private void toggleDebug() {
        lobbyService.updateLobby(lobby.getName(), LobbyOptions.DEBUG, toggleDebugButton.isSelected());
    }

    /**
     * Method called when the head of the slider has been moved
     */
    @FXML
    private void changeTimer() {
        int timeInSeconds = (int) timerSlider.getValue() * 5;
        lobbyService.updateLobby(lobby.getName(), LobbyOptions.TIMER_DURATION, timeInSeconds);
    }

    /**
     * Method called when the leave button is pressed
     * <p>
     * After pressing the button, the user gets removed from the lobby via the lobby service.
     * Should the user be the last user in the lobby they will instead be informed that the lobby will be deleted.
     *
     * @see LobbyService
     */
    @FXML
    private void onLeaveButtonPressed() {
        if (users.size() == 1) {
            invisibleScene.setVisible(true);
        } else {
            lobbyService.leaveLobby(lobby.getName());
        }
    }

    /**
     * Method called when the button that confirms the deletion of the lobby is pressed
     * <p>
     * After pressing the button, the lobby gets deleted via the lobby service.
     *
     * @see LobbyService
     */
    @FXML
    private void onConfirmLeaveButtonPressed() {
        lobbyService.leaveLobby(lobby.getName());
    }

    /**
     * Method called when the button that cancels the deletion of the lobby is pressed
     * <p>
     * It closes the popup.
     */
    @FXML
    private void onCancelLeaveButtonPressed() {
        invisibleScene.setVisible(false);
    }

    @FXML
    private void addBotButtonPressed() {
        if (adminRights) {
            eventBus.post(new AddBotToLobbyRequest(lobby.getName()));
        }
    }

    /**
     * Method called to update the Label that shows how many Users are ready and if the lobby can be started
     */
    private void updateLobbyReadyStatus() {
        if (!lobby.canLobbyBeStarted()) {
            Platform.runLater(() -> readyLabel.setText(
                    lobby.getReadyUsers().size() + "/" + lobby.getUsers().size() + " Spieler sind bereit. \nSpiel kann nicht gestartet werden!")
            );
            startGameButton.setDisable(true);

        } else {
            Platform.runLater(() ->
                    readyLabel.setText(
                            lobby.getReadyUsers().size() + "/" + lobby.getUsers().size() + " Spieler sind bereit. \nSpiel kann gestartet werden!")
            );
            startGameButton.setDisable(false);
        }
    }

    /**
     * Method called when the start button is pressed
     * <p>
     * After pressing the button, the game session will be started
     */
    @FXML
    public void onStartButtonPressed() {
        if (lobby.canLobbyBeStarted() && adminRights) {
            lobbyService.startGameSession(lobby.getName());
        }
    }

    /**
     * Handles ChangedUserInfoMessage found on the EventBus.
     * <p>
     * The old user will be replaced by the user whose name was changed
     *
     * @param response ChangedUserInfoMessage found on the EventBus
     * @see ChangedUserInfoMessage
     */
    @Subscribe
    public void onChangedUserName(ChangedUserInfoMessage response) {
        Platform.runLater(() -> {
            int index = users.indexOf(response.getOldUsername());
            users.set(index, response.getUserDTO().getUsername());
        });
    }

    /**
     * This Method is called when the user pressed the rejoin-button
     * <p>
     * it calls the rejoinGameSession-method of LobbyServise
     *
     * @see LobbyService
     */
    @FXML
    public void onRejoinGame() {
        lobbyService.rejoinGameSession(lobby.getName());
    }

    /**
     * this method is called if a user can not rejoin a lobby
     *
     * @param reason the reason as string why the user can not rejoin the lobby
     */
    public void canNotRejoin(String reason) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, reason);
            alert.showAndWait();
        });
    }

    /**
     * Custom ListCell for the lobby user list
     */
    @SuppressWarnings({"java:S110", "PMD.AccessorMethodGeneration"})
    private class ListCellWithCtxMenu extends TextFieldListCell<String> {
        private final ContextMenu contextMenu;
        private final Image ownerImg = new Image("/graphics/etc/owner.png");
        private final ImageView ownerImgV = new ImageView(ownerImg);
        private final Image memberImg = new Image("/graphics/etc/member.png");
        private final ImageView memberImgV = new ImageView(memberImg);
        private final Image botImg = new Image("/graphics/etc/bot.png");
        private final ImageView botImgV = new ImageView(botImg);


        /**
         * Constructor
         */
        public ListCellWithCtxMenu() {
            contextMenu = new ContextMenu();
            Label lbl = new Label("Zum Eigentümer machen.");
            Label lbl2 = new Label("Spieler rauswerfen.");
            CustomMenuItem owner = new CustomMenuItem(lbl);
            CustomMenuItem kick = new CustomMenuItem(lbl2);
            if (!"Bot".equals(getItem())) {
                contextMenu.getItems().add(owner);
            }
            contextMenu.getItems().add(kick);
            lbl.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    makeOwner(getItem());
                }
            });
            lbl2.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    kickPlayer(getItem());
                }
            });
        }

        private void makeOwner(String username) {
            Optional<User> user = lobby.getUsers().stream().filter(u -> u.getUsername().equals(username)).findFirst();
            user.ifPresent(u -> lobbyService.makeOwner(lobby.getName(), u));
        }

        private void kickPlayer(String username) {
            if ("Bot".equals(username)) {
                eventBus.post(new RemoveBotFromLobbyRequest(lobby.getName()));
            } else {
                Optional<User> user = lobby.getUsers().stream().filter(u -> u.getUsername().equals(username)).findFirst();
                user.ifPresent(u -> lobbyService.kickPlayer(lobby.getName(), u));
            }
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            String bot = "Bot";
            if (lobby.getOwner().getUsername().equals(item)) {
                setGraphic(ownerImgV);
            } else if (bot.equals(item)) {
                setGraphic(botImgV);
            } else if (!empty) {
                setGraphic(memberImgV);
            }
            if (empty || item == null || !adminRights || loggedInUser.getUsername().equals(item)) {
                setContextMenu(null);
            } else {
                setContextMenu(contextMenu);
            }
        }
    }
}