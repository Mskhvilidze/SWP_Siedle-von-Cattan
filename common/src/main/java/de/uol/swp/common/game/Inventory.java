package de.uol.swp.common.game;

import de.uol.swp.common.game.board.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to store the number of resource and development cards a player has
 */
public class Inventory {

    private final ResourceEnumMap resources = new ResourceEnumMap();
    private final DevCardEnumMap devCards = new DevCardEnumMap();
    private final PieceTypeEnumMap availablePieces = new PieceTypeEnumMap();
    private final DevCardEnumMap playableDevCards = new DevCardEnumMap();

    /**
     * Constructor
     */
    public Inventory() {
        resources.put(ResourceType.LUMBER, 0);
        resources.put(ResourceType.WOOL, 0);
        resources.put(ResourceType.GRAIN, 0);
        resources.put(ResourceType.ORE, 0);
        resources.put(ResourceType.BRICK, 0);
        availablePieces.put(PieceType.ROAD, 15);
        availablePieces.put(PieceType.CITY, 4);
        availablePieces.put(PieceType.SETTLEMENT, 5);
    }

    /**
     * Increases the amount of the given dev card in this inventory by one
     *
     * @param devCard the dev card that should be added to this inventory
     */
    public void addDevCard(DevCard devCard) {
        devCards.put(devCard, devCards.get(devCard) + 1);
    }

    /**
     * Increases the amount of the given dev card in this inventory by one
     *
     * @param devCard the dev card that should be added to this inventory
     */
    public void removeDevCard(DevCard devCard) {
        devCards.put(devCard, devCards.get(devCard) - 1);
    }

    /**
     * Returns the amount of resourceTypes a player has from a certain resource.
     *
     * @return amount of resourceTypes of a resource
     */
    public int getResource(ResourceType resourceType) {
        return resources.get(resourceType);
    }

    /**
     * Sets the quantity of a given resource
     * <p>
     * To increase/decrease the quantity use
     *
     * @param resource the resource that is set
     * @param count    the amount to which the resource is set
     */
    public void setResource(ResourceType resource, int count) {
        resources.put(resource, count);
    }

    /**
     * Increases the stored quantity of a given resource
     *
     * @param resource the resource that is increased
     * @param count    the amount by which the resource is increased
     */
    public void increaseResource(ResourceType resource, int count) {
        resources.put(resource, resources.get(resource) + count);
    }

    /**
     * Decreases the stored quantity of a given resource. Will be capped at 0
     *
     * @param resource the resource that is decreased
     * @param count    the amount by which the resource is decreased
     */
    public void decreaseResource(ResourceType resource, int count) {
        resources.put(resource, Math.max(resources.get(resource) - count, 0));
    }

    /**
     * Returns whether enough of a given resource is stored in this inventory
     *
     * @param resource the resource that is checked
     * @param count    the minimum quantity of the resource that should be stored
     * @return {@code true} if the resource quantity in this inventory is greater or equal to the given count, otherwise {@code false}
     */
    public boolean hasResource(ResourceType resource, int count) {
        return resources.get(resource) >= count;
    }

    /**
     * Returns whether enough of the given resources are stored in this inventory
     *
     * @param resources a {@code ResourceMap} that stores the resources that are checked
     * @return {@code true} if the resource quantity in this inventory is greater or equal to the given resources, otherwise {@code false}
     */
    public boolean hasResources(ResourceEnumMap resources) {
        return this.resources.hasResources(resources);
    }

    /**
     * Returns a {@code ResourceMap} representing the quantity of all resources stored in this inventory
     *
     * @return a {@code ResourceMap} representing the quantity of all resources stored in this inventory
     */
    public ResourceEnumMap getResources() {
        return resources.copy();
    }

    /**
     * Decreases number of settlements and checks if >= 0
     */
    public void decrNumOfAvailableSettlements() {
        availablePieces.put(PieceType.SETTLEMENT, availablePieces.get(PieceType.SETTLEMENT) - 1);
    }

    /**
     * Decreases number of cities and checks if >= 0
     */
    public void decrNumOfAvailableCities() {
        availablePieces.put(PieceType.CITY, availablePieces.get(PieceType.CITY) - 1);
    }

    /**
     * Decreases number of roads and checks if >= 0
     */
    public void decrNumOfAvailableRoads() {
        availablePieces.put(PieceType.ROAD, availablePieces.get(PieceType.ROAD) - 1);
    }

    /**
     * Increases the number of settlements
     */
    public void incrNumOfAvailableSettlements() {
        availablePieces.put(PieceType.SETTLEMENT, availablePieces.get(PieceType.SETTLEMENT) + 1);
    }

    /**
     * Returns the number of roads
     *
     * @return number of roads
     */
    public int getNumOfAvailableRoads() {
        return availablePieces.get(PieceType.ROAD);
    }

    /**
     * Returns the number of buildableObjects
     *
     * @return number of buildableObjects
     */
    public PieceTypeEnumMap getAvailablePieces() {
        return availablePieces;
    }

    /**
     * Returns the number of settlements
     *
     * @return number of settlements
     */
    public int getNumOfAvailableSettlements() {
        return availablePieces.get(PieceType.SETTLEMENT);
    }

    /**
     * Returns the number of cities
     *
     * @return number of cities
     */
    public int getNumOfAvailableCities() {
        return availablePieces.get(PieceType.CITY);
    }

    /**
     * Returns the number of lumber
     *
     * @return number of lumber
     */
    public int getLumber() {
        return resources.get(ResourceType.LUMBER);
    }

    /**
     * Set the number of lumber
     *
     * @param lumber the new number of lumber
     */
    public void setLumber(int lumber) {
        resources.put(ResourceType.LUMBER, lumber);
    }

    /**
     * Returns the number of wool
     *
     * @return number of wool
     */
    public int getWool() {
        return resources.get(ResourceType.WOOL);
    }

    /**
     * Set the number of wool
     *
     * @param wool the new number of wool
     */
    public void setWool(int wool) {
        resources.put(ResourceType.WOOL, wool);
    }

    /**
     * Returns the number of grain
     *
     * @return number of grain
     */
    public int getGrain() {
        return resources.get(ResourceType.GRAIN);
    }

    /**
     * Set the number of grain
     *
     * @param grain the new number of grain
     */
    public void setGrain(int grain) {
        resources.put(ResourceType.GRAIN, grain);
    }

    /**
     * Returns the number of ore
     *
     * @return number of ore
     */
    public int getOre() {
        return resources.get(ResourceType.ORE);
    }

    /**
     * Set the number of ore
     *
     * @param ore the new number of ore
     */
    public void setOre(int ore) {
        resources.put(ResourceType.ORE, ore);
    }

    /**
     * Returns the number of brick
     *
     * @return number of brick
     */
    public int getBrick() {
        return resources.get(ResourceType.BRICK);
    }

    /**
     * Set the number of brick
     *
     * @param brick the new number of brick
     */
    public void setBrick(int brick) {
        resources.put(ResourceType.BRICK, brick);
    }

    /**
     * Returns the number of cards
     *
     * @return number of cards
     */
    public int getNumOfResourceCards() {
        return (getLumber() + getWool() + getGrain() + getOre() + getBrick());
    }

    /**
     * Returns the number of knight cards in the inventory
     *
     * @return the number of knight cards in the inventory
     */
    public int getKnightCardCount() {
        return devCards.get(DevCard.KNIGHT);
    }

    /**
     * Set the number of knight cards in the inventory
     *
     * @param count the new number of knight cards in the inventory
     */
    public void setKnight(int count) {
        devCards.put(DevCard.KNIGHT, count);
    }

    /**
     * Returns the number of roadBuilding cards in the inventory
     *
     * @return the number of roadBuilding cards in the inventory
     */
    public int getRoadBuildingCardCount() {
        return devCards.get(DevCard.ROAD_BUILDING);
    }

    /**
     * Set the number of roadBuilding cards in the inventory
     *
     * @param count the new number of roadBuilding cards in the inventory
     */
    public void setRoadBuilding(int count) {
        devCards.put(DevCard.ROAD_BUILDING, count);
    }

    /**
     * Returns the number of yearOfPlenty cards in the inventory
     *
     * @return the number of yearOfPlenty cards in the inventory
     */
    public int getYearOfPlentyCardCount() {
        return devCards.get(DevCard.YEAR_OF_PLENTY);
    }

    /**
     * Set the number of yearOfPlenty
     *
     * @param count the new number of yearPlenty cards in the inventory
     */
    public void setYearOfPlenty(int count) {
        devCards.put(DevCard.YEAR_OF_PLENTY, count);
    }

    /**
     * Returns the number of Monopoly cards in the inventory
     *
     * @return the number of Monopoly cards in the inventory
     */
    public int getMonopolyCardCount() {
        return devCards.get(DevCard.MONOPOLY);
    }

    /**
     * Set the number of monopoly cards in the inventory
     *
     * @param count the new number of monopoly cards in the inventory
     */
    public void setMonopoly(int count) {
        devCards.put(DevCard.MONOPOLY, count);
    }

    /**
     * Updates the playableDevCards
     * <p>
     * At the beginning of each turn the a copy of the devCards is made, so this map doesn't include cards,
     * the player has bought this turn and because of this are not allowed to be played
     */
    public void updatePlayableDevCards() {
        for (Map.Entry<DevCard, Integer> entry : devCards.entrySet()) {
            if (entry.getKey() != DevCard.VP) {
                playableDevCards.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Getter for the playableDevCards: this Map doesn't include the cards the player bought this turn
     *
     * @return the devCards the player is allowed to use
     */
    public DevCardEnumMap getPlayableCards() {
        return playableDevCards;
    }

    /**
     * Returns the number of victoryPointCard cards in the inventory
     *
     * @return the number of victoryPointCard cards in the inventory
     */
    public int getVictoryPointCardCount() {
        return devCards.get(DevCard.VP);
    }

    /**
     * Set the number of victoryPoint cards in the inventory
     *
     * @param count the new number of victoryPoint cards in the inventory
     */
    public void setVictoryPointCard(int count) {
        devCards.put(DevCard.VP, count);
    }

    /**
     * Returns the DevCardEnumMap that contains the DevCards
     *
     * @return the DevCardEnumMap that contains the DevCards
     */
    public DevCardEnumMap getDevCards() {
        return devCards;
    }

    /**
     * Decreases the amount of resources as given by the Resource enum Map.
     *
     * @param toDiscard
     */
    public void decreaseResources(ResourceEnumMap toDiscard) {
        toDiscard.forEach(this::decreaseResource);
    }

    /**
     * Remove amount of Random ressources
     *
     * @param amount amount removed
     * @return the last removed resource
     */
    public List<ResourceType> removeRandomResources(int amount) {
        int amountLeft = amount;
        List<ResourceType> removedResource = new ArrayList<>();
        while (amountLeft > 0) {
            ResourceType randomType = ResourceType.getRandom();
            if (hasResource(randomType, 1)) {
                decreaseResource(randomType, 1);
                removedResource.add(randomType);
                amountLeft--;
            }
            if (resources.sumOfResources() <= 0) break;
        }
        return removedResource;
    }


}