package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.game.Inventory;
import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.message.inventory.BuildableObjectMessage;
import de.uol.swp.common.game.message.inventory.DevCardCountMessage;
import de.uol.swp.common.game.message.inventory.DevCardDetailedCountMessage;
import de.uol.swp.common.game.message.inventory.ResourceCardCountMessage;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.exception.OverDrawException;
import de.uol.swp.server.game.session.GameSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Service to manage every transaction that alters the inventory
 */
@Singleton
public class InventoryService extends AbstractService {

    private static final ResourceEnumMap SETTLEMENT_COST = new ResourceEnumMap(1, 1, 1, 0, 1);
    private static final ResourceEnumMap CITY_COST = new ResourceEnumMap(0, 0, 2, 3, 0);
    private static final ResourceEnumMap ROAD_COST = new ResourceEnumMap(1, 0, 0, 0, 1);
    private static final Logger LOG = LogManager.getLogger(InventoryService.class);
    private final GameSessionService gameSessionService;

    /**
     * Constructor called by guice
     *
     * @param bus                the EvenBus used throughout the server
     * @param gameSessionService the instance of {@code GameSessionService} injected by guice
     */
    @Inject
    public InventoryService(EventBus bus, GameSessionService gameSessionService) {
        super(bus);
        this.gameSessionService = gameSessionService;
    }

    private void postResourcesToGameSession(Player player, ResourceEnumMap resources) {
        var message = new ResourceCardCountMessage(player.getGameSessionName(), player.createDTO(), resources);
        gameSessionService.sendToAllInGameSession(player.getGameSessionName(), message);
    }

    private void postBuildableObjectsToGameSession(Player player, PieceTypeEnumMap piece) {
        var message = new BuildableObjectMessage(player.getGameSessionName(), player.createDTO(), piece);
        gameSessionService.sendToAllInGameSession(player.getGameSessionName(), message);
    }

    private void postDevCardsToGameSession(Player player, DevCardEnumMap devCards) {
        var playerMessage = new DevCardDetailedCountMessage(player.getGameSessionName(), devCards);
        gameSessionService.sendToPlayerInGameSession(player.getGameSessionName(), playerMessage, player.getPlayerName(), false);
        int count = 0;
        for (Map.Entry<DevCard, Integer> entry : devCards.entrySet()) {
            count += entry.getValue();
        }
        var sessionMessage = new DevCardCountMessage(player.getGameSessionName(), player.createDTO(), count);
        gameSessionService.sendToAllInGameSession(player.getGameSessionName(), sessionMessage);
    }

    /**
     * Overwrites the current resources with the number from the resourceEnumMap
     */
    public void updateResources(Player player, ResourceEnumMap resourceEnumMap) {
        var inventory = player.getInventory();
        inventory.setLumber(resourceEnumMap.get(ResourceType.LUMBER));
        inventory.setBrick(resourceEnumMap.get(ResourceType.BRICK));
        inventory.setGrain(resourceEnumMap.get(ResourceType.GRAIN));
        inventory.setWool(resourceEnumMap.get(ResourceType.WOOL));
        inventory.setOre(resourceEnumMap.get(ResourceType.ORE));
        postResourcesToGameSession(player, resourceEnumMap);
    }

    /**
     * Adds the given resources to the inventory
     */
    public void increaseResources(Player player, ResourceEnumMap resourceEnumMap) {
        var inventory = player.getInventory();
        inventory.increaseResource(ResourceType.LUMBER, resourceEnumMap.get(ResourceType.LUMBER));
        inventory.increaseResource(ResourceType.BRICK, resourceEnumMap.get(ResourceType.BRICK));
        inventory.increaseResource(ResourceType.GRAIN, resourceEnumMap.get(ResourceType.GRAIN));
        inventory.increaseResource(ResourceType.WOOL, resourceEnumMap.get(ResourceType.WOOL));
        inventory.increaseResource(ResourceType.ORE, resourceEnumMap.get(ResourceType.ORE));
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Sets the value of a ResourceType in the players inventory to the given number
     */
    public void setResource(Player player, ResourceType resource, int count) {
        var inventory = player.getInventory();
        inventory.setResource(resource, count);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Sets the values of the ResourceEnumMap in the players inventory to the given number
     */
    public void setResources(Player player, ResourceEnumMap resourceMap) {
        var inventory = player.getInventory();
        resourceMap.forEach(inventory::setResource);
        postResourcesToGameSession(player, resourceMap);
    }

    /**
     * Increases the number of a ResourceType in the players inventory for the given number
     */
    public void increaseResource(Player player, ResourceType resource, int count) {
        var inventory = player.getInventory();
        inventory.increaseResource(resource, count);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Decreases the number of a ResourceType in the players inventory for the given number
     */
    public void decreaseResource(Player player, ResourceType resource, int count) throws OverDrawException {
        var inventory = player.getInventory();
        if (inventory.getResource(resource) >= count) {
            inventory.decreaseResource(resource, count);
            postResourcesToGameSession(player, inventory.getResources());
        } else {
            throw new OverDrawException("Can not decrease " + resource.toString() + "because it is already 0");
        }
    }

    /**
     * Increases amount of lumber a certain player has
     */
    public void increaseLumber(Player player, int lumber) {
        var inventory = player.getInventory();
        inventory.increaseResource(ResourceType.LUMBER, lumber);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Increases amount of wool a certain player has
     */
    public void increaseWool(Player player, int wool) {
        var inventory = player.getInventory();
        inventory.increaseResource(ResourceType.WOOL, wool);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Increases amount of grain a certain player has
     */
    public void increaseGrain(Player player, int grain) {
        var inventory = player.getInventory();
        inventory.increaseResource(ResourceType.GRAIN, grain);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Increases amount of ore a certain player has
     */
    public void increaseOre(Player player, int ore) {
        var inventory = player.getInventory();
        inventory.increaseResource(ResourceType.ORE, ore);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Increases amount of brick a certain player has
     */
    public void increaseBrick(Player player, int brick) {
        var inventory = player.getInventory();
        inventory.increaseResource(ResourceType.BRICK, brick);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Adds the Resources from the ResourceEnumMap to the players inventory
     */
    public void addResources(Player player, ResourceEnumMap resourceMap) {
        var inventory = player.getInventory();
        resourceMap.forEach(inventory::increaseResource);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Removes the Resources from the ResourceEnumMap from the players inventory
     */
    public void removeResources(Player player, ResourceEnumMap resourceMap) throws OverDrawException {
        var inventory = player.getInventory();
        if (inventory.hasResources(resourceMap)) {
            resourceMap.forEach(inventory::decreaseResource);
            postResourcesToGameSession(player, inventory.getResources());
        } else {
            throw new OverDrawException("Can not remove all resources from the ResourceEnumMap cause the player does not have enough resources");
        }
    }

    /**
     * Adds and removes the given resources without sending an update message to the clients inbetween
     *
     * @param player            the player whose resources will be altered
     * @param resourcesToAdd    the resources that will be added
     * @param resourcesToRemove the resources that will be removed
     * @throws OverDrawException if the player does not have enough resources to remove, will be checked before adding the given resources
     */
    public void addAndRemoveResources(Player player, ResourceEnumMap resourcesToAdd, ResourceEnumMap resourcesToRemove) throws OverDrawException {
        var inventory = player.getInventory();
        if (inventory.hasResources(resourcesToRemove)) {
            resourcesToRemove.forEach(inventory::decreaseResource);
        } else {
            throw new OverDrawException("Can not remove all resources from the ResourceEnumMap cause the player does not have enough resources");
        }
        resourcesToAdd.forEach(inventory::increaseResource);
        postResourcesToGameSession(player, inventory.getResources());
    }

    /**
     * Removes the a random Resource from the players inventory
     */
    public List<ResourceType> removeRandomResources(Player player, int amount) throws OverDrawException {
        var inventory = player.getInventory();
        List<ResourceType> removedResource;
        if (inventory.getNumOfResourceCards() >= amount) {
            removedResource = inventory.removeRandomResources(amount);
            postResourcesToGameSession(player, inventory.getResources());
        } else {
            throw new OverDrawException(
                    "Can not remove " + amount + "resources cause the player only got " + inventory.getNumOfResourceCards() + "ResourceCards");
        }
        return removedResource;
    }

    /**
     * Decreases the players Inventory with the cost of the given PieceType and
     * increases or decreases the number of buildable Pieces depending on the case
     *
     * @param player the affected player
     * @param piece  the piece with price is used
     */
    public void decreaseResources(Player player, PieceType piece) throws OverDrawException {
        Inventory inventory = player.getInventory();
        switch (piece) {
            case SETTLEMENT:
                if (inventory.hasResources(SETTLEMENT_COST)) {
                    inventory.decreaseResources(SETTLEMENT_COST);
                    inventory.decrNumOfAvailableSettlements();
                    postResourcesToGameSession(player, inventory.getResources());
                    postBuildableObjectsToGameSession(player, inventory.getAvailablePieces());
                } else {
                    throw new OverDrawException("Can not build a settlement, because the player does not have enough resources");
                }
                break;
            case ROAD:
                if (inventory.hasResources(ROAD_COST)) {
                    inventory.decreaseResources(ROAD_COST);
                    inventory.decrNumOfAvailableRoads();
                    postResourcesToGameSession(player, inventory.getResources());
                    postBuildableObjectsToGameSession(player, inventory.getAvailablePieces());
                } else {
                    throw new OverDrawException("Can not build a road, because the player does not have enough resources");
                }
                break;
            case CITY:
                if (inventory.hasResources(CITY_COST)) {
                    inventory.decreaseResources(CITY_COST);
                    inventory.decrNumOfAvailableCities();
                    inventory.incrNumOfAvailableSettlements();
                    postResourcesToGameSession(player, inventory.getResources());
                    postBuildableObjectsToGameSession(player, inventory.getAvailablePieces());
                    break;
                } else {
                    throw new OverDrawException("Can not build a city, because the player does not have enough resources");
                }
        }
    }

    /**
     * Decreases the number of buildable Pieces depending on the case
     *
     * @param player the affected player
     * @param piece  the piece with price is used
     */
    public void decreaseObjects(Player player, PieceType piece) {
        Inventory inventory = player.getInventory();
        switch (piece) {
            case ROAD:
                inventory.decrNumOfAvailableRoads();
                break;
            case SETTLEMENT:
                inventory.decrNumOfAvailableSettlements();
                break;
            default:
                LOG.debug("Not able to decrease buildable object.");
        }
        postBuildableObjectsToGameSession(player, inventory.getAvailablePieces());
    }

    /**
     * Overwrites the DevCards with the numbers from the resourceEnumMap
     */
    public void updateDevCards(Player player, DevCardEnumMap devCardEnumMap) {
        var inventory = player.getInventory();
        inventory.setKnight(devCardEnumMap.get(DevCard.KNIGHT));
        inventory.setMonopoly(devCardEnumMap.get(DevCard.MONOPOLY));
        inventory.setRoadBuilding(devCardEnumMap.get(DevCard.ROAD_BUILDING));
        inventory.setVictoryPointCard(devCardEnumMap.get(DevCard.VP));
        inventory.setYearOfPlenty(devCardEnumMap.get(DevCard.YEAR_OF_PLENTY));
        postDevCardsToGameSession(player, devCardEnumMap);
    }

    /**
     * Adds a DevCards to the players inventory
     */
    public void addDevCard(Player player, DevCard devCard) {
        var inventory = player.getInventory();
        inventory.addDevCard(devCard);
        postDevCardsToGameSession(player, inventory.getDevCards());
    }

    /**
     * Adds a DevCards to the players inventory
     */
    public void removeDevCard(Player player, DevCard devCard) {
        var inventory = player.getInventory();
        inventory.removeDevCard(devCard);
        postDevCardsToGameSession(player, inventory.getDevCards());
    }
}
