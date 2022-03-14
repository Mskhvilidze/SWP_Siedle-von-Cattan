package de.uol.swp.server.exception;

/**
 * Base class for all trade exceptions that get thrown due to a TradeState error
 */
public class TradeException extends GameStateException {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public TradeException() {

    }

    /**
     * Constructs a new exception with the specified detail message
     *
     * @param message the detail message
     */
    public TradeException(String message) {
        super(message);
    }
}
