package de.uol.swp.common.lobby.request;

import de.uol.swp.common.game.dto.UserDTO;

import java.util.Objects;

/**
 * Request sent to the server when a lobby owner wants to make someone else the owner
 */
public class UpdateLobbyOwnerRequest extends AbstractLobbyRequest {

    private final UserDTO newOwner;

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby
     * @param newOwner  the new owner of the lobby
     */
    public UpdateLobbyOwnerRequest(String lobbyName, UserDTO newOwner) {
        super(lobbyName);
        this.newOwner = newOwner;
    }

    /**
     * Getter for the new owner of the lobby
     *
     * @return the new owner of the lobby
     */
    public UserDTO getNewOwner() {
        return newOwner;
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
        UpdateLobbyOwnerRequest that = (UpdateLobbyOwnerRequest) object;
        return Objects.equals(newOwner, that.newOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newOwner);
    }
}
