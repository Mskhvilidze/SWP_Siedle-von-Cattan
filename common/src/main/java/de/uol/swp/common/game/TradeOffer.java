package de.uol.swp.common.game;

import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceType;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a trade between 2 or more players. The name of the offering player will always be validated and set by the server
 */
public class TradeOffer implements Serializable {

    private final ResourceEnumMap offer;
    private final ResourceEnumMap want;
    //Player who sent the offer
    private String offerName;
    //Player who received the offer. Null if all players should receive
    private String receiverName;

    /**
     * Constructs a trade offer with empty resource maps and no players
     * Must have at least one resource, otherwise a TradeException will be send
     */
    public TradeOffer() {
        offer = new ResourceEnumMap();
        want = new ResourceEnumMap();
        putOffer(0, 0, 0, 0, 0);
        putWant(1, 0, 0, 0, 0);
    }

    /**
     * Constructs a trade offer with the given resource maps and no players
     *
     * @param offer the resource map that stores the resources that the offering player offers
     * @param want  the resource map that stores the resources that the offering player wants
     */
    public TradeOffer(ResourceEnumMap offer, ResourceEnumMap want) {
        this.offer = offer;
        this.want = want;
    }

    private void putOffer(int lumber, int wool, int grain, int ore, int brick) {
        offer.put(ResourceType.LUMBER, lumber);
        offer.put(ResourceType.WOOL, wool);
        offer.put(ResourceType.GRAIN, grain);
        offer.put(ResourceType.ORE, ore);
        offer.put(ResourceType.BRICK, brick);
    }

    private void putWant(int lumber, int wool, int grain, int ore, int brick) {
        want.put(ResourceType.LUMBER, lumber);
        want.put(ResourceType.WOOL, wool);
        want.put(ResourceType.GRAIN, grain);
        want.put(ResourceType.ORE, ore);
        want.put(ResourceType.BRICK, brick);
    }

    /**
     * Sets the offer amount for a specific resource
     *
     * @param resource the resource that should be set
     * @param amount   the amount that the resource is set to
     */
    public void setOffer(ResourceType resource, int amount) {
        offer.put(resource, amount);
    }

    /**
     * Sets the receiving amount for a specific resource
     *
     * @param resource the resource that should be set
     * @param amount   the amount that the resource is set to
     */
    public void setWant(ResourceType resource, int amount) {
        want.put(resource, amount);
    }

    /**
     * Returns the {@code ResourceEnumMap} containing the resources that the offering player offers
     *
     * @return the {@code ResourceEnumMap} containing the resources that the offering player offers
     */
    public ResourceEnumMap getOffer() {
        return offer;
    }

    /**
     * Returns the {@code ResourceEnumMap} containing the resources that the offering player wants
     *
     * @return the {@code ResourceEnumMap} containing the resources that the offering player wants
     */
    public ResourceEnumMap getWant() {
        return want;
    }

    /**
     * Returns the name of the player who sent the trade offer
     * <p>
     * <b>The returned player should always be validated on server side</b>
     *
     * @return the name of the player who sent the trade offer
     */
    public String getOfferingPlayer() {
        return offerName;
    }

    /**
     * Sets the name of the player who sent the trade offer
     *
     * @param offerName the name of the player who sent the trade offer
     */
    public void setOfferingPlayer(String offerName) {
        this.offerName = offerName;
    }

    /**
     * Returns the name of the player who receives the trade offer
     * <p>
     * <b>The returned player should always be validated on server side</b>
     *
     * @return the name of the player who receives the trade offer
     */
    @Nullable
    public String getReceivingPlayer() {
        return receiverName;
    }

    /**
     * Sets the name of the player who receives the trade offer
     *
     * @param receiverName the name of the player who receives the trade offer
     */
    public void setReceivingPlayer(String receiverName) {
        this.receiverName = receiverName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offerName, receiverName, offer, want);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TradeOffer that = (TradeOffer) obj;
        return Objects.equals(offerName, that.offerName) && Objects.equals(receiverName,
                that.receiverName) && Objects.equals(offer, that.offer) && Objects.equals(want, that.want);
    }
}
