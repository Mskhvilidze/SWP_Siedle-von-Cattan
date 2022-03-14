package de.uol.swp.server.exception;

/**
 * Thrown when a Player has not selected a dev card when he should and when he selected the wrong amount of dev cards.
 *
 * @since 2021-06-22
 */
public class PlayStateException extends GameStateException {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public PlayStateException() {

    }

    /**
     * Constructs a new exception with the specified detail message
     *
     * @param message the detail message
     */
    public PlayStateException(String message) {
        super(message);
    }
}