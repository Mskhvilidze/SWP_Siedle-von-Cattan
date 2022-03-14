package de.uol.swp.common.lobby.message;

import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.message.AbstractServerMessage;

import java.util.Objects;
import java.util.Set;

/**
 * Message sent to all users when a lobby is updated
 * <p>
 * This message contains a {@code Set} of all public lobbies currently on the server
 */
public class LobbyListUpdatedMessage extends AbstractServerMessage {

    @SuppressWarnings("java:S1948")
    private final Set<Lobby> lobbies;

    /**
     * Constructor
     *
     * @param lobbies list of the lobbies
     */
    public LobbyListUpdatedMessage(Set<Lobby> lobbies) {
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
        LobbyListUpdatedMessage that = (LobbyListUpdatedMessage) obj;
        return Objects.equals(lobbies, that.lobbies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lobbies);
    }
}
