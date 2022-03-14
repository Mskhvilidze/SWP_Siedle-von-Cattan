package de.uol.swp.common.game.board;

/**
 * The resources that a hex tile can have. Desert tiles do not have a resource
 */
public enum ResourceTile {
    LUMBER,
    WOOL,
    GRAIN,
    ORE,
    BRICK,
    DESERT;

    /**
     * Checks whether a given resource tile is a desert tile
     *
     * @param resourceTile the {@code ResourceTile} to check
     * @return {@code true} if the given resource tile is a desert tile, otherwise {@code false}
     */
    public static boolean isDesertTile(ResourceTile resourceTile) {
        return resourceTile.equals(DESERT);
    }

    /**
     * Converts a ResourceTile in ResourceType
     *
     * @return ResourceType
     */

    public static ResourceType toResourceType(ResourceTile tile){
        switch (tile){
            case LUMBER:
                return ResourceType.LUMBER;
            case BRICK:
                return ResourceType.BRICK;
            case ORE:
                return ResourceType.ORE;
            case WOOL:
                return ResourceType.WOOL;
            case GRAIN:
                return ResourceType.GRAIN;
            default:
                return null;
        }
    }
}
