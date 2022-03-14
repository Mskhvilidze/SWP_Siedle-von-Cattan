package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.GameDTO;


import java.util.Objects;

/**
 * Message sent from Server to Clients, informing them that the gameSession has been updated, because a user left or rejoined the game
 */
public class UpdateGameSessionMessage extends AbstractGameMessage{

    private final GameDTO game;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the gamesession
     */
    public UpdateGameSessionMessage(String gameSessionName, GameDTO game) {
        super(gameSessionName);
        this.game = game;
    }

    /**
     * getter for the game
     *
     * @return the GameDTO of the gameSession
     */
    public GameDTO getGame(){ return game;}

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        UpdateGameSessionMessage that = (UpdateGameSessionMessage) object;
        return Objects.equals(game, that.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), game);
    }
}
