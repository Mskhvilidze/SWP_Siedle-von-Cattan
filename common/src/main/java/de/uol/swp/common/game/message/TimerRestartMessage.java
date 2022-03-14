package de.uol.swp.common.game.message;

import java.util.Objects;

/**
 * Message sent to all players in a game session when the timer restarts
 */
public class TimerRestartMessage extends AbstractGameMessage {
    private final int turnTimer;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param turnTimer       the duration of the timer in seconds
     */
    public TimerRestartMessage(String gameSessionName, int turnTimer) {
        super(gameSessionName);
        this.turnTimer = turnTimer;
    }

    /**
     * Returns the duration of the timer in seconds
     *
     * @return the duration of the timer in seconds
     */
    public int getTurnTimer() {
        return turnTimer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        TimerRestartMessage that = (TimerRestartMessage) obj;
        return turnTimer == that.turnTimer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), turnTimer);
    }
}
