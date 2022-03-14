package de.uol.swp.common.game.message;

import java.util.Objects;

/**
 * Message sent to all players in a game session after the robber stole a resource
 */
public class RobberDoneMessage extends AbstractGameMessage {

    private final String robber;
    private final String victim;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the associated game
     * @param robber          the name of the player who robbed another
     * @param victim          the name of the victim
     */
    public RobberDoneMessage(String gameSessionName, String robber, String victim) {
        super(gameSessionName);
        this.robber = robber;
        this.victim = victim;
    }

    /**
     * getter for the name of the robber
     *
     * @return name of the robber
     */
    public String getRobber(){
        return robber;
    }

    /**
     * getter for the name of the victim
     *
     * @return name of the victim
     */
    public String getVictim() {
        return victim;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RobberDoneMessage that = (RobberDoneMessage) obj;
        return Objects.equals(robber, that.robber) && Objects.equals(victim, that.victim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), robber, victim);
    }
}
