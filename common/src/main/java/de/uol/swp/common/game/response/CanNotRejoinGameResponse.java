package de.uol.swp.common.game.response;

import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * Response to a player if they could not rejoin the lobby
 */

public class CanNotRejoinGameResponse extends AbstractResponseMessage {

    private final String lobbyName;
    private final String reason;

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby from which the user tried to rejoin the lobby
     * @param reason the reason as String for why they could not rejoin
     */
    public CanNotRejoinGameResponse(String lobbyName, String reason) {
        this.lobbyName = lobbyName;
        this.reason = reason;
    }

    /**
     * getter for lobbyName
     *
     * @return the name of the lobby from which the user tried to rejoin the lobby
     */
    public String getLobbyName(){
        return lobbyName;
    }

    /**
     * getter for reason
     *
     * @return the reason as String for why they could rejoin
     */
    public String getReason(){
        return reason;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        CanNotRejoinGameResponse that = (CanNotRejoinGameResponse) object;
        return Objects.equals(lobbyName, that.lobbyName) &&
                Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName, reason);
    }
}
