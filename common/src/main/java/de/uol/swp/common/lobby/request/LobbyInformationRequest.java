package de.uol.swp.common.lobby.request;

/**
 * Request sent to the server when a user wants to get information to a lobby
 *
 * @see AbstractLobbyRequest
 */
public class LobbyInformationRequest extends AbstractLobbyRequest {

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     */
    public LobbyInformationRequest(String lobbyName) {
        super(lobbyName);
    }
}
