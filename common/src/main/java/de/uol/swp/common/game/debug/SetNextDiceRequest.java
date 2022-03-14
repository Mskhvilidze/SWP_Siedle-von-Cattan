package de.uol.swp.common.game.debug;

import java.util.Objects;

/**
 * DEBUG message used to set the next dice roll
 */
public class SetNextDiceRequest extends AbstractDebugRequest {
    private final int diceValue;

    /**
     * Constructor
     *
     * @param sessionName the name of the game session this message is sent to
     * @param diceValue   DEBUG: -1: random dice value otherwise this
     */
    public SetNextDiceRequest(String sessionName, int diceValue) {
        super(sessionName);
        this.diceValue = diceValue;
    }

    /**
     * Returns the next dice value
     *
     * @return the next dice value
     */
    public int getDiceValue() {
        return diceValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        SetNextDiceRequest that = (SetNextDiceRequest) obj;
        return diceValue == that.diceValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), diceValue);
    }
}
