package de.uol.swp.common.lobby.request;

/**
 * Request sent to the server when a user wants to leave a lobby
 *
 * @author Marco Grawunder
 * @see AbstractLobbyRequest
 * @see de.uol.swp.common.user.User
 * @since 2019-10-08
 */
public class LeaveLobbyRequest extends AbstractLobbyRequest {

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     * @since 2019-10-08
     */
    public LeaveLobbyRequest(String lobbyName) {
        super(lobbyName);
    }

}
