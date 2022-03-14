package de.uol.swp.common.lobby.message;

import de.uol.swp.common.lobby.dto.LobbyDTO;

import java.util.Objects;

/**
 * Message sent to all users when a public lobby has been created
 */
public class LobbyCreatedMessage extends AbstractLobbyMessage {

    private final LobbyDTO lobby;

    /**
     * Constructor
     *
     * @param name  the name of the created lobby
     * @param lobby a dto that mirrors the lobby that was created
     */
    public LobbyCreatedMessage(String name, LobbyDTO lobby) {
        super(name);
        this.lobby = lobby;
    }

    /**
     * Returns a dto that mirrors the lobby that was created
     *
     * @return a dto that mirrors the lobby that was created
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
        LobbyCreatedMessage that = (LobbyCreatedMessage) object;
        return Objects.equals(lobby, that.lobby);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobby);
    }
}
