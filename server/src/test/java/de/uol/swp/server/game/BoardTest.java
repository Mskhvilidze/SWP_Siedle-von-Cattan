package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.Direction;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.board.ResourceTile;
import de.uol.swp.common.game.dto.MapNode;
import de.uol.swp.common.game.dto.PieceDTO;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.user.User;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.mapobject.CityPiece;
import de.uol.swp.server.game.mapobject.RoadPiece;
import de.uol.swp.server.game.mapobject.SettlementPiece;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoardTest {

    static final Coord VALID_EDGE = new Coord(0, 0, Direction.NORTH, MapNode.EDGE);
    static final Coord VALID_CORNER = new Coord(0, 0, Direction.NORTH, MapNode.CORNER);
    static final Coord VALID_HEX = new Coord(0, 0);
    static final Coord INVALID_EDGE = new Coord(4, 4, Direction.NORTH, MapNode.EDGE);
    static final Coord INVALID_CORNER = new Coord(4, 4, Direction.NORTH, MapNode.CORNER);
    static final Coord INVALID_HEX = new Coord(4, 4);
    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final User test1 = new UserDTO("test1", "test", "test@test.com");
    final EventBus bus = new EventBus();
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, mock(AuthenticationService.class));
    final InventoryService inventoryService = new InventoryService(bus, gameSessionService);
    final GameSession fullGameSession = createFullGameSession();

    final Board board = fullGameSession.getBoard();
    Player[] players = fullGameSession.getPlayers();

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

    @Test
    void addPieceRoad() {
        RoadPiece road = new RoadPiece(players[0]);
        assertNull(board.getRoadPiece(VALID_EDGE));
        board.addPiece(VALID_EDGE, road);
        assertEquals(road, board.getRoadPiece(VALID_EDGE));
    }

    @Test
    void addPieceRoadError() {
        RoadPiece road = new RoadPiece(players[0]);
        assertNull(board.getRoadPiece(VALID_EDGE));
        board.addPiece(VALID_EDGE, road);
        assertThrows(IllegalArgumentException.class, () -> board.addPiece(VALID_EDGE, road), "Coord for Road must empty");
    }

    @Test
    void addPieceSettlement() {
        SettlementPiece settlement = new SettlementPiece(players[0]);
        board.addPiece(VALID_CORNER, settlement);
        assertEquals(settlement, board.getCornerPiece(VALID_CORNER));
    }

    @Test
    void addPieceSettlementError() {
        SettlementPiece settlement = new SettlementPiece(players[0]);
        board.addPiece(VALID_CORNER, settlement);
        assertThrows(IllegalArgumentException.class, () -> board.addPiece(VALID_CORNER, settlement), "Coord for settlement must empty");

    }

    @Test
    void addPieceCity() {
        SettlementPiece settlement = new SettlementPiece(players[0]);
        board.addPiece(VALID_CORNER, settlement);
        CityPiece city = new CityPiece(players[0]);
        board.addPiece(VALID_CORNER, city);
        assertEquals(city, board.getCornerPiece(VALID_CORNER));
    }

    @Test
    void addPieceCityError() {
        CityPiece city = new CityPiece(players[0]);
        assertThrows(IllegalArgumentException.class, () -> board.addPiece(VALID_CORNER, city), "Coord for City must not be empty");
        assertNotEquals(city, board.getCornerPiece(VALID_CORNER));
    }

    @Test
    void addPieceCityError2() {
        SettlementPiece settlement = new SettlementPiece(players[0]);
        board.addPiece(VALID_CORNER, settlement);
        CityPiece city = new CityPiece(players[0]);
        board.addPiece(VALID_CORNER, city);
        assertThrows(IllegalArgumentException.class, () -> board.addPiece(VALID_CORNER, city), "Coord for City must contain settlement");
    }

    @Test
    void edgeIsOnMap() {
        assertTrue(board.edgeIsOnMap(VALID_EDGE));
        assertFalse(board.edgeIsOnMap(INVALID_EDGE));
    }

    @Test
    void cornerIsOnMap() {
        assertTrue(board.cornerIsOnMap(VALID_CORNER));
        assertFalse(board.cornerIsOnMap(INVALID_CORNER));
    }

    @Test
    void hexagonIsOnMap() {
        assertTrue(board.hexagonIsOnMap(VALID_HEX));
        assertFalse(board.hexagonIsOnMap(INVALID_HEX));
    }

    @Test
    void getPortFromCoord() {
        //TODO: Compare mit vordefinierter ports liste
    }

    @Test
    void getPortFromCoordError() {
        assertThrows(IllegalArgumentException.class, () -> board.getPortFromCoord(INVALID_CORNER));
    }

    @Test
    void cornerIsAPort() {
//            assertTrue(board.cornerIsAPort(VALID_PORT1));
        //TODO: Compare mit vordefinierter ports liste
    }

    @Test
    void ensureRobberOnDesert() {
        Coord desert = findDesertCoord(board.getResourceTileMap());
        assertEquals(board.getRobber().getCoord(), desert);
    }

    @Test
    void testUpdateRobberPos() {
        Coord notDesert = VALID_HEX;
        Coord desert = findDesertCoord(board.getResourceTileMap());
        if (desert.equals(VALID_HEX)) {
            notDesert = new Coord(1, 1);
        }
        board.updateRobberPos(notDesert);
        assertEquals(board.getRobber().getCoord(), notDesert);
    }


    @Test
    void testHasPlayerSettlementOnCoord() {
        var players = fullGameSession.getPlayers();
        SettlementPiece settlement = new SettlementPiece(players[0]);
        board.addPiece(VALID_CORNER, settlement);
        assertTrue(board.hasPlayerSettlementOnCoord(VALID_CORNER, players[0]));
    }

    @Test
    void testHasPlayerSettlementOnCoordError() {
        var players = fullGameSession.getPlayers();
        SettlementPiece settlement = new SettlementPiece(players[0]);
        board.addPiece(VALID_CORNER, settlement);
        assertFalse(board.hasPlayerSettlementOnCoord(VALID_CORNER, players[1]));
    }

    @Test
    void testContainsOtherSettlementOnAdjacentCoords() {
        var players = fullGameSession.getPlayers();
        SettlementPiece settlement = new SettlementPiece(players[1]);
        board.addPiece(new Coord(1, -1, Direction.SOUTH, MapNode.CORNER), settlement);
        assertTrue(board.containsSettlementOnAdjacentCoords(VALID_CORNER));
    }

    @Test
    void testContainsOtherSettlementOnAdjacentCoordsError() {
        var players = fullGameSession.getPlayers();
        SettlementPiece settlement = new SettlementPiece(players[1]);
        board.addPiece(new Coord(1, -1, Direction.SOUTH, MapNode.CORNER), settlement);
        assertTrue(board.containsSettlementOnAdjacentCoords(VALID_CORNER));
    }

    @Test
    void testContainsPlayerRoadsAdjacentToCorner() {
        var players = fullGameSession.getPlayers();
        RoadPiece road = new RoadPiece(players[0]);
        board.addPiece(VALID_EDGE, road);
        assertTrue(board.containsPlayerRoadsAdjacentToCorner(VALID_EDGE, players[0]));
    }


    @Test
    void testContainsPlayerRoadsAdjacentToCornerError() {
        var players = fullGameSession.getPlayers();
        RoadPiece road = new RoadPiece(players[0]);
        board.addPiece(VALID_EDGE, road);
        assertFalse(board.containsPlayerRoadsAdjacentToCorner(VALID_EDGE, players[1]));
    }

    @Test
    void testContainsPlayerRoadAdjacentToEdge() {
        var players = fullGameSession.getPlayers();
        RoadPiece road = new RoadPiece(players[0]);
        board.addPiece(VALID_EDGE, road);
        assertTrue(board.containsPlayerRoadAdjacentToEdge(new Coord(0, 0, Direction.WEST, MapNode.EDGE), players[0]));
    }

    @Test
    void testContainsPlayerRoadAdjacentToEdgeError() {
        var players = fullGameSession.getPlayers();
        RoadPiece road = new RoadPiece(players[0]);
        board.addPiece(VALID_EDGE, road);
        assertFalse(board.containsPlayerRoadAdjacentToEdge(new Coord(0, 0, Direction.WEST, MapNode.EDGE), players[1]));
    }

    @Test
    void testGetAllPlacedPieces() {
        var players = fullGameSession.getPlayers();
        RoadPiece road = new RoadPiece(players[0]);
        board.addPiece(VALID_EDGE, road);
        SettlementPiece settlement = new SettlementPiece(players[1]);
        board.addPiece(VALID_CORNER, settlement);

        var allPieces = board.getAllPlacedPieces();
        assertEquals(allPieces.get(VALID_EDGE), new PieceDTO(PieceType.ROAD, players[0].createDTO()));
        assertEquals(allPieces.get(VALID_CORNER), new PieceDTO(PieceType.SETTLEMENT, players[1].createDTO()));
        assertEquals(2, allPieces.size());
    }

    private Coord findDesertCoord(Map<Coord, ResourceTile> map) {
        AtomicReference<Coord> returnCoord = new AtomicReference<>();
        map.forEach((coord, tile) -> {
            if (ResourceTile.DESERT == tile) {
                returnCoord.set(coord);
            }
        });
        return returnCoord.get();
    }

    @Nested
    class FindLongestRoad {
        @Test
        @DisplayName("A new road longer than 4 and no other road should set the longest road owner")
        void newRoadLongerThan4_ShouldGiveLongestRoad() {
            assertNull(board.getPlayerWithLongestRoad());
            assertEquals(0, board.getLongestPlayerRoad(players[0]).getPathLength());

            addRoadAndFindLongestRoad(players[0], 5);

            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(5, board.getLongestPlayerRoad(players[0]).getPathLength());
        }

        @Test
        @DisplayName("A new road longer than 4 and longer than longest road should set the longest road owner")
        void newRoadLongerThan4AndLongerThanLongestRoad_ShouldGiveLongestRoad() {
            addRoadAndFindLongestRoad(players[0], 5);

            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(5, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(0, board.getLongestPlayerRoad(players[1]).getPathLength());

            addRoadAndFindLongestRoad(players[1], 6);
            assertEquals(players[1], board.getPlayerWithLongestRoad());
            assertEquals(5, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(6, board.getLongestPlayerRoad(players[1]).getPathLength());
        }

        @Test
        @DisplayName("A new road longer than 4 and shorter than longest road should retain the longest road owner")
        void newRoadLongerThan4AndShorterThanLongestRoad_ShouldNotGiveLongestRoad() {
            addRoadAndFindLongestRoad(players[0], 6);
            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(6, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(0, board.getLongestPlayerRoad(players[1]).getPathLength());

            addRoadAndFindLongestRoad(players[1], 5);
            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(6, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[1]).getPathLength());
        }

        @Test
        @DisplayName("Longest road breaking should nullify the longest road owner")
        void longestRoadBreaking_ShouldNullLongestRoad() {
            addRoadAndFindLongestRoad(players[0], 5);
            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(5, board.getLongestPlayerRoad(players[0]).getPathLength());

            board.addPiece(Coord.newCorner(-1, 1, Direction.NORTH), new SettlementPiece(players[1]));
            board.findLongestRoadAfterBreak(players[0]);
            assertNull(board.getPlayerWithLongestRoad());
            assertEquals(3, board.getLongestPlayerRoad(players[0]).getPathLength());
        }

        @Test
        @DisplayName("Longest road breaking with other player having longest road should set the longest road owner")
        void longestRoadBrokeOtherPlayerHasLongestRoad_ShouldGiveLongestRoad() {
            addRoadAndFindLongestRoad(players[0], 7);
            addRoadAndFindLongestRoad(players[1], 6);
            addRoadAndFindLongestRoad(players[2], 5);

            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(7, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(6, board.getLongestPlayerRoad(players[1]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[2]).getPathLength());

            board.addPiece(Coord.newCorner(-1, 1, Direction.NORTH), new SettlementPiece(players[1]));
            board.findLongestRoadAfterBreak(players[0]);
            assertEquals(players[1], board.getPlayerWithLongestRoad());
            assertEquals(4, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(6, board.getLongestPlayerRoad(players[1]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[2]).getPathLength());
        }

        @Test
        @DisplayName("Longest road breaking with other player having tie should nullify the longest road owner")
        void longestRoadBrokeOtherPlayersHaveTie_ShouldNullLongestRoad() {
            addRoadAndFindLongestRoad(players[0], 6);
            addRoadAndFindLongestRoad(players[1], 5);
            addRoadAndFindLongestRoad(players[2], 5);

            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(6, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[1]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[2]).getPathLength());

            board.addPiece(Coord.newCorner(-1, 1, Direction.NORTH), new SettlementPiece(players[1]));
            board.findLongestRoadAfterBreak(players[0]);
            assertNull(board.getPlayerWithLongestRoad());
            assertEquals(3, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[1]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[2]).getPathLength());
        }

        @Test
        @DisplayName("Longest road breaking with owner having tie should retain the longest road owner")
        void longestRoadBrokeOwnerHasTie_ShouldRetainLongestRoad() {
            addRoadAndFindLongestRoad(players[0], 7);
            addRoadAndFindLongestRoad(players[1], 5);
            addRoadAndFindLongestRoad(players[2], 4);

            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(7, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[1]).getPathLength());
            assertEquals(4, board.getLongestPlayerRoad(players[2]).getPathLength());

            board.addPiece(Coord.newCorner(0, 1, Direction.NORTH), new SettlementPiece(players[1]));
            board.findLongestRoadAfterBreak(players[0]);
            assertEquals(players[0], board.getPlayerWithLongestRoad());
            assertEquals(5, board.getLongestPlayerRoad(players[0]).getPathLength());
            assertEquals(5, board.getLongestPlayerRoad(players[1]).getPathLength());
            assertEquals(4, board.getLongestPlayerRoad(players[2]).getPathLength());
        }

        private void addRoadAndFindLongestRoad(Player player, int length) {
            RoadPiece road = new RoadPiece(player);
            board.addPiece(Coord.newEdge(-2, 1 + player.getPlayerId(), Direction.NORTH), road);
            board.addPiece(Coord.newEdge(-1, player.getPlayerId(), Direction.SOUTH), road);
            board.addPiece(Coord.newEdge(-1, 1 + player.getPlayerId(), Direction.NORTH), road);
            board.addPiece(Coord.newEdge(0, player.getPlayerId(), Direction.SOUTH), road);
            if (length >= 5) board.addPiece(Coord.newEdge(0, 1 + player.getPlayerId(), Direction.NORTH), road);
            if (length >= 6) board.addPiece(Coord.newEdge(1, player.getPlayerId(), Direction.SOUTH), road);
            if (length >= 7) board.addPiece(Coord.newEdge(1, 1 + player.getPlayerId(), Direction.NORTH), road);
            board.findLongestRoadFromEdge(Coord.newEdge(-2, 1 + player.getPlayerId(), Direction.NORTH), player);
        }

        @Test
        void findLongestRoadError() {
            assertThrows(IllegalArgumentException.class, () -> board.findLongestRoadFromEdge(null, players[0]), "coord must not be null");
        }
    }
}
