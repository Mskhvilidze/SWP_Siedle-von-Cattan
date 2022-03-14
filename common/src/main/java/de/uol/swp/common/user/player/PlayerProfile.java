package de.uol.swp.common.user.player;

import java.io.Serializable;
import java.util.Objects;

/**
 * PlayerProfile shows the information about players
 */
public class PlayerProfile implements Serializable {

    private final String playerName;
    private final String email;
    private final String won;
    private final String loss;
    private final String ratio;

    /**
     * Constructor
     *
     * @param playerName the name of the player
     * @param email      the E-Mail of the player
     * @param won        a String representing the number of games the player has won
     * @param loss       a String representing the number of games the player has lost
     */
    public PlayerProfile(String playerName, String email, String won, String loss) {
        this.playerName = playerName;
        this.email = email;
        this.won = won;
        this.loss = loss;
        double playerWon = Double.parseDouble(won);
        double playerLoss = Double.parseDouble(loss);
        double result = playerLoss + playerWon;
        ratio = (int) Math.round((playerWon / result) * 100) + "%";
    }

    /**
     * Returns the name of the player
     *
     * @return the name of the player
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns the E-Mail of the player
     *
     * @return the E-Mail of the player
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns a string representing the number of games the player has won
     *
     * @return a string representing the number of games the player has won
     */
    public String getWon() {
        return won;
    }

    /**
     * Returns a string representing the number of games the player has lost
     *
     * @return a string representing the number of games the player has lost
     */
    public String getLoss() {
        return loss;
    }

    /**
     * Getter for Ratio
     * <p>
     * Returns a string that represents the ratio between gains and losses
     *
     * @return ratio between gains and losses
     */
    public String getRatio() {
        return ratio;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayerProfile that = (PlayerProfile) obj;
        return Objects.equals(playerName, that.playerName) && Objects.equals(email, that.email) && Objects.equals(won,
                that.won) && Objects.equals(loss, that.loss) && Objects.equals(ratio, that.ratio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, email, won, loss, ratio);
    }
}
