package de.uol.swp.common.lobby.message;

import de.uol.swp.common.lobby.dto.LobbyOption;

import java.io.Serializable;
import java.util.Objects;

/**
 * Message sent to a lobby when a user successfully edited a lobby option
 *
 * @param <T> the type of the lobby option value
 */
public class LobbyUpdatedMessage<T extends Serializable> extends AbstractLobbyMessage implements LobbyNotificationMessage {

    private final LobbyOption<T> option;
    private final T newValue;

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby
     * @param option    the {@code LobbyOption} that was changed
     * @param newValue  the value the option was changed to
     */
    public LobbyUpdatedMessage(String lobbyName, LobbyOption<T> option, T newValue) {
        super(lobbyName);
        this.option = option;
        this.newValue = newValue;
    }

    /**
     * Getter for the {@code LobbyOption} that was changed
     *
     * @return the {@code LobbyOption} that was changed
     */
    public LobbyOption<T> getOption() {
        return option;
    }

    /**
     * Getter for the new option value
     *
     * @return the new option value
     */
    public T getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        LobbyUpdatedMessage<?> message = (LobbyUpdatedMessage<?>) object;
        return Objects.equals(option, message.option) && Objects.equals(newValue, message.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), option, newValue);
    }
}
