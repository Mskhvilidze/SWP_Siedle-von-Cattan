package de.uol.swp.client.gameSession;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.client.game.GameSessionService;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.request.TurnEndRequest;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Class for the GameSessionService
 */
@SuppressWarnings("UnstableApiUsage")
class GameSessionServiceTest {

    static final Logger LOG = LogManager.getLogger(GameSessionServiceTest.class);
    final EventBus bus = new EventBus();
    final GameSessionService gameSessionService = new GameSessionService(bus);
    final Map<String, GameSessionPresenter> gameSessions = new HashMap<>();
    final GameSessionPresenter gameSessionPresenter = new GameSessionPresenter();
    final CountDownLatch lock = new CountDownLatch(1);
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
        LOG.info("Dead event: {}\n", this.event);
        lock.countDown();
    }

    /**
     * Helper method run before each test case
     * <p>
     * This method resets the variable event to null and registers the object of
     * this class to the EventBus.
     *
     * @since 2019-10-10
     */
    @BeforeEach
    void registerBus() {
        event = null;
        bus.register(this);
    }


    @Test
    void addGameSessionTest() {
        gameSessions.put("test", gameSessionPresenter);
        assertEquals(gameSessions.get("test"), gameSessionPresenter);
    }

    @Test
    void endTurnTest() {
        gameSessionService.endTurn("test");
        assertTrue(event instanceof TurnEndRequest);
        TurnEndRequest turnEndRequest = (TurnEndRequest) event;
        assertEquals("test", turnEndRequest.getGameSessionName());
    }


    @Test
    void createObjectPlacementRequestTest() {
        Coord coord = new Coord(0, 0);
        gameSessionService.createObjectPlacementRequest("test", coord, PieceType.SETTLEMENT);

        assertTrue(event instanceof PlaceObjectRequest);

        PlaceObjectRequest request = (PlaceObjectRequest) event;
        assertEquals("test", request.getGameSessionName());
        assertEquals(PieceType.SETTLEMENT, request.getObjectToPlace());
        assertEquals(coord, request.getCoord());
    }
}
