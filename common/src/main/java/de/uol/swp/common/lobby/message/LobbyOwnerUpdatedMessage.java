package de.uol.swp.common.lobby.message;

import de.uol.swp.common.game.dto.UserDTO;

import java.util.Objects;

/**
 * Message sent to a lobby when a user was given owner rights
 */
public class LobbyOwnerUpdatedMessage extends AbstractLobbyMessage {

    private final UserDTO newOwner;

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby
     * @param newOwner  the new owner of the lobby
     */
    public LobbyOwnerUpdatedMessage(String lobbyName, UserDTO newOwner) {
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
        LobbyOwnerUpdatedMessage message = (LobbyOwnerUpdatedMessage) object;
        return Objects.equals(newOwner, message.newOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newOwner);
    }
}
