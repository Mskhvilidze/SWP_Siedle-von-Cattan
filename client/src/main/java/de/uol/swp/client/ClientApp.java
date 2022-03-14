package de.uol.swp.client;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uol.swp.client.di.ClientModule;
import de.uol.swp.client.game.GameSessionService;
import de.uol.swp.client.lobby.LobbyPresenter;
import de.uol.swp.client.lobby.LobbyService;
import de.uol.swp.client.user.ClientUserService;
import de.uol.swp.client.user.UserInfo;
import de.uol.swp.common.game.response.RejoinGameResponse;
import de.uol.swp.common.lobby.message.StartGameSessionMessage;
import de.uol.swp.common.lobby.response.LobbyJoinSuccessfulResponse;
import de.uol.swp.common.user.exception.RegistrationExceptionMessage;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.response.ChangeUserInfoSuccessfulResponse;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import de.uol.swp.common.user.response.RegistrationSuccessfulResponse;
import io.netty.channel.Channel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * The application class of the client
 * <p>
 * This class handles the startup of the application, as well as, incoming login
 * and registration responses and error messages
 *
 * @author Marco Grawunder
 * @see de.uol.swp.client.ConnectionListener
 * @see javafx.application.Application
 * @since 2017-03-17
 */
@SuppressWarnings({"UnstableApiUsage", "PMD.AvoidCatchingGenericException"})
public class ClientApp extends Application implements ConnectionListener {

    private static final Logger LOG = LogManager.getLogger(ClientApp.class);

    private String name;
    private String pass;
    private boolean loginSupplied = false;

    private String host;
    private int port;

    private UserInfo userInfo;

    private ClientUserService userService;

    private LobbyService lobbyService;

    private GameSessionService gameSessionService;

    private ClientConnection clientConnection;

    private EventBus eventBus;

    private SceneManager sceneManager;

    // -----------------------------------------------------
    // Java FX Methods
    // ----------------------------------------------------

    /**
     * Default startup method for javafx applications
     *
     * @param args 1. host, 2. port
     *             "localhost" 50092
     * @since 2017-03-17
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        Parameters parameter = getParameters();
        List<String> args = parameter.getRaw();

        if (args.size() != 2 && !args.get(0).equals("localhost")) {
            host = "duemmer.informatik.uni-oldenburg.de";
            port = 50_092;
            LOG.debug("Usage: {} host port", ClientConnection.class.getSimpleName());
            LOG.debug("Using default port {} on {}", port, host);
        } else {
            host = args.get(0);
            port = Integer.parseInt(args.get(1));
        }

        if (!args.isEmpty()) {
            for (int i = 0; i < args.size(); ++i) {
                if (args.get(i).equals("--login")) {
                    name = args.get(i + 1);
                    pass = args.get(i + 2);
                    loginSupplied = true;
                    break;
                }
            }
        }
        // do not establish connection here
        // if connection is established in this stage, no GUI is shown and
        // exceptions are only visible in console!
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setResizable(false);
        // Client app is created by java, so injection must
        // be handled here manually
        Injector injector = Guice.createInjector(new ClientModule());

        // get user service from guice, is needed for logout
        this.userService = injector.getInstance(ClientUserService.class);
        this.lobbyService = injector.getInstance(LobbyService.class);
        this.gameSessionService = injector.getInstance(GameSessionService.class);

        this.userInfo = injector.getInstance(UserInfo.class);

        // get event bus from guice
        eventBus = injector.getInstance(EventBus.class);
        // Register this class for de.uol.swp.client.events (e.g. for exceptions)
        eventBus.register(this);

        // Client app is created by java, so injection must
        // be handled here manually
        SceneManagerFactory sceneManagerFactory = injector.getInstance(SceneManagerFactory.class);
        this.sceneManager = sceneManagerFactory.create(primaryStage);

        ClientConnectionFactory connectionFactory = injector.getInstance(ClientConnectionFactory.class);
        clientConnection = connectionFactory.create(host, port);
        clientConnection.addConnectionListener(this);
        // JavaFX Thread should not be blocked to long!
        Thread thread = new Thread(() -> {
            try {
                clientConnection.start();
            } catch (Exception e) {
                exceptionOccurred(e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        eventBus.unregister(this);
        // Important: Close connection so connection thread can terminate
        // else client application will not stop
        LOG.trace("Trying to shut down client ...");
        if (clientConnection != null) {
            clientConnection.close();
        }
        LOG.info("ClientConnection shutdown");
    }

    @Override
    public void connectionEstablished(Channel channel) {
        if (loginSupplied) {
            Platform.runLater(() -> userService.login(name, pass));
        } else {
            sceneManager.showLoginScreen();
        }
    }

    @Override
    public void exceptionOccurred(String exception) {
        sceneManager.showServerError(exception);
    }

    /**
     * Handles successful login
     * <p>
     * If an LoginSuccessfulResponse object is detected on the EventBus this
     * method is called. It tells the SceneManager to show the main menu and sets
     * this clients user to the user found in the object. If the loglevel is set
     * to DEBUG or higher "user logged in successfully " and the username of the
     * logged in user are written to the log.
     *
     * @param message the LoginSuccessfulResponse object detected on the EventBus
     * @see de.uol.swp.client.SceneManager
     * @since 2017-03-17
     */
    @Subscribe
    public void userLoggedIn(LoginSuccessfulResponse message) {
        LOG.debug("user logged in successfully {}", message.getUser().getUsername());
        userInfo.setLoggedInUser(message.getUser());
        sceneManager.showMainScreen(message.getUser());
    }

    /**
     * Handles successful logout
     * <p>
     * If an UserLoggedOutMessage object is detected on the EventBus
     * this method is called. If the username stored in the message
     * is equal to the clients username it will tell the SceneManager
     * to show the login screen.
     *
     * @param message the UserLoggedOutMessage object detected on the EventBus
     */
    @Subscribe
    public void userLoggedOut(UserLoggedOutMessage message) {
        if (message.getUsername().equals(userInfo.getLoggedInUser().getUsername())) {
            sceneManager.showLoginScreen();
        }
    }

    /**
     * If an ClientChangedUserInfoMessage is detected on the EventBus, this method is called.
     * The found user is displayed by ScannerManager in the main menu.
     *
     * @param response
     * @see ChangeUserInfoSuccessfulResponse
     */
    @Subscribe
    public void onChangedUser(ChangeUserInfoSuccessfulResponse response) {
        LOG.debug("User updated successfully {}", response.getNewUserInfo().getUsername());
        userInfo.setLoggedInUser(response.getNewUserInfo());
    }

    /**
     * Handles unsuccessful registrations
     * <p>
     * If an RegistrationExceptionMessage object is detected on the EventBus this
     * method is called. It tells the SceneManager to show the sever error alert.
     * If the loglevel is set to Error or higher "Registration error " and the
     * error message are written to the log.
     *
     * @param message the RegistrationExceptionMessage object detected on the EventBus
     * @see de.uol.swp.client.SceneManager
     * @since 2019-09-02
     */
    @Subscribe
    public void onRegistrationExceptionMessage(RegistrationExceptionMessage message) {
        sceneManager.showServerError("Registration error " + message);
        LOG.error("Registration error: {}", message);
    }

    /**
     * Handles successful registrations
     * <p>
     * If an RegistrationSuccessfulResponse object is detected on the EventBus this
     * method is called. It tells the SceneManager to show the login window. If
     * the loglevel is set to INFO or higher "Registration Successful." is written
     * to the log.
     *
     * @param message the RegistrationSuccessfulResponse object detected on the EventBus
     * @see de.uol.swp.client.SceneManager
     * @since 2019-09-02
     */
    @Subscribe
    public void onRegistrationSuccessfulMessage(RegistrationSuccessfulResponse message) {
        LOG.info("Registration successful.");
        sceneManager.showLoginScreen();
    }

    /**
     * Handles errors produced by the EventBus
     * <p>
     * If an DeadEvent object is detected on the EventBus, this method is called.
     * It writes "DeadEvent detected " and the error message of the detected DeadEvent
     * object to the log, if the loglevel is set to ERROR or higher.
     *
     * @param deadEvent the DeadEvent object found on the EventBus
     * @since 2019-08-07
     */
    @Subscribe
    private void handleEventBusError(DeadEvent deadEvent) {
        LOG.error("DeadEvent detected: {}", deadEvent);
    }

    /**
     * Handles the creation of a new lobby view
     * <p>
     * If an LobbyJoinSuccessfulResponse object is detected on the EventBus this method is called.
     * It tells the SceneManager to create a new lobby tab and adds the returned {@link de.uol.swp.client.lobby.LobbyContainer}
     * to the {@code LobbyService}
     *
     * @param message the LobbyJoinSuccessfulResponse object detected on the EventBus
     * @see LobbyPresenter
     * @see LobbyService
     */
    @Subscribe
    private void onLobbySuccessfulJoined(LobbyJoinSuccessfulResponse message) {
        lobbyService.addLobby(message.getLobby().getName(), sceneManager.createLobbyTab(message, userInfo.getLoggedInUser()));
    }

    /**
     * Handles the creation of a new game session view
     * <p>
     * If an {@code StartGameSessionMessage} object is detected on the EventBus this method is called.
     * It tells the {@code SceneManager} to create a new game tab
     *
     * @param message the StartGameSessionMessage object detected on the EventBus
     */
    @Subscribe
    public void onStartGameSessionMessage(StartGameSessionMessage message) {
        LobbyPresenter lobbyPresenter = lobbyService.getLobbies().get(message.getLobbyName());
        gameSessionService.addGameSession(message.getLobbyName(), sceneManager.createGameTab(lobbyPresenter, message.getGameDTO()));
    }


    /**
     * Handles the creation of game session view for a rejoined user
     * <p>
     * If an {@code RejoinGameResponse} object is detected on the EventBus this method is called.
     * It tells the {@code SceneManager} to create a new game tab
     * after that it calls the updateWhoseTurn - method of gameSessionService
     *
     * @param response the RejoinGameResponse object detected on the EventBus
     */
    @Subscribe
    public void onRejoinGameSessionResponse(RejoinGameResponse response) {
        LobbyPresenter lobbyPresenter = lobbyService.getLobbies().get(response.getLobbyName());
        gameSessionService.addGameSession(response.getLobbyName(), sceneManager.createGameTab(lobbyPresenter, response.getGameDTO()));
        if (response.getWhoseTurn() == null) {
            gameSessionService.updateWhoseTurn(response.getLobbyName(), null, 0);
        } else {
            gameSessionService.updateWhoseTurn(response.getLobbyName(), response.getWhoseTurn().getPlayerName(), 0);
        }
    }
}
