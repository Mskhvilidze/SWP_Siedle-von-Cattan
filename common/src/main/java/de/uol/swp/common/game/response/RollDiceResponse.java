package de.uol.swp.common.game.response;

import java.util.Objects;

/**
 * Response send to the Client for a RolledDiceRequest
 * with the rolled number and the name of the game session
 */
public class RollDiceResponse extends AbstractGameResponse {

    private final int numberRolled;

    /**
     * @param gameSessionName the name of the game session this message is sent to
     * @param numberRolled    the number that was rolled
     */
    public RollDiceResponse(String gameSessionName, int numberRolled) {
        super(gameSessionName);
        this.numberRolled = numberRolled;
    }

    /**
     * Returns the number that was rolled
     *
     * @return the number that was rolled
     */
    public int getNumberRolled() {
        return numberRolled;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RollDiceResponse that = (RollDiceResponse) obj;
        return numberRolled == that.numberRolled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numberRolled);
    }
}
