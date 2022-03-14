package de.uol.swp.server.game;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.Direction;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.dto.MapNode;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.message.GameOverMessage;
import de.uol.swp.common.game.request.AbstractGameRequest;
import de.uol.swp.common.game.request.RollDiceRequest;
import de.uol.swp.common.game.request.UseCardRequest;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.exception.CanNotRejoinException;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.exception.UserIsNotPartOfGameSessionException;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.mapobject.RoadPiece;
import de.uol.swp.server.game.mapobject.SettlementPiece;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.game.state.DiceState;
import de.uol.swp.server.game.state.PlayState;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GameSession Test Class
 */
@SuppressWarnings("UnstableApiUsage")
class GameSessionTest {

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

    @Nested
    class KnightAwardsTest {

        @BeforeEach
        void setup() {
            fullGameSession.setCurrentState(PlayState.INSTANCE);
        }

        @Test
        void contextShouldContainThePlayerWithLargestArmyBonus() throws GameStateException, OverDrawException {
            var players = fullGameSession.getPlayers();
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);
            players[1].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);

            assertEquals(players[0], fullGameSession.getContext().getPlayerWithLargestArmy());
        }

        @Test
        void awardVictoryPointsToPlayer() throws GameStateException, OverDrawException {
            var players = fullGameSession.getPlayers();
            var oldVictoryPoints = players[0].getNumOfPublicVP();
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);
            var newVictoryPoints = players[0].getNumOfPublicVP();

            assertEquals(oldVictoryPoints + 2, newVictoryPoints);
        }

        @Test
        void victoryPointBonusShouldChange() throws GameStateException, OverDrawException {
            var players = fullGameSession.getPlayers();
            int oldVictoryPoints = players[0].getNumOfPublicVP();
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);
            players[1].setPlayedDevCardThisTurn(false);
            int newVictoryPoints = players[0].getNumOfPublicVP();
            assertEquals(oldVictoryPoints + 2, newVictoryPoints);

            playKnightCardAndSkipRobberPlacing(players[1]);
            players[1].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);
            players[1].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);
            players[1].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);
            players[1].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);

            newVictoryPoints = players[0].getNumOfPublicVP();
            assertEquals(oldVictoryPoints, newVictoryPoints);


        }

        @Test
        void insufficientKnightsPlayedShouldNotAwardVictoryPoints() throws GameStateException, OverDrawException {
            var players = fullGameSession.getPlayers();
            var oldVictoryPoints = players[0].getNumOfPublicVP();
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[0]);
            players[0].setPlayedDevCardThisTurn(false);
            playKnightCardAndSkipRobberPlacing(players[1]);
            var newVictoryPoints = players[0].getNumOfPublicVP();

            assertEquals(oldVictoryPoints, newVictoryPoints);
        }


        private void insertSenderIntoRequest(UseCardRequest request, String sender) {
            Session session = mock(Session.class);
            request.setSession(session);
            when(session.getUser()).thenReturn(new UserDTO(sender, "", ""));
        }

        private void playKnightCardAndSkipRobberPlacing(Player sender) throws GameStateException, OverDrawException {
            UseCardRequest request = new UseCardRequest(fullGameSession.getGameSessionName(), DevCard.KNIGHT, mock(ResourceEnumMap.class));
            insertSenderIntoRequest(request, sender.getPlayerName());
            fullGameSession.userInput(request);
            // robber Placing Test
            fullGameSession.setCurrentState(PlayState.INSTANCE);
        }
    }

    /**
     * SubClass for Game End tests of the current GameSession
     */
    @Nested
    class GameEndTest {

        @Test
        void updateVictoryPointsTest() {
            players[0].updateBuildingVictoryPoints(4);
            assertEquals(4, players[0].getNumOfPublicVP());
        }

        @Test
        void checkVictoryTest() {
            players[0].updateBuildingVictoryPoints(10);
            fullGameSession.checkVictory();
            assertTrue(event instanceof GameOverMessage);
        }

        @Test
        void addLongestRoadPointTest() {
            players[0].updateBuildingVictoryPoints(4);
            RoadPiece road = new RoadPiece(players[0]);
            board.addPiece(VALID_EDGE, road);
            board.addPiece(new Coord(0, 0, Direction.WEST, MapNode.EDGE), road);
            board.addPiece(new Coord(0, 0, Direction.SOUTH, MapNode.EDGE), road);
            board.addPiece(new Coord(0, 1, Direction.NORTH, MapNode.EDGE), road);
            board.addPiece(new Coord(1, 0, Direction.SOUTH, MapNode.EDGE), road);
            board.findLongestRoadFromEdge(VALID_EDGE, players[0]);

            assertEquals(6, players[0].getNumOfTotalVP());

        }

        @Test
        void getStandingsTest() {
            var testPlayers = fullGameSession.getPlayers();

            testPlayers[1].updateBuildingVictoryPoints(6);
            testPlayers[0].updateBuildingVictoryPoints(4);
            testPlayers[3].updateBuildingVictoryPoints(2);
            testPlayers[2].updateBuildingVictoryPoints(0);

            var standings = fullGameSession.getStandings();

            assertEquals(standings.get(0).getPlayerName(), testPlayers[1].getPlayerName());
            assertEquals(standings.get(1).getPlayerName(), testPlayers[0].getPlayerName());
            assertEquals(standings.get(2).getPlayerName(), testPlayers[3].getPlayerName());
            assertEquals(standings.get(3).getPlayerName(), testPlayers[2].getPlayerName());

        }
    }

    @Nested
    class AssignColorTest {
        @Test
        void everyColorIsAssignedOnlyOnce() {
            int blueOccurrence = 0;
            int greenOccurrence = 0;
            int yellowOccurrence = 0;
            int redOccurrence = 0;
            Player[] testPlayers = fullGameSession.getPlayers();
            for (Player player : testPlayers) {
                PlayerColor color = player.getColor();
                switch (color) {
                    case RED:
                        redOccurrence++;
                        break;
                    case BLUE:
                        blueOccurrence++;
                        break;
                    case GREEN:
                        greenOccurrence++;
                        break;
                    case YELLOW:
                        yellowOccurrence++;
                        break;
                }
            }
            assertEquals(1, blueOccurrence);
            assertEquals(1, redOccurrence);
            assertEquals(1, greenOccurrence);
            assertEquals(1, yellowOccurrence);
        }

        @Test
        void everyPlayerIsAssignedToAColor() {
            Player[] testPlayers = fullGameSession.getPlayers();
            for (Player player : testPlayers) {
                assertNotNull(player.getColor());
            }
        }
    }

    @Nested
    class LeaveGameSessionTest {
        @Test
        void userCanNotLeave_UserIsNoPartOfTheGameSession() {
            User test5 = new UserDTO("test5", "test", "test@test.com");
            assertFalse(fullGameSession.getUsers().contains(test5));
            Session session = mock(Session.class);
            when(session.getUser()).thenReturn(test5);
            Assertions.assertThrows(UserIsNotPartOfGameSessionException.class, () -> fullGameSession.leaveGameSession(session));
        }

        @Test
        void userCanLeave_UserIsNoPartOfTheGameSessionAnymore() throws UserIsNotPartOfGameSessionException {
            assertTrue(fullGameSession.getUsers().contains(test1));
            assertEquals("test1", fullGameSession.getPlayers()[0].getPlayerName());
            Session session = mock(Session.class);
            when(session.getUser()).thenReturn(test1);
            fullGameSession.leaveGameSession(session);
            assertFalse(fullGameSession.getUsers().contains(test1));
            assertNotEquals("test1", fullGameSession.getPlayers()[0].getPlayerName());
        }

        @Test
        void userCanLeave_UserIsReplacedByABot() throws UserIsNotPartOfGameSessionException {
            assertNotEquals("Bot0", fullGameSession.getPlayers()[0].getPlayerName());
            assertEquals(0, fullGameSession.getAmountOfBots());
            Session session = mock(Session.class);
            when(session.getUser()).thenReturn(test1);
            fullGameSession.leaveGameSession(session);
            assertEquals("Bot0", fullGameSession.getPlayers()[0].getPlayerName());
            assertEquals(1, fullGameSession.getAmountOfBots());
        }
    }

    @Nested
    class RejoinGameSessionTest {

        @Test
        void userCanNotRejoin_WereNoPartOfTheGameSession() {
            User test5 = new UserDTO("test5", "test", "test@test.com");

            Assertions.assertThrows(CanNotRejoinException.class, () -> fullGameSession.rejoinGameSession(test5));
        }

        @Test
        void userCanRejoin() throws UserIsNotPartOfGameSessionException, CanNotRejoinException {
            Session session = mock(Session.class);
            when(session.getUser()).thenReturn(test1);
            fullGameSession.leaveGameSession(session);
            fullGameSession.advanceTurn();
            fullGameSession.rejoinGameSession(test1);
            assertTrue(fullGameSession.getUsers().contains(test1));
            assertEquals("test1", fullGameSession.getPlayers()[0].getPlayerName());
        }
    }

    @Nested
    class DiceTest {

        @BeforeEach
        void registerBus() {
            fullGameSession.setCurrentState(DiceState.INSTANCE);
        }

        private RollDiceRequest createRollDiceRequest(int diceValue) {
            fullGameSession.getContext().setNextDiceResult(diceValue);
            RollDiceRequest request = new RollDiceRequest("");
            insertSenderIntoRequest(request, fullGameSession.getWhoseTurn().getPlayerName());
            return request;
        }

        @Test
        @DisplayName("Dice throw with settlements at hex should increment resources by one each")
        void diceThrowWithSettlement_ShouldIncrementByOne() throws GameStateException {
            Collection<Coord> hexCoords = board.getHexagonNumbers().get(2);
            Coord hexCoord = hexCoords.iterator().next();
            SettlementPiece settlementPiece = new SettlementPiece(fullGameSession.getPlayer(0));
            board.addPiece(Coord.getCornersFromHex(hexCoord)[0], settlementPiece);
            SettlementPiece settlementPiece2 = new SettlementPiece(fullGameSession.getPlayer(1));
            board.addPiece(Coord.getCornersFromHex(hexCoord)[1], settlementPiece2);
            SettlementPiece settlementPiece3 = new SettlementPiece(fullGameSession.getPlayer(1));
            board.addPiece(Coord.getCornersFromHex(hexCoord)[2], settlementPiece3);

            int first = fullGameSession.getPlayer(0).getInventory().getNumOfResourceCards();
            int second = fullGameSession.getPlayer(1).getInventory().getNumOfResourceCards();
            fullGameSession.userInput(createRollDiceRequest(2));
            assertEquals(first + 1, fullGameSession.getPlayer(0).getInventory().getNumOfResourceCards());
            assertEquals(second + 2, fullGameSession.getPlayer(1).getInventory().getNumOfResourceCards());
        }
    }
}
