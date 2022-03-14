package de.uol.swp.server.game;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.Direction;
import de.uol.swp.common.game.dto.MapNode;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.request.AbstractGameRequest;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildTest {

    static final Coord VALID_EDGE = new Coord(0, 0, Direction.NORTH, MapNode.EDGE);
    static final User test1 = new UserDTO("test1", "test", "test@test.com");
    static final GameLobby GAME_LOBBY = new GameLobby("gameLobbyEm", test1, false);
    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final EventBus bus = new EventBus();
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, mock(AuthenticationService.class));
    final InventoryService inventoryService = new InventoryService(bus, gameSessionService);
    final GameSession fullGameSession = createFullGameSession();

    final Board board = fullGameSession.getBoard();
    final CountDownLatch lock = new CountDownLatch(1);
    final Player[] players = fullGameSession.getPlayers();
    Object event;

    /**
     * Handles DeadEvents detected on the EventBus
     * <p>
     * If a DeadEvent is detected the event variable of this class gets updated
     * to its event and its event is printed to the console output.
     *
     * @param event the DeadEvent detected on the EventBus
     * @since 2019-10-10
     */
    @Subscribe
    void handle(DeadEvent event) {
        this.event = event.getEvent();
        lock.countDown();
    }

    /**
     * Helper method run before each test case
     * <p>
     * This method resets the variable event to null and registers the object of
     * this class to the EventBus.
     */
    @BeforeEach
    void registerBus() {
        event = null;
        bus.register(this);
    }

    /**
     * Helper method run after each test case
     * <p>
     * This method only unregisters the object of this class from the EventBus.
     */
    @AfterEach
    void deregisterBus() {
        bus.unregister(this);
        fullGameSession.stopGame();
    }


    private GameSession createFullGameSession() {
        User test2 = new UserDTO("test2", "test", "test@test.com");
        User test3 = new UserDTO("test3", "test", "test@test.com");
        User test4 = new UserDTO("test4", "test", "test@test.com");
        GAME_LOBBY.joinUser(test2);
        GAME_LOBBY.joinUser(test3);
        GAME_LOBBY.joinUser(test4);
        when(gameSessionFactory.create(GAME_LOBBY)).thenReturn(new GameSession(GAME_LOBBY, gameSessionService, inventoryService, bus));
        GameSession gameSession = gameSessionService.createGameSession(GAME_LOBBY);
        gameSession.playerReady(test1.getUsername());
        gameSession.playerReady(test2.getUsername());
        gameSession.playerReady(test3.getUsername());
        gameSession.playerReady(test4.getUsername());
        return gameSession;
    }

    private void insertSenderIntoRequest(AbstractGameRequest request, String sender) {
        Session session = mock(Session.class);
        request.setSession(session);
        when(session.getUser()).thenReturn(new UserDTO(sender, "", ""));
    }

    @Test
    void boardRuleRoad() {
        //!empty return false
        //settlement adjacent return true
        //else road adjacent and no break return true
        //else road adjacent and break return false
    }

    @Test
    void boardRuleSettlement() {
        //!empty return false
        //road adjacent and no settlement return true
        //no road adjacent and no settlement return true
        //settlement adjacent return false
    }

    @Test
    void boardRuleCity() {
        //has settlement return true else false
    }

    @Test
    void legalNodes() {

    }

    @Test
    void StartBuildRequest() {
        //turn check
        //legal nodes no response
        //cancel road card
        //legal nodes road response
        //legal nodes startbuildresponse
    }

    @Test
    void CancelBuildRequest() {
        //turn check
        //free roads cant cancel
        //response
    }

    @Test
    void PlaceObjectRequest() {
        //turn check
        //Resource check
        //board rule check else exception
        //if free roads reduce and no rss
        //else rss decrease
        //update board
        //update legal nodes
        //uptade longest road + log messages?
        //update vp
        //send objectwasplacedmessage
    }
}
