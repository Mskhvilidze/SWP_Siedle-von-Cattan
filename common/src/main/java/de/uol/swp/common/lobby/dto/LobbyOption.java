package de.uol.swp.common.lobby.dto;

import java.io.Serializable;

/**
 * Interface to unify the lobby option enums created in {@link LobbyOptions}
 *
 * @param <T> the type of the lobby option value
 */
public interface LobbyOption<T extends Serializable> extends Serializable {
    /**
     * Getter for the lobby option's default value
     *
     * @return the default value of the lobby option
     */
    T getDefaultValue();

    /**
     * Getter for the minimum value an option must have
     *
     * @return the minimum value an option must have. Can be null for values that arent numbers
     * @implNote Should only be implemented if {@link T} extends {@code Number}
     */
    T getMin();

    /**
     * Getter for the maximum value an option must have
     *
     * @return the maximum value an option must have. Can be null for values that arent numbers
     * @implNote Should only be implemented if {@link T} extends {@code Number}
     */
    T getMax();
}