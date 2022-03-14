package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.message.AbstractResponseMessage;

import java.util.Objects;
import java.util.Set;

/**
 * Response sent to the client after they log in
 *
 * @see de.uol.swp.common.lobby.request.RetrieveLobbyListRequest
 */
public class RetrieveLobbyListResponse extends AbstractResponseMessage {

    private final Set<Lobby> lobbies;

    /**
     * Constructor
     *
     * @param lobbies list of the lobbies
     */
    public RetrieveLobbyListResponse(Set<Lobby> lobbies) {
        this.lobbies = lobbies;
    }

    public Set<Lobby> getLobbies() {
        return lobbies;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RetrieveLobbyListResponse that = (RetrieveLobbyListResponse) obj;
        return Objects.equals(lobbies, that.lobbies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbies);
    }
}
