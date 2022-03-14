package de.uol.swp.client.main;

import com.google.common.eventbus.Subscribe;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.credits.event.ShowCreditsViewEvent;
import de.uol.swp.client.intro.event.ShowIntroViewEvent;
import de.uol.swp.client.lobby.event.SelectLobbyViewEvent;
import de.uol.swp.client.profile.event.ShowProfileViewEvent;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.message.ChangedUserInfoMessage;
import de.uol.swp.common.user.message.UserAccountDropMessage;
import de.uol.swp.common.user.message.UserLoggedInMessage;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.player.PlayerProfile;
import de.uol.swp.common.user.request.StatisticRequest;
import de.uol.swp.common.user.response.AllOnlineUsersResponse;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import de.uol.swp.common.user.response.StatisticResponseMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Manages the main menu
 *
 * @author Marco Grawunder
 * @see de.uol.swp.client.AbstractPresenter
 * @since 2019-08-29
 */
@SuppressWarnings("UnstableApiUsage")
public class MainMenuPresenter extends AbstractPresenter {

    public static final String FXML = "/fxml/MainMenuView.fxml";

    private static final ShowProfileViewEvent SHOW_PROFILE_VIEW_MESSAGE = new ShowProfileViewEvent();

    private static final ShowCreditsViewEvent SHOW_CREDITS_VIEW_EVENT = new ShowCreditsViewEvent();

    private static final ShowIntroViewEvent SHOW_INTRO_VIEW_EVENT = new ShowIntroViewEvent();

    private static final Logger LOG = LogManager.getLogger(MainMenuPresenter.class);
    private static final StatisticRequest STATISTIC_REQUEST = new StatisticRequest();
    @FXML
    public JFXButton lobbyButton;
    @FXML
    public JFXButton mainMenuStatistic;
    @FXML
    private Pane paneStatistics;
    @FXML
    private TableView<PlayerProfile> tableStatistics;
    @FXML
    private TableColumn<PlayerProfile, String> playerName;
    @FXML
    private TableColumn<PlayerProfile, String> winsStatistic;
    @FXML
    private TableColumn<PlayerProfile, String> lossesStatistic;
    private ObservableList<PlayerProfile> playerStatistics;
    private ObservableList<String> users;

    @FXML
    private JFXListView<String> usersView;

    /**
     * Handles successful login
     * <p>
     * If a LoginSuccessfulResponse is posted to the EventBus the full
     * list of users currently logged in is requested.
     *
     * @param message the LoginSuccessfulResponse object seen on the EventBus
     * @see de.uol.swp.common.user.response.LoginSuccessfulResponse
     * @since 2019-09-05
     */
    @Subscribe
    public void loginSuccessful(LoginSuccessfulResponse message) {
        userService.retrieveAllUsers();
        lobbyService.retrieveAllLobbies();
    }

    /**
     * Handles ChangedUserInfoMessage found on the EventBus.
     * <p>
     * The old user is deleted from the list and a new one is added.
     *
     * @param response ChangedUserInfoMessage found on the EventBus
     * @see ChangedUserInfoMessage
     */
    @Subscribe
    public void changedUserName(ChangedUserInfoMessage response) {
        Platform.runLater(() -> {
            LOG.debug("User {} is updated", response.getUserDTO().getUsername());
            users.add(response.getUserDTO().getUsername());
            users.remove(response.getOldUsername());
        });
    }

    /**
     * Handles new logged in users
     * <p>
     * If a new UserLoggedInMessage object is posted to the EventBus the name of the newly
     * logged in user is appended to the user list in the main menu.
     * Furthermore if the LOG-Level is set to DEBUG the message "New user {@literal
     * <Username>} logged in." is displayed in the log.
     *
     * @param message the UserLoggedInMessage object seen on the EventBus
     * @see de.uol.swp.common.user.message.UserLoggedInMessage
     * @since 2019-08-29
     */
    @Subscribe
    public void newUser(UserLoggedInMessage message) {

        LOG.debug("New user {} logged in", message.getUsername());
        Platform.runLater(() -> {
            if (users != null && userInfo.getLoggedInUser() != null && !userInfo.getLoggedInUser().getUsername().equals(message.getUsername())) {
                users.add(message.getUsername());
            }
        });
    }

    /**
     * Handles new logged out users
     * <p>
     * If a new UserLoggedOutMessage object is posted to the EventBus the name of the newly
     * logged out user is removed from the user list in the main menu.
     * Furthermore if the LOG-Level is set to DEBUG the message "User {@literal
     * <Username>} logged out." is displayed in the log.
     *
     * @param message the UserLoggedOutMessage object seen on the EventBus
     * @see de.uol.swp.common.user.message.UserLoggedOutMessage
     * @since 2019-08-29
     */
    @Subscribe
    public void onUserLoggedOutMessage(UserLoggedOutMessage message) {
        LOG.debug("User {} logged out", message.getUsername());
        Platform.runLater(() -> users.remove(message.getUsername()));
    }

    /**
     * Handles deleted users
     * <p>
     * When a new UserAccountDropMessage object is posted on the EventBus,
     * user will be removed from the user list in the main menu.
     *
     * @param message the UserAccountDropMessage object seen on the EventBus
     * @see de.uol.swp.common.user.message.UserAccountDropMessage
     */
    @Subscribe
    public void onUserAccountDropMessage(UserAccountDropMessage message) {
        LOG.debug("User {} is deleted", message.getUsername());
        Platform.runLater(() -> users.remove(message.getUsername()));
    }

    /**
     * Handles new list of users
     * <p>
     * If a new AllOnlineUsersResponse object is posted to the EventBus the names
     * of currently logged in users are put onto the user list in the main menu.
     * Furthermore if the LOG-Level is set to DEBUG the message "Update of user
     * list" with the names of all currently logged in users is displayed in the
     * log.
     *
     * @param allUsersResponse the AllOnlineUsersResponse object seen on the EventBus
     * @see de.uol.swp.common.user.response.AllOnlineUsersResponse
     * @since 2019-08-29
     */
    @Subscribe
    public void userList(AllOnlineUsersResponse allUsersResponse) {
        LOG.debug("Update of user list {}", allUsersResponse.getUsers());
        updateUsersList(allUsersResponse.getUsers());
    }

    /**
     * Updates the main menus user list according to the list given
     * <p>
     * This method clears the entire user list and then adds the name of each user
     * in the list given to the main menus user list. If there ist no user list
     * this it creates one.
     *
     * @param userList a list of UserDTO objects including all currently logged in
     *                 users
     * @implNote the code inside this Method has to run in the JavaFX-application
     * thread. Therefore it is crucial not to remove the {@code Platform.runLater()}
     * @see UserDTO
     * @since 2019-08-29
     */
    private void updateUsersList(List<UserDTO> userList) {
        // Attention: This must be done on the FX Thread!
        Platform.runLater(() -> {
            if (users == null) {
                users = FXCollections.observableArrayList();
                usersView.setItems(users);
            }
            users.clear();
            userList.forEach(u -> users.add(u.getUsername()));
        });
    }

    /**
     * Method called when the logout button is pressed
     * <p>
     * If the logout button is pressed, this method requests the user service
     * to logout the loggedInUser
     *
     * @see de.uol.swp.client.user.UserService
     * @see de.uol.swp.client.user.UserInfo
     */
    @FXML
    private void onLogoutButtonPressed() {
        userService.logout(userInfo.getLoggedInUser());
    }

    /**
     * Method called when the Lobby button is pressed
     * <p>
     * If the lobby button is pressed, this method posts an event on the bus for the
     * SceneManager to change the view of the loggedInUser.
     * The method also posts a request on the bus to get the list of Lobbies.
     *
     * @see de.uol.swp.client.user.UserService
     */
    @FXML
    private void onLobbyButtonPressed() {
        SelectLobbyViewEvent selectLobbyViewEvent = new SelectLobbyViewEvent();
        eventBus.post(selectLobbyViewEvent);
    }

    /**
     * Method called when the profile button is pressed
     * <p>
     * It posts an instance of the ShowProfileViewEvent
     * to the EventBus the SceneManager is subscribed to.
     *
     * @see de.uol.swp.client.profile.event.ShowProfileViewEvent
     * @see de.uol.swp.client.SceneManager
     */
    @FXML
    private void onProfileButtonPressed() {
        eventBus.post(SHOW_PROFILE_VIEW_MESSAGE);
    }

    /**
     * Method called when the show credits button is pressed
     * <p>
     * It posts an instance of the ShowCreditsViewEvent
     * to the EventBus the SceneManager is subscribed to.
     *
     * @see de.uol.swp.client.credits.event.ShowCreditsViewEvent
     * @see de.uol.swp.client.SceneManager
     */
    @FXML
    private void onCreditsButtonPressed() {
        eventBus.post(SHOW_CREDITS_VIEW_EVENT);
    }


    /**
     * Method called when the show intro button is pressed
     * <p>
     * It posts an instance of the ShowIntroViewEvent
     * to the EventBus the SceneManager is subscribed to.
     */
    @FXML
    private void onIntroButtonPressed() {
        eventBus.post(SHOW_INTRO_VIEW_EVENT);
    }

    /**
     * Method called when the "statistics" button is pressed
     * <p>
     * It shows the statistics of all players, defined in PlayerStatistic
     *
     * @see de.uol.swp.common.user.request.StatisticRequest
     */
    @FXML
    private void onStatisticRequestButtonPressed() {
        eventBus.post(STATISTIC_REQUEST);
        paneStatistics.setVisible(!paneStatistics.isVisible());
    }

    /**
     * Handles a new list of user statistics
     * <p>
     * If the statistics table hasn't been initialized before the backing ObservableList will be initialized
     * and the table cell factories set
     *
     * @param message the {@code StatisticResponseMessage} found on the EventBus
     */
    @Subscribe
    public void onStatisticResponseMessage(StatisticResponseMessage message) {
        Platform.runLater(() -> {
            if (playerStatistics == null) {
                playerStatistics = FXCollections.observableArrayList();

                Platform.runLater(() -> tableStatistics.getItems().addAll(playerStatistics));
                playerName.setCellValueFactory(new PropertyValueFactory<>("playerName"));
                winsStatistic.setCellValueFactory(new PropertyValueFactory<>("won"));
                lossesStatistic.setCellValueFactory(new PropertyValueFactory<>("loss"));
            }
            playerStatistics.clear();
            playerStatistics.addAll(message.getList());
        });
    }

    /**
     * Method for hiding statistics after usage
     */
    @FXML
    private void closePlayerStatistics() {
        paneStatistics.setVisible(false);
    }
}

