package de.uol.swp.common.lobby.message;

/**
 * Message sent to a user who has been kicked from a lobby
 */
public class KickedFromLobbyMessage extends AbstractLobbyMessage {

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby the user was kicked from
     */
    public KickedFromLobbyMessage(String lobbyName) {
        super(lobbyName);
    }
}
