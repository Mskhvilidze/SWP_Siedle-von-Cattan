package de.uol.swp.common.lobby.message;


import de.uol.swp.common.lobby.dto.LobbyDTO;

import java.util.Objects;

/**
 * Message sent to all users when a lobby has been dropped
 *
 * @see de.uol.swp.common.lobby.message.AbstractLobbyMessage
 */
public class LobbyDroppedMessage extends AbstractLobbyMessage {

    private final LobbyDTO lobby;

    /**
     * Response sent to the client when the lobby is deleted
     *
     * @param name the name of the lobby
     * @see de.uol.swp.common.lobby.message.AbstractLobbyMessage
     */
    public LobbyDroppedMessage(String name, LobbyDTO lobby) {
        super(name);
        this.lobby = lobby;
    }

    /**
     * Returns a dto that mirrors the lobby that was dropped
     *
     * @return a dto that mirrors the lobby that was dropped
     */
    public LobbyDTO getLobby() {
        return lobby;
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
        LobbyDroppedMessage that = (LobbyDroppedMessage) object;
        return Objects.equals(lobby, that.lobby);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobby);
    }
}
