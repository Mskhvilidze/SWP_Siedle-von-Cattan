package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;

/**
 * Response sent to the client with information of the requested lobby
 */
public class LobbyInformationResponse extends AbstractResponseMessage {

    private final LobbyDTO lobby;

    /**
     * Constructor
     *
     * @param lobby the lobby
     */
    public LobbyInformationResponse(LobbyDTO lobby) {
        this.lobby = lobby;
    }

    public LobbyDTO getLobby() {
        return lobby;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LobbyInformationResponse that = (LobbyInformationResponse) o;
        return Objects.equals(lobby, that.lobby);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobby);
    }
}
