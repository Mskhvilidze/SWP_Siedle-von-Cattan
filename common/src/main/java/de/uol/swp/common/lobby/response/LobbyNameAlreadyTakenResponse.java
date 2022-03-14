package de.uol.swp.common.lobby.response;

import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * Response sent to the client if the lobbyname is already taken
 */
public class LobbyNameAlreadyTakenResponse extends AbstractResponseMessage {

    private final String lobbyName;

    /**
     * Constructor
     *
     * @param lobbyName the lobbyname that is already taken
     */
    public LobbyNameAlreadyTakenResponse(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    /**
     * Returns the lobbyname that is already taken
     *
     * @return the lobbyname that is already taken
     */
    public String getLobbyName() {
        return lobbyName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        LobbyNameAlreadyTakenResponse that = (LobbyNameAlreadyTakenResponse) object;
        return Objects.equals(lobbyName, that.lobbyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbyName);
    }
}
