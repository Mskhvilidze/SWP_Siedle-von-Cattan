package de.uol.swp.server.game.mapobject;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.server.game.Player;

/**
 * A game class which represents the game figure "Robber"
 */
public class Robber {

    private Player lastPlayer;
    private Coord coord;

    /**
     * Sets the coordinates of the bandit
     *
     * @param coord for the new position of the bandit
     */
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    /**
     * Sets the last player who moved the bandit
     *
     * @param player who moved the bandit
     */
    public void setLastPlayer(Player player) {
        this.lastPlayer = player;
    }

    /**
     * Return the coordinates of the bandit
     *
     * @return coord of the bandit
     */
    public Coord getCoord() {
        return coord;
    }

    /**
     * Returns the last player who moved the bandit
     *
     * @return the last player who moved the bandit
     */
    public Player getLastPlayer() {
        return lastPlayer;
    }


}
