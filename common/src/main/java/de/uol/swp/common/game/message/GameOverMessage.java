package de.uol.swp.common.game.message;


import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.lobby.message.LobbyNotificationMessage;

import java.util.List;
import java.util.Objects;

/**
 * Message signaling the end of a game
 */
public class GameOverMessage extends AbstractGameMessage implements LobbyNotificationMessage {

    private final List<PlayerDTO> standings;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this message is sent to
     * @param standings       the result of the game
     */

    public GameOverMessage(String gameSessionName, List<PlayerDTO> standings) {
        super(gameSessionName);
        this.standings = standings;
    }

    @Override
    public String getLobbyName() {
        return getGameSessionName();
    }

    /**
     * Getter for the standings List.
     *
     * @return standings
     */
    public List<PlayerDTO> getStandings() {
        return standings;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        GameOverMessage that = (GameOverMessage) obj;
        return Objects.equals(standings, that.standings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), standings);
    }
}
