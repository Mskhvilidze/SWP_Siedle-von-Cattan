package de.uol.swp.server.exception;

import de.uol.swp.server.game.session.GameSession;

/**
 * A {@code CanNotRejoinException} is thrown by a {@link GameSession} if a can not rejoin a lobby
 */
public class CanNotRejoinException extends Exception{

    private final String reason;

    /**
     * Constructor
     *
     * @param reason the reason as string why the user can not rejoin
     */
    public CanNotRejoinException(String reason){
        this.reason = reason;
    }

    /**
     * getter for the reason
     *
     * @return the reason as string why the user can not rejoin
     */
    public String getReason(){
        return reason;
    }
}
