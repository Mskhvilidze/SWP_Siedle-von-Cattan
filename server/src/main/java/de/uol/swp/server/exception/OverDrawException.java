package de.uol.swp.server.exception;

/**
 * Throws an exception if resourceCards get decreased while their value is 0
 */
public class OverDrawException extends Exception {

    /**
     * Constructor
     */
    public OverDrawException(String message) {
        super(message);
    }
}
