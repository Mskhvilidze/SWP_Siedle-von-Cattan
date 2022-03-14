package de.uol.swp.common.lobby.message;

/**
 * Message sent to all users in a lobby session when a bot has been added to the lobby
 */
public class AddedBotToLobbyMessage extends AbstractLobbyMessage {

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby this message is sent to
     */
    public AddedBotToLobbyMessage(String lobbyName) {
        super(lobbyName);
    }
}
