package de.uol.swp.client.profile.event;

/**
 * Event to display the password confirmation window
 * <p>
 * To display the registration window with this event, publish an instance of it
 * on the eventBus to which the SceneManager has subscribed.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class ConfirmPasswordErrorEvent {
    private final String message;

    /**
     * Constructor
     *
     * @param message Message containing the cause of the Error
     */
    public ConfirmPasswordErrorEvent(String message) {
        this.message = message;
    }

    /**
     * Get the error message
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
}
