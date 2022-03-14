package de.uol.swp.server.exception;

/**
 * Exception used in the SetupPhase
 */
public class SetupException extends GameStateException {

    /**
     * Constructor
     *
     * @param message reason the build failed
     */
    public SetupException(String message) {
        super(message);
    }

}
