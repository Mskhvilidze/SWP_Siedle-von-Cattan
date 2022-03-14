package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.Inventory;
import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.user.User;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class InventoryServiceTest {
    static final User OWNER = new UserDTO("testOwner", "test", "test@test.com");
    static final GameLobby GAME_LOBBY = new GameLobby("gameLobby", OWNER, false);
    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final EventBus bus = new EventBus();
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, mock(AuthenticationService.class));
    final InventoryService inventoryService = new InventoryService(bus, gameSessionService);
    final GameSession gameSession = new GameSession(GAME_LOBBY, gameSessionService, inventoryService, bus);

    final Player[] players = gameSession.getPlayers();
    final Player playerOne = players[0];
    final Inventory playerOneInventory = playerOne.getInventory();
    final ResourceEnumMap resourceMapOne = new ResourceEnumMap(1, 1, 1, 1, 1);
    final ResourceEnumMap resourceMapTwo = new ResourceEnumMap(20, 20, 20, 20, 20);
    final DevCardEnumMap devCardMapOne = new DevCardEnumMap(1, 1, 1, 1, 1);

    @BeforeEach
    void setup() {
        ResourceEnumMap resourceEnumMap = new ResourceEnumMap(10, 10, 10, 10, 10);
        DevCardEnumMap devCardEnumMap = new DevCardEnumMap(2, 2, 2, 2, 2);
        for (Player player : players) {
            inventoryService.updateResources(player, resourceEnumMap);
            inventoryService.updateDevCards(player, devCardEnumMap);
        }
    }

    @Test
    void decreaseResourcesForBuildablePieces() {

    }

    @Test
    void testUpdateResources() {
        inventoryService.updateResources(playerOne, resourceMapOne);
        assertEquals(1, playerOneInventory.getBrick());
        assertEquals(1, playerOneInventory.getGrain());
        assertEquals(1, playerOneInventory.getLumber());
        assertEquals(1, playerOneInventory.getWool());
        assertEquals(1, playerOneInventory.getOre());
        assertEquals(5, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testIncreaseResources() {
        inventoryService.increaseResources(playerOne, resourceMapOne);
        assertEquals(11, playerOneInventory.getBrick());
        assertEquals(11, playerOneInventory.getGrain());
        assertEquals(11, playerOneInventory.getLumber());
        assertEquals(11, playerOneInventory.getWool());
        assertEquals(11, playerOneInventory.getOre());
        assertEquals(55, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testUpdateDevCards() {
        inventoryService.updateDevCards(playerOne, devCardMapOne);
        assertEquals(1, playerOneInventory.getRoadBuildingCardCount());
        assertEquals(1, playerOneInventory.getYearOfPlentyCardCount());
        assertEquals(1, playerOneInventory.getVictoryPointCardCount());
        assertEquals(1, playerOneInventory.getMonopolyCardCount());
        assertEquals(1, playerOneInventory.getKnightCardCount());
    }

    @Test
    void testSetResource() {
        inventoryService.setResource(playerOne, ResourceType.LUMBER, 99);
        assertEquals(99, playerOneInventory.getLumber());
    }

    @Test
    void testSetResources() {
        inventoryService.setResources(playerOne, resourceMapOne);
        assertEquals(1, playerOneInventory.getBrick());
        assertEquals(1, playerOneInventory.getGrain());
        assertEquals(1, playerOneInventory.getLumber());
        assertEquals(1, playerOneInventory.getWool());
        assertEquals(1, playerOneInventory.getOre());
        assertEquals(5, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testIncreaseResource() {
        inventoryService.increaseResource(playerOne, ResourceType.LUMBER, 2);
        assertEquals(12, playerOneInventory.getLumber());
    }

    @Test
    void testDecreaseResource() throws OverDrawException {
        inventoryService.decreaseResource(playerOne, ResourceType.LUMBER, 2);
        assertEquals(8, playerOneInventory.getLumber());
    }

    @Test
    void testIncreaseLumber() {
        inventoryService.increaseLumber(playerOne, 90);
        assertEquals(100, playerOneInventory.getLumber());
    }

    @Test
    void testIncreaseOre() {
        inventoryService.increaseOre(playerOne, 90);
        assertEquals(100, playerOneInventory.getOre());
    }

    @Test
    void testIncreaseBrick() {
        inventoryService.increaseBrick(playerOne, 90);
        assertEquals(100, playerOneInventory.getBrick());
    }

    @Test
    void testIncreaseWool() {
        inventoryService.increaseWool(playerOne, 90);
        assertEquals(100, playerOneInventory.getWool());
    }

    @Test
    void testIncreaseGrain() {
        inventoryService.increaseGrain(playerOne, 90);
        assertEquals(100, playerOneInventory.getGrain());
    }

    @Test
    void testAddDevCard() {
        inventoryService.addDevCard(playerOne, DevCard.KNIGHT);
        assertEquals(3, playerOneInventory.getKnightCardCount());
    }

    @Test
    void testAddResources() {
        inventoryService.addResources(playerOne, resourceMapOne);
        assertEquals(11, playerOneInventory.getBrick());
        assertEquals(11, playerOneInventory.getGrain());
        assertEquals(11, playerOneInventory.getLumber());
        assertEquals(11, playerOneInventory.getWool());
        assertEquals(11, playerOneInventory.getOre());
        assertEquals(55, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testRemoveResources() throws OverDrawException {
        inventoryService.removeResources(playerOne, resourceMapOne);
        assertEquals(9, playerOneInventory.getBrick());
        assertEquals(9, playerOneInventory.getGrain());
        assertEquals(9, playerOneInventory.getLumber());
        assertEquals(9, playerOneInventory.getWool());
        assertEquals(9, playerOneInventory.getOre());
        assertEquals(45, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testSetVictoryPointCard() {
        playerOne.getInventory().setVictoryPointCard(5);
        assertEquals(5, playerOneInventory.getVictoryPointCardCount());
    }

    @Test
    void testRemoveRandomResources() throws OverDrawException {
        inventoryService.removeRandomResources(playerOne, 10);
        assertEquals(40, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testDecreaseResourcesSETTLEMENT() throws OverDrawException {
        inventoryService.decreaseResources(playerOne, PieceType.SETTLEMENT);
        assertEquals(9, playerOneInventory.getBrick());
        assertEquals(9, playerOneInventory.getGrain());
        assertEquals(9, playerOneInventory.getLumber());
        assertEquals(9, playerOneInventory.getWool());
        assertEquals(46, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testDecreaseResourcesROAD() throws OverDrawException {
        inventoryService.decreaseResources(playerOne, PieceType.ROAD);
        assertEquals(9, playerOneInventory.getBrick());
        assertEquals(9, playerOneInventory.getLumber());
        assertEquals(48, playerOneInventory.getNumOfResourceCards());
    }

    @Test
    void testDecreaseResourcesCITY() throws OverDrawException {
        inventoryService.decreaseResources(playerOne, PieceType.CITY);
        assertEquals(8, playerOneInventory.getGrain());
        assertEquals(7, playerOneInventory.getOre());
        assertEquals(45, playerOneInventory.getNumOfResourceCards());
    }
}
