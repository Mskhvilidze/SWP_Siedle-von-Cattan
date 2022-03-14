package de.uol.swp.server.exception;

/**
 * Base class for all exceptions that get thrown due to a GameState error
 */
public abstract class GameStateException extends Exception {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    protected GameStateException() {

    }

    /**
     * Constructs a new exception with the specified detail message
     *
     * @param message the detail message
     */
    protected GameStateException(String message) {
        super(message);
    }
}