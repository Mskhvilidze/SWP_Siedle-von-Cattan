package de.uol.swp.server.exception;

/**
 * Exception ocuring in RobberPlacingState when a player not priviliged to place the robber sends a RobberPlacingRequest
 */
public class RobberPlacingException extends GameStateException {

    /**
     * Constructor
     *
     * @param message message
     */
    public RobberPlacingException(String message) {
        super(message);
    }
}
