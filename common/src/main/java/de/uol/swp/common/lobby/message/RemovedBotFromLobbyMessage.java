package de.uol.swp.common.lobby.message;

/**
 * Message sent to all users in a lobby session when a bot has been removed from the lobby
 */
public class RemovedBotFromLobbyMessage extends AbstractLobbyMessage {
    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby this message is sent to
     * @since 2019-10-08
     */
    public RemovedBotFromLobbyMessage(String lobbyName) {
        super(lobbyName);
    }
}
