package de.uol.swp.common.game.response;

import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * Response to the player if they could not leave the gameSession
 */
public class CanNotLeaveGameResponse extends AbstractResponseMessage {

    private final String gameSessionName;
    private final String reason;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the gameSession the user tried to leave
     * @param reason the reason as String for why they could not leave
     */
    public CanNotLeaveGameResponse(String gameSessionName, String reason) {
        this.gameSessionName = gameSessionName;
        this.reason = reason;
    }

    /**
     * getter for gameSession
     *
     * @return the name of the gameSession the user tried to leave
     */
    public String getGameSessionName(){
        return gameSessionName;
    }

    /**
     * getter for reason
     *
     * @return the reason as String for why the user could not leave
     */
    public String getReason(){
        return reason;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        CanNotLeaveGameResponse that = (CanNotLeaveGameResponse) object;
        return Objects.equals(gameSessionName, that.gameSessionName) &&
                Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameSessionName, reason);
    }
}
