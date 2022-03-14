package de.uol.swp.common.game.debug;

import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.message.AbstractRequestMessage;

import java.util.Objects;

/**
 * DEBUG message used to set the resources of a player
 */
public class SetResourcesRequest extends AbstractRequestMessage {
    private final String lobbyName;
    private final ResourceEnumMap resourceMap;
    private final String playerName;

    /**
     * Constructor
     *
     * @param lobbyName   the game session name
     * @param resourceMap the resources
     * @param playerName  the player who will get the resources
     */
    public SetResourcesRequest(String lobbyName, ResourceEnumMap resourceMap, String playerName) {
        this.lobbyName = lobbyName;
        this.resourceMap = resourceMap;
        this.playerName = playerName;
    }

    /**
     * Returns the game session name
     *
     * @return the game session name
     */
    public String getLobbyName() {
        return lobbyName;
    }

    /**
     * Returns the resources
     *
     * @return the resources
     */
    public ResourceEnumMap getResourceMap() {
        return resourceMap;
    }

    /**
     * Returns the player who will get the resources
     *
     * @return the player who will get the resources
     */
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        SetResourcesRequest that = (SetResourcesRequest) obj;
        return Objects.equals(lobbyName, that.lobbyName) && Objects.equals(resourceMap, that.resourceMap) && Objects.equals(
                playerName, that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName, resourceMap, playerName);
    }
}
