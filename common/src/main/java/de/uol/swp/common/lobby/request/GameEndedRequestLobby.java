package de.uol.swp.common.lobby.request;

/**
 * Request used to signal that the lobby should be force closed
 */
public class GameEndedRequestLobby extends AbstractLobbyRequest {

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     */
    public GameEndedRequestLobby(String lobbyName) {
        super(lobbyName);
    }
}
