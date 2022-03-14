package de.uol.swp.common.lobby.request;

/**
 * Request to start a game Session
 */
public class StartGameSessionRequest extends AbstractLobbyRequest {


    /**
     * Constructor
     *
     * @param lobbyName    the name of the lobby that wants to start a game session
     * @param debugEnabled if the debug mode is enabled or not
     */
    public StartGameSessionRequest(String lobbyName) {
        super(lobbyName);
    }
}