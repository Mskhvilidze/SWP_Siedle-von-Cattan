package de.uol.swp.common.game.response;

import de.uol.swp.common.game.board.DevCard;

import java.util.Objects;

/**
 * The response of the server if the player is allowed the given devCard
 */
public class IsUsingCardAllowedResponse extends AbstractGameResponse {

    private final boolean allowed;
    private final String reason;
    private final DevCard devCard;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the associated GameSession
     * @param allowed         the boolean that shows if the player is allowed to use the given devCard
     * @param devCard         the devCard the user wants to use
     * @param reason          the reason why the user can not user the devCard
     */
    public IsUsingCardAllowedResponse(String gameSessionName, boolean allowed, DevCard devCard, String reason){
        super(gameSessionName);
        this.allowed = allowed;
        this.devCard = devCard;
        this.reason = reason;
    }

    /**
     * getter for allowed
     *
     * @return the boolean that shows if the player is allowed to use the given card
     */
    public boolean isAllowed(){
        return allowed;
    }

    /**
     * getter for reason
     *
     * @return the reason as string why the user can not use the dev card
     */
    public String getReason() {
        return reason;
    }

    /**
     * getter for devCard
     *
     * @return the DevCard the user wants to use
     */
    public DevCard getDevCard() {
        return devCard;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        IsUsingCardAllowedResponse that = (IsUsingCardAllowedResponse) obj;
        return allowed == that.allowed && Objects.equals(reason, that.reason) && devCard == that.devCard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), allowed, reason, devCard);
    }
}
