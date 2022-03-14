package de.uol.swp.client;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import de.uol.swp.client.auth.LoginPresenter;
import de.uol.swp.client.auth.events.ShowLoginViewEvent;
import de.uol.swp.client.credits.CreditsPresenter;
import de.uol.swp.client.credits.event.ShowCreditsViewEvent;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.client.intro.IntroPresenter;
import de.uol.swp.client.intro.event.ShowIntroViewEvent;
import de.uol.swp.client.lobby.LobbyContainer;
import de.uol.swp.client.lobby.LobbyPresenter;
import de.uol.swp.client.lobby.SelectLobbyPresenter;
import de.uol.swp.client.lobby.event.SelectLobbyViewEvent;
import de.uol.swp.client.main.MainMenuPresenter;
import de.uol.swp.client.main.event.ReturnToMainMenuViewEvent;
import de.uol.swp.client.main.tab.MainTabPresenter;
import de.uol.swp.client.main.tab.SessionTab;
import de.uol.swp.client.profile.ProfilePresenter;
import de.uol.swp.client.profile.event.ConfirmPasswordErrorEvent;
import de.uol.swp.client.profile.event.ShowProfileViewEvent;
import de.uol.swp.client.register.RegistrationPresenter;
import de.uol.swp.client.register.event.RegistrationCanceledEvent;
import de.uol.swp.client.register.event.RegistrationErrorEvent;
import de.uol.swp.client.register.event.ShowRegistrationViewEvent;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.lobby.response.LobbyJoinSuccessfulResponse;
import de.uol.swp.common.user.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * Class that manages which window/scene is currently shown
 *
 * @author Marco Grawunder
 * @since 2019-09-03
 */
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "UnstableApiUsage"})
public class SceneManager {

    private static final Logger LOG = LogManager.getLogger(SceneManager.class);
    private static final String STYLE_SHEET = "css/swp.css";

    private final Stage primaryStage;
    private final Injector injector;
    //Scenes//
    private Scene loginScene;
    private Scene registrationScene;
    private Scene lastScene = null;
    private Scene currentScene = null;
    private String lastTitle;

    //Tab-Panes//
    private Parent mainPane;
    private Parent selectLobbyPane;
    private Parent profilePane;
    private Parent creditsPane;
    private Parent introPane;
    private Scene mainTabScene;
    private MainTabPresenter mainTabPresenter;

    /**
     * Constructor
     *
     * @param eventBus     eventBus
     * @param injected     The Injector
     * @param primaryStage primaryStage
     */

    @Inject
    public SceneManager(EventBus eventBus, Provider<Injector> injected, @Assisted Stage primaryStage) {
        eventBus.register(this);
        this.primaryStage = primaryStage;
        this.injector = injected.get();
        initViews();
    }

    /**
     * Initializes the fxmlLoader
     *
     * @since 2020-12-09
     */
    @SuppressWarnings("java:S112")
    private FXMLLoader initLoader(String fxmlFile) {
        FXMLLoader loader = injector.getInstance(FXMLLoader.class);
        try {
            URL url = getClass().getResource(fxmlFile);
            LOG.debug("Loading {}", url);
            loader.setLocation(url);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Could not load View!" + e.getMessage(), e);
        }
        return loader;
    }

    /**
     * Subroutine to initialize all views
     * <p>
     * This is a subroutine of the constructor to initialize all views
     *
     * @since 2019-09-03
     */
    private void initViews() {
        initLoginView();
        initMainView();
        initRegistrationView();
        initSelectLobbyView();
        initProfileView();
        initCreditsView();
        initIntroView();
        initMainTabScene();
    }

    /**
     * Subroutine creating parent panes from FXML files
     * <p>
     * This Method tries to create a parent pane from the FXML file specified by
     * the URL String given to it. If the LOG-Level is set to Debug or higher loading
     * is written to the LOG.
     * If it fails to load the view a RuntimeException is thrown.
     *
     * @param fxmlFile the FXML file to load the view from
     * @return view loaded from FXML or null
     * @since 2019-09-03
     */
    @SuppressWarnings("java:S112")
    private Parent initPresenter(String fxmlFile) {
        Parent rootPane;
        FXMLLoader loader = injector.getInstance(FXMLLoader.class);
        try {
            URL url = getClass().getResource(fxmlFile);
            LOG.debug("Loading {}", url);
            loader.setLocation(url);
            rootPane = loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Could not load View!" + e.getMessage(), e);
        }
        return rootPane;
    }

    /**
     * Initializes the main menu view
     * <p>
     * If the mainPane is null it gets set to a new pane showing the main menu view
     * as specified by the MainMenuView FXML file.
     *
     * @see MainMenuPresenter#FXML
     */
    private void initMainView() {
        if (mainPane == null) {
            mainPane = initPresenter(MainMenuPresenter.FXML);
        }
    }

    /**
     * Initializes the select Lobby menu view
     * <p>
     * If the selectLobbyPane is null it gets set to a new pane showing the main menu view
     * as specified by the SelectLobbyView FXML file.
     *
     * @see SelectLobbyPresenter#FXML
     */
    private void initSelectLobbyView() {
        if (selectLobbyPane == null) {
            selectLobbyPane = initPresenter(SelectLobbyPresenter.FXML);
        }
    }

    /**
     * Initializes the login view
     * <p>
     * If the loginScene is null it gets set to a new scene containing the
     * a pane showing the login view as specified by the LoginView FXML file.
     *
     * @see de.uol.swp.client.auth.LoginPresenter#FXML
     * @since 2019-09-03
     */
    private void initLoginView() {
        if (loginScene == null) {
            Parent rootPane = initPresenter(LoginPresenter.FXML);
            loginScene = new Scene(rootPane);
            loginScene.getStylesheets().add(STYLE_SHEET);
        }
    }

    /**
     * Initializes the profile view
     * <p>
     * If the profilePane is null it gets set to a new pane showing the profile view
     * as specified by the ProfileView FXML file.
     *
     * @see de.uol.swp.client.profile.ProfilePresenter#FXML
     */
    private void initProfileView() {
        if (profilePane == null) {
            profilePane = initPresenter(ProfilePresenter.FXML);
        }
    }

    /**
     * Initializes the credits view
     * <p>
     * If the creditsPane is null it gets set to a new pane showing the profile view
     * as specified by the creditsView FXML file.
     *
     * @see de.uol.swp.client.credits.CreditsPresenter#FXML
     */
    private void initCreditsView() {
        if (creditsPane == null) {
            creditsPane = initPresenter(CreditsPresenter.FXML);
        }
    }

    /**
     * Initializes the Intro view
     * <p>
     * If the introPane is null it gets set to a new pane showing the profile view
     * as specified by the IntroView FXML file.
     *
     * @see de.uol.swp.client.intro.IntroPresenter#FXML
     */
    private void initIntroView() {
        if (introPane == null) {
            introPane = initPresenter(IntroPresenter.FXML);
        }
    }

    /**
     * Initializes the registration view
     * <p>
     * If the registrationScene is null it gets set to a new scene containing the
     * a pane showing the registration view as specified by the RegistrationView
     * FXML file.
     *
     * @see de.uol.swp.client.register.RegistrationPresenter#FXML
     * @since 2019-09-03
     */
    private void initRegistrationView() {
        if (registrationScene == null) {
            Parent rootPane = initPresenter(RegistrationPresenter.FXML);
            registrationScene = new Scene(rootPane);
            registrationScene.getStylesheets().add(STYLE_SHEET);
        }
    }

    /**
     * Handles ShowRegistrationViewEvent detected on the EventBus
     * <p>
     * If a ShowRegistrationViewEvent is detected on the EventBus, this method gets
     * called. It calls a method to switch the current screen to the registration
     * screen.
     *
     * @param event the ShowRegistrationViewEvent detected on the EventBus
     * @see de.uol.swp.client.register.event.ShowRegistrationViewEvent
     * @since 2019-09-03
     */
    @Subscribe
    public void onShowRegistrationViewEvent(ShowRegistrationViewEvent event) {
        showRegistrationScreen();
    }

    /**
     * Handles ShowLoginViewEvent detected on the EventBus
     * <p>
     * If a ShowLoginViewEvent is detected on the EventBus, this method gets
     * called. It calls a method to switch the current screen to the login screen.
     *
     * @param event the ShowLoginViewEvent detected on the EventBus
     * @see de.uol.swp.client.auth.events.ShowLoginViewEvent
     * @since 2019-09-03
     */
    @Subscribe
    public void onShowLoginViewEvent(ShowLoginViewEvent event) {
        showLoginScreen();
    }

    /**
     * Handles ShowProfileViewEvent detected on the EventBus
     * <p>
     * If a ShowProfileViewEvent is detected on the EventBus, this method gets
     * called. It calls a method to switch the current screen to the profile screen.
     *
     * @param event the ShowLoginViewEvent detected on the EventBus
     * @see de.uol.swp.client.profile.event.ShowProfileViewEvent
     */
    @Subscribe
    private void onShowProfileViewEvent(ShowProfileViewEvent event) {
        showTabScene(profilePane);
    }

    /**
     * Handles ShowCreditsViewEvent detected on the EventBus
     * <p>
     * If a ShowCreditsViewEvent is detected on the EventBus, this method gets
     * called. It calls a method to switch the current screen to the credits screen.
     *
     * @param event the ShowLoginViewEvent detected on the EventBus
     * @see de.uol.swp.client.credits.event.ShowCreditsViewEvent
     */
    @Subscribe
    private void onShowCreditsViewEvent(ShowCreditsViewEvent event) {
        showTabScene(creditsPane);
    }

    /**
     * This method is called  when a SelectLobbyViewEvent is posted on the bus.
     * This method changes the view to the SelectLobbyView.
     *
     * @param event the SelectLobbyViewEvent detected on the EventBus
     */
    @Subscribe
    public void showSelectLobbyScene(SelectLobbyViewEvent event) {
        showTabScene(selectLobbyPane);
    }

    /**
     * Shows the Intro screen
     * <p>
     * Switches the current tab content to the introPane
     */
    @Subscribe
    public void showIntroScreen(ShowIntroViewEvent event) {
        LOG.info("Intro started.");
        showTabScene(introPane);
    }

    /**
     * Handles ReturnToMainMenuViewEvent detected on the EventBus
     * <p>
     * If a ReturnToMainMenuViewEvent is detected on the EventBus, this method gets
     * called. It calls a method to return the current tab content to the main menu pane
     *
     * @param event the ReturnToMainMenuViewEvent detected on the EventBus
     * @see ReturnToMainMenuViewEvent
     */
    @Subscribe
    private void onReturnToMainMenuViewEvent(ReturnToMainMenuViewEvent event) {
        showTabScene(mainPane);
    }

    /**
     * Handles RegistrationCanceledEvent detected on the EventBus
     * <p>
     * If a RegistrationCanceledEvent is detected on the EventBus, this method gets
     * called. It calls a method to show the screen shown before registration.
     *
     * @param event the RegistrationCanceledEvent detected on the EventBus
     * @see de.uol.swp.client.register.event.RegistrationCanceledEvent
     * @since 2019-09-03
     */
    @Subscribe
    public void onRegistrationCanceledEvent(RegistrationCanceledEvent event) {
        showScene(lastScene, lastTitle);
    }

    /**
     * Handles RegistrationErrorEvent detected on the EventBus
     * <p>
     * If a RegistrationErrorEvent is detected on the EventBus, this method gets
     * called. It shows the error message of the event in a error alert.
     *
     * @param event the RegistrationErrorEvent detected on the EventBus
     * @see de.uol.swp.client.register.event.RegistrationErrorEvent
     * @since 2019-09-03
     */
    @Subscribe
    public void onRegistrationErrorEvent(RegistrationErrorEvent event) {
        showError(event.getMessage());
    }

    /**
     * Handles ConfirmPasswordErrorEvent detected on the EvenBus
     * <p>
     * If a ConfirmPasswordErrorEvent is detected on the EventBus, this method gets
     * called, It shows the error message of the event in a error alert
     *
     * @param event the ConfirmPasswordErrorEvent detected on the EventBus
     * @see ConfirmPasswordErrorEvent
     */
    @Subscribe
    public void onConfirmPasswordErrorEvent(ConfirmPasswordErrorEvent event) {
        showError(event.getMessage());
    }

    /**
     * Shows an error message inside an error alert
     *
     * @param message the type of error to be shown
     * @param error   the error message
     * @since 2019-09-03
     */
    public void showError(String message, String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message + error);
            alert.showAndWait();
        });
    }

    /**
     * Shows a server error message inside an error alert
     *
     * @param error the error message
     * @since 2019-09-03
     */
    public void showServerError(String error) {
        showError("Server returned an error:\n", error);
    }

    /**
     * Shows an error message inside an error alert
     *
     * @param error the error message
     * @since 2019-09-03
     */
    public void showError(String error) {
        showError("Error:\n", error);
    }

    /**
     * Switches the current scene and title to the given ones
     * <p>
     * The current scene and title are saved in the lastScene and lastTitle variables,
     * before the new scene and title are set and shown.
     *
     * @param scene the new scene to show
     * @param title the new window title
     * @since 2019-09-03
     */
    private void showScene(final Scene scene, final String title) {
        this.lastScene = currentScene;
        this.lastTitle = primaryStage.getTitle();
        this.currentScene = scene;
        Platform.runLater(() -> {
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        });
    }

    /**
     * Shows the main tab screen
     * <p>
     * Switches the current Scene to the mainTabScene and sets the title of
     * the window to "Welcome " and the username of the current user
     */
    public void showMainScreen(User currentUser) {
        showScene(mainTabScene, "Catan - Willkommen " + currentUser.getUsername());
        showTabScene(mainPane);
    }

    /**
     * Shows the login screen
     * <p>
     * Switches the current Scene to the loginScene and sets the title of
     * the window to "Login"
     *
     * @since 2019-09-03
     */
    public void showLoginScreen() {
        showScene(loginScene, "Catan - Anmelden");
    }

    /**
     * Shows the registration screen
     * <p>
     * Switches the current Scene to the registrationScene and sets the title of
     * the window to "Registration"
     *
     * @since 2019-09-03
     */
    public void showRegistrationScreen() {
        showScene(registrationScene, "Catan - Registrieren");
    }

    /**
     * Initializes the main tab view
     * <p>
     * If the mainTabScene is null it gets set to a new scene containing the
     * a pane showing the main tab view as specified by the MainTabView
     * FXML file.
     *
     * @see MainTabPresenter
     */
    private void initMainTabScene() {
        if (mainTabScene == null) {
            FXMLLoader loader = initLoader("/fxml/MainTabView.fxml");
            mainTabScene = new Scene(loader.getRoot());
            mainTabScene.getStylesheets().add(STYLE_SHEET);
            mainTabPresenter = loader.getController();
        }
    }

    /**
     * Sets the content of the main tab
     *
     * @param root the pane for the main tab
     */
    public void showTabScene(Parent root) {
        mainTabPresenter.setMainTabContent((Pane) root);
    }

    /**
     * Initializes the lobby menu
     * <p>
     * Initializes the {@code LobbyPresenter} and creates a new tab by calling
     * {@link MainTabPresenter#addNewLobbyTab(String, Pane)}.
     *
     * @param message the {@code LobbyJoinSuccessfulResponse} that started the join
     * @param user    the user that is currently logged in
     * @return a {@code LobbyContainer} object containing an instance of the {@code LobbyPresenter} and the {@code SessionTab}
     */
    public LobbyContainer createLobbyTab(LobbyJoinSuccessfulResponse message, User user) {
        FXMLLoader loader = initLoader(LobbyPresenter.FXML);
        LobbyPresenter lobbyPresenter = loader.getController();
        lobbyPresenter.setUser(user);
        lobbyPresenter.setLobby(message.getLobby());
        lobbyPresenter.setChatLobby(message.getLobby());
        lobbyPresenter.initLobbyUserList();

        SessionTab tab = mainTabPresenter.addNewLobbyTab(message.getLobby().getName(), loader.getRoot());
        return new LobbyContainer(lobbyPresenter, tab);
    }

    /**
     * Initializes the game session view
     * <p>
     * Initializes the {@code GameSessionPresenter} and sets the content
     * of the correct tab to the game pane by calling {@link MainTabPresenter#addNewGameTab(String, Pane)}
     *
     * @param gameDTO the game that is started
     * @return the {@code GameSessionPresenter} instance that was created
     */
    public GameSessionPresenter createGameTab(LobbyPresenter lobbyPresenter, GameDTO gameDTO) {
        FXMLLoader loader = new FXMLLoader();
        EventBus bus = new EventBus();
        try {
            URL url = getClass().getResource(GameSessionPresenter.FXML);
            LOG.debug("Loading GameSessionView: {}", url);
            loader.setControllerFactory(param -> {
                var instance = injector.getInstance(param);
                if (instance instanceof AbstractGamePresenter) {
                    ((AbstractGamePresenter) instance).setSecondEventBus(bus);
                } else if (instance instanceof GameSessionPresenter) {
                    ((GameSessionPresenter) instance).setLocalEventBus(bus);
                }
                return instance;
            });
            loader.setLocation(url);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Could not load GameSessionView!" + e.getMessage(), e);
        }
        GameSessionPresenter gameSessionPresenter = loader.getController();
        gameSessionPresenter.setGame(gameDTO, gameDTO.getGameSessionName());
        gameSessionPresenter.setChatLobby(lobbyPresenter.getChatPresenter());
        mainTabPresenter.addNewGameTab(gameDTO.getGameSessionName(), loader.getRoot());
        return gameSessionPresenter;
    }
}
