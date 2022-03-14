package de.uol.swp.common.lobby.request;

import de.uol.swp.common.game.dto.UserDTO;

import java.util.Objects;

/**
 * Request sent to the server when a user wants to create a new lobby
 *
 * @author Marco Grawunder
 * @see AbstractLobbyRequest
 * @see de.uol.swp.common.user.User
 * @since 2019-10-08
 */
public class CreateLobbyRequest extends AbstractLobbyRequest {

    private final boolean privateLobby;
    private final UserDTO owner;

    /**
     * Constructor
     *
     * @param name         the name that the new lobby should have
     * @param owner        the user trying to create the lobby (user is also the owner of the lobby)
     * @param privateLobby boolean if the lobby is private or not
     * @since 2019-10-08
     */
    public CreateLobbyRequest(String name, UserDTO owner, boolean privateLobby) {
        super(name);
        this.owner = owner;
        this.privateLobby = privateLobby;
    }

    public UserDTO getOwner() {
        return owner;
    }

    /**
     * Returns whether the lobby should be private
     *
     * @return {@code true} if the lobby should be private, otherwise {@code false}
     */
    public boolean isPrivateLobby() {
        return privateLobby;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        CreateLobbyRequest that = (CreateLobbyRequest) obj;
        return privateLobby == that.privateLobby;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), privateLobby);
    }
}
