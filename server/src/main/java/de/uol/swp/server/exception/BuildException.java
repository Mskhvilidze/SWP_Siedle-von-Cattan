package de.uol.swp.server.exception;

/**
 * Exception Class for building requests
 */
public class BuildException extends GameStateException {
    // TODO sub-classes for the different reasons the build failed

    /**
     * Constructor
     *
     * @param message reason the build failed
     */
    public BuildException(String message) {
        super(message);
    }
}
