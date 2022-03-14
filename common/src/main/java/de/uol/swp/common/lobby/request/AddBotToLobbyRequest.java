package de.uol.swp.common.lobby.request;

/**
 * Request sent to the server when a lobby owner wants to add a bot to the lobby
 */
public class AddBotToLobbyRequest extends AbstractLobbyRequest {
    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     */
    public AddBotToLobbyRequest(String lobbyName) {
        super(lobbyName);
    }
}
