package de.uol.swp.common.game.request;

import de.uol.swp.common.game.board.DevCard;

import java.util.Objects;

/**
 * a request from the client, to confirm if they is allowed to use the given DevCard
 */
public class IsUsingCardAllowedRequest extends AbstractGameRequest{

    private final DevCard devCard;

    /**
     * Constructor
     *
     * @param gameSessionName name of the associated gameSession
     * @param devCard the devCard the user wants to use
     */
    public IsUsingCardAllowedRequest(String gameSessionName, DevCard devCard){
        super(gameSessionName);
        this.devCard = devCard;
    }

    /**
     * Getter for the devCard
     *
     * @return the DevCard the user wants to use
     */
    public DevCard getDevCard() {
        return devCard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IsUsingCardAllowedRequest that = (IsUsingCardAllowedRequest) o;
        return devCard == that.devCard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), devCard);
    }
}
