package de.uol.swp.common.game.message;

import java.util.Objects;

/**
 * Message sent to all users in a game session when a player rolled the dices
 */
public class DiceResultMessage extends AbstractGameMessage {
    private final String playerName;
    private final int diceResult;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session this trade has happened in
     */
    public DiceResultMessage(String gameSessionName, String playerName, int diceResult) {
        super(gameSessionName);
        this.playerName = playerName;
        this.diceResult = diceResult;
    }

    /**
     * Getter for the player name
     *
     * @return the name of the player who rolled the dice
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Getter for the dice result
     *
     * @return value of the dice roll
     */
    public int getDiceResult() {
        return diceResult;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        DiceResultMessage that = (DiceResultMessage) obj;
        return diceResult == that.diceResult && Objects.equals(playerName, that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerName, diceResult);
    }
}
