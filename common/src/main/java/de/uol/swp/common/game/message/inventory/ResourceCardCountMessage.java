package de.uol.swp.common.game.message.inventory;

import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.AbstractGameMessage;

import java.util.Objects;

/**
 * Message that notifies the client about changes in a player's inventory.
 */
public class ResourceCardCountMessage extends AbstractGameMessage {

    private final PlayerDTO player;
    private final ResourceEnumMap resourceEnumMap;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the session
     * @param player          the player who's affected
     * @param resourceEnumMap the current inventory state
     */
    public ResourceCardCountMessage(String gameSessionName, PlayerDTO player, ResourceEnumMap resourceEnumMap) {
        super(gameSessionName);
        this.resourceEnumMap = resourceEnumMap;
        this.player = player;
    }

    /**
     * Returns the resourceEnumMap
     *
     * @return the resourceEnumMap
     */
    public ResourceEnumMap getResourceEnumMap() {
        return resourceEnumMap;
    }

    /**
     * Returns the the player who's affected
     *
     * @return the the player who's affected
     */
    public PlayerDTO getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        ResourceCardCountMessage that = (ResourceCardCountMessage) obj;
        return Objects.equals(player, that.player) && Objects.equals(resourceEnumMap, that.resourceEnumMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), player, resourceEnumMap);
    }
}