package de.uol.swp.common.lobby.message;

import de.uol.swp.common.game.dto.GameDTO;

import java.util.Objects;

/**
 * Message sent to a lobby indicating a Game Session has been started.
 */
public class StartGameSessionMessage extends AbstractLobbyMessage implements LobbyNotificationMessage {

    private final GameDTO gameDTO;
    private final String lobby;

    /**
     * Constructor
     *
     * @param lobbyName Lobby Name
     */
    public StartGameSessionMessage(String lobbyName, GameDTO gameDTO) {
        super(lobbyName);
        this.lobby = lobbyName;
        this.gameDTO = gameDTO;
    }

    /**
     * Returns a dto that contains import information about the game session that was started
     *
     * @return a dto that contains import information about the game session that was started
     */
    public GameDTO getGameDTO() {
        return gameDTO;
    }

    /**
     * Getter for the lobby that the user joined
     *
     * @return the lobby that the user joined
     */
    public String getLobby() {
        return lobby;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        StartGameSessionMessage that = (StartGameSessionMessage) object;
        return Objects.equals(gameDTO, that.gameDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameDTO);
    }
}
