package de.uol.swp.common.lobby.message;

/**
 * Message that is sent by the server to signal that a game has ended
 */

public class GameEndedMessage extends AbstractLobbyMessage{

    /**
     * Constructor
     *
     * @param lobbyName name of the lobby
     */
    public GameEndedMessage(String lobbyName){
        super(lobbyName);
    }
}
