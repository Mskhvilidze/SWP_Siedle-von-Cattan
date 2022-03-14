package de.uol.swp.common.lobby.request;

/**
 * Request sent to the server when the lobby owner wants to add a bot to the lobby
 */
public class RemoveBotFromLobbyRequest extends AbstractLobbyRequest {
    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     */
    public RemoveBotFromLobbyRequest(String lobbyName) {
        super(lobbyName);
    }
}
