package de.uol.swp.common.game.request;

import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.ResourceEnumMap;

import java.util.Objects;

/**
 * Request from a player who wants to use a development card.
 *
 * @since 2021-06-13
 */
public class UseCardRequest extends AbstractGameRequest {
    private final DevCard devCard;
    private final ResourceEnumMap resources;

    /**
     * Constructor
     *
     * @param gameSessionName the gameSessionName
     * @param devCard         the using development card
     * @param resources       the resources that the player wants to have
     */
    public UseCardRequest(String gameSessionName, DevCard devCard, ResourceEnumMap resources) {
        super(gameSessionName);
        this.devCard = devCard;
        this.resources = resources;
    }

    /**
     * Getter for the devCard
     *
     * @return devCard
     */
    public DevCard getDevCard() {
        return devCard;
    }

    /**
     * Getter for the resourceEnumMap
     *
     * @return resourceEnumMap
     */
    public ResourceEnumMap getResources() {
        return resources;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        UseCardRequest that = (UseCardRequest) obj;
        return devCard == that.devCard && Objects.equals(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), devCard, resources);
    }
}