package de.uol.swp.common.game.board;

import java.util.EnumMap;

/**
 * Custom implementation of EnumMap that allows for easier control over resources
 */
public class ResourceEnumMap extends EnumMap<ResourceType, Integer> {

    /**
     * Constructs a ResourceEnumMap with all resources quantities set to 0
     */
    public ResourceEnumMap() {
        super(ResourceType.class);
        putResources(0, 0, 0, 0, 0);
    }

    /**
     * Constructs a ResourceEnumMap with all resources quantities set to the given quantities
     *
     * @param lumber the lumber quantity
     * @param wool   the wool quantity
     * @param grain  the grain quantity
     * @param ore    the ore quantity
     * @param brick  the brick quantity
     */
    public ResourceEnumMap(int lumber, int wool, int grain, int ore, int brick) {
        super(ResourceType.class);
        putResources(lumber, wool, grain, ore, brick);
    }

    /**
     * Sum off all resources in this enumMap
     *
     * @return the sum of the resources
     */
    public int sumOfResources() {
        return get(ResourceType.LUMBER) + get(ResourceType.WOOL) + get(ResourceType.GRAIN) + get(ResourceType.ORE) + get(ResourceType.BRICK);
    }

    /**
     * Puts the given amount of each resource into this map
     *
     * @param lumber the amount of lumber to store
     * @param wool   the amount of wool to store
     * @param grain  the amount of grain to store
     * @param ore    the amount of ore to store
     * @param brick  the amount of brick to store
     */
    protected final void putResources(int lumber, int wool, int grain, int ore, int brick) {
        put(ResourceType.LUMBER, lumber);
        put(ResourceType.WOOL, wool);
        put(ResourceType.GRAIN, grain);
        put(ResourceType.ORE, ore);
        put(ResourceType.BRICK, brick);
    }

    /**
     * Returns whether enough of multiple given resources is stored in this map
     *
     * @param resources a {@code ResourceMap} that stores the resources that are checked
     * @return {@code true} if the resource quantity in this map is greater or equal to the given count, otherwise {@code false}
     */
    public boolean hasResources(ResourceEnumMap resources) {
        for (Entry<ResourceType, Integer> resource : resources.entrySet()) {
            if (resource.getValue() > this.get(resource.getKey())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the content of this map in such a way that the client chat can represent it as a series of images
     *
     * @return the content of this map in such a way that the client chat can represent it as a series of images
     */
    public String toChatFormat() {
        StringBuilder stringBuilder = new StringBuilder();
        forEach((resourceTile, integer) -> {
            for (int i = 0; i < integer; i++) {
                stringBuilder.append('{').append(resourceTile.name().toLowerCase()).append('}');
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Returns a copy of this map
     *
     * @return a copy of this map
     */
    public ResourceEnumMap copy() {
        return new ResourceEnumMap(get(ResourceType.LUMBER), get(ResourceType.WOOL), get(ResourceType.GRAIN), get(ResourceType.ORE),
                get(ResourceType.BRICK));
    }
}
