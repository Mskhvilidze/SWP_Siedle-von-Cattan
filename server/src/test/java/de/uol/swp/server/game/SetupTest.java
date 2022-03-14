package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.Direction;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.dto.MapNode;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.exception.SetupException;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.game.state.SetupState;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SetupTest {

    static final Coord SETTLEMENT = new Coord(0, 1, Direction.NORTH, MapNode.CORNER);
    static final Coord ADJACENT_ROAD = new Coord(1, 0, Direction.WEST, MapNode.EDGE);
    static final Coord SETTLEMENT2 = new Coord(0, 0, Direction.NORTH, MapNode.CORNER);
    static final Coord SETTLEMENT3 = new Coord(0, -2, Direction.NORTH, MapNode.CORNER);
    static final Coord SETTLEMENT4 = new Coord(-1, 0, Direction.NORTH, MapNode.CORNER);
    static final Coord SETTLEMENT5 = new Coord(2, 1, Direction.NORTH, MapNode.CORNER);
    static final Coord SETTLEMENT6 = new Coord(1, -1, Direction.NORTH, MapNode.CORNER);
    static final Coord SETTLEMENT7 = new Coord(1, -2, Direction.NORTH, MapNode.CORNER);
    static final Coord SETTLEMENT8 = new Coord(-2, 0, Direction.NORTH, MapNode.CORNER);

    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final User test1 = new UserDTO("test1", "test", "test@test.com");
    final EventBus bus = new EventBus();
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, mock(AuthenticationService.class));
    final InventoryService inventoryService = new InventoryService(bus, gameSessionService);
    final GameSession fullGameSession = createFullGameSession();
    final Player first = fullGameSession.getPlayer(0);
    final Player second = fullGameSession.getPlayer(1);
    final Player third = fullGameSession.getPlayer(2);
    final Player fourth = fullGameSession.getPlayer(3);

    /**
     * Helper method run after each test case
     */
    @AfterEach
    void stopGame() {
        fullGameSession.stopGame();
    }

    private GameSession createFullGameSession() {
        User test2 = new UserDTO("test2", "test", "test@test.com");
        User test3 = new UserDTO("test3", "test", "test@test.com");
        User test4 = new UserDTO("test4", "test", "test@test.com");
        GameLobby gameLobby = new GameLobby("gameLobby", test1, false);
        gameLobby.joinUser(test2);
        gameLobby.joinUser(test3);
        gameLobby.joinUser(test4);
        when(gameSessionFactory.create(gameLobby)).thenReturn(new GameSession(gameLobby, gameSessionService, inventoryService, bus));
        GameSession gameSession = gameSessionService.createGameSession(gameLobby);
        gameSession.playerReady(test1.getUsername());
        gameSession.playerReady(test2.getUsername());
        gameSession.playerReady(test3.getUsername());
        gameSession.playerReady(test4.getUsername());
        return gameSession;
    }

    @BeforeEach
    void setupState() {
        fullGameSession.setCurrentState(SetupState.INSTANCE);
    }

    @Test
    void getResourcesInSecondPhaseOfSetupPhaseTest() throws GameStateException, OverDrawException {
        placeObjectAndSendRequest(first, SETTLEMENT5);
        placeObjectAndSendRequest(first, first.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT5));
        placeObjectAndSendRequest(second, SETTLEMENT2);
        placeObjectAndSendRequest(second, second.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT2));
        placeObjectAndSendRequest(third, SETTLEMENT3);
        placeObjectAndSendRequest(third, third.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT3));
        placeObjectAndSendRequest(fourth, SETTLEMENT4);
        placeObjectAndSendRequest(fourth, fourth.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT4));

        assertEquals(0, fourth.getInventory().getResources().sumOfResources());

        placeObjectAndSendRequest(fourth, SETTLEMENT);
        assertTrue(2 <= fourth.getInventory().getResources().sumOfResources());
    }

    @Test
    void reverseTurnQueue_ShouldReverseTurnQueue() {
        assertEquals(first, fullGameSession.getWhoseTurn());
        assertEquals(second, fullGameSession.advanceTurn());
        assertEquals(third, fullGameSession.advanceTurn());
        assertEquals(fourth, fullGameSession.advanceTurn());
        fullGameSession.reverseTurnQueueNextAdvance();
        assertEquals(fourth, fullGameSession.advanceTurn());
        assertEquals(third, fullGameSession.advanceTurn());
        assertEquals(second, fullGameSession.advanceTurn());
        assertEquals(first, fullGameSession.advanceTurn());
    }


    @Test
    void placeSettlementFirstTest() {
        SetupException exception = assertThrows(SetupException.class, () -> placeObjectAndSendRequest(first, ADJACENT_ROAD));
        assertEquals("Place Settlement instead", exception.getMessage());
    }

    @Test
    void advanceTurnAfterTwoPlacements() throws GameStateException, OverDrawException {
        placeObjectAndSendRequest(first, SETTLEMENT);
        placeObjectAndSendRequest(first, ADJACENT_ROAD);
        assertEquals(second, fullGameSession.getWhoseTurn());
    }


    @Test
    void ensureSetupEnds() throws GameStateException, OverDrawException {
        placeObjectAndSendRequest(first, SETTLEMENT);
        placeObjectAndSendRequest(first, first.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT));
        placeObjectAndSendRequest(second, SETTLEMENT2);
        placeObjectAndSendRequest(second, second.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT2));
        placeObjectAndSendRequest(third, SETTLEMENT3);
        placeObjectAndSendRequest(third, third.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT3));
        placeObjectAndSendRequest(fourth, SETTLEMENT4);
        placeObjectAndSendRequest(fourth, fourth.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT4));

        placeObjectAndSendRequest(fourth, SETTLEMENT8);
        placeObjectAndSendRequest(fourth, fourth.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT8));
        placeObjectAndSendRequest(third, SETTLEMENT7);
        placeObjectAndSendRequest(third, third.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT7));
        placeObjectAndSendRequest(second, SETTLEMENT6);
        placeObjectAndSendRequest(second, second.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT6));
        placeObjectAndSendRequest(first, SETTLEMENT5);
        placeObjectAndSendRequest(first, first.getRandomLegalRoadConnectedToCornerCoord(SETTLEMENT5));


        assertTrue(fullGameSession.getContext().isSetupPhaseCompleted());
        assertNotSame(SetupState.INSTANCE, fullGameSession.getCurrentState());

    }


    private void insertSenderIntoRequest(PlaceObjectRequest request, String sender) {
        Session session = mock(Session.class);
        request.setSession(session);
        when(session.getUser()).thenReturn(new UserDTO(sender, "", ""));
    }

    private void placeObjectAndSendRequest(Player player, Coord coord) throws GameStateException, OverDrawException {
        PlaceObjectRequest request;
        if (coord.getNodeType() == MapNode.CORNER) {
            request = new PlaceObjectRequest(fullGameSession.getGameSessionName(), PieceType.SETTLEMENT, coord);
        } else {
            request = new PlaceObjectRequest(fullGameSession.getGameSessionName(), PieceType.ROAD, coord);
        }
        insertSenderIntoRequest(request, player.getPlayerName());
        fullGameSession.userInput(request);
    }
}
