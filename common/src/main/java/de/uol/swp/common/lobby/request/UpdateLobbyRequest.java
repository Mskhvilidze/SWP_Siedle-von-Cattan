package de.uol.swp.common.lobby.request;

import de.uol.swp.common.lobby.dto.LobbyOption;

import java.io.Serializable;
import java.util.Objects;

/**
 * Request to the server when a user wants to edit a lobby option
 *
 * @param <T> the type of the lobby option value
 */
public class UpdateLobbyRequest<T extends Serializable> extends AbstractLobbyRequest {

    private final LobbyOption<T> option;
    private final T newValue;

    /**
     * Constructor
     *
     * @param lobbyName the name of the lobby
     * @param option    the {@code LobbyOption} that has to be changed
     * @param newValue  the value the option has to be changed to
     */
    public UpdateLobbyRequest(String lobbyName, LobbyOption<T> option, T newValue) {
        super(lobbyName);
        this.option = option;
        this.newValue = newValue;
    }

    /**
     * Getter for the {@code LobbyOption} that has to be changed
     *
     * @return the {@code LobbyOption} that has to be changed
     */
    public LobbyOption<T> getOption() {
        return option;
    }

    /**
     * Getter for the value the option has to be changed to
     *
     * @return the value the option has to be changed to
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
        UpdateLobbyRequest<?> request = (UpdateLobbyRequest<?>) object;
        return Objects.equals(option,
                request.option) && Objects.equals(newValue, request.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), option, newValue);
    }
}
