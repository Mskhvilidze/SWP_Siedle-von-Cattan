package de.uol.swp.common.user;

import de.uol.swp.common.game.dto.UserDTO;

import java.io.Serializable;

/**
 * Interface for different kinds of user objects.
 * <p>
 * This interface is for unifying different kinds of user objects throughout the
 * project. With this being the base project it is currently only used for the UserDTO
 * objects.
 *
 * @author Marco Grawunder
 * @see UserDTO
 * @since 2019-08-05
 */
@SuppressWarnings({"PMD.ShortClassName"})
public interface User extends Serializable, Comparable<User> {

    /**
     * Getter for the username variable
     *
     * @return username of the user as String
     * @since 2019-08-05
     */
    String getUsername();

    /**
     * Getter for the password variable
     * <p>
     * The plain text password should only be used to store user input.
     *
     * @return the plain text password of the user if it has been set, otherwise {@code null}
     * @since 2019-08-05
     */
    String getPlainPassword();

    /**
     * Getter for the email variable
     *
     * @return email address of the user as String
     * @since 2019-08-05
     */
    String getEMail();

    /**
     * Creates a duplicate of this object leaving its password empty
     *
     * @return a Copy of this with empty hash and salt field
     */
    User getWithoutPassword();

    /**
     * Getter for the hash variable
     *
     * @return the hash of the user's password as byte array
     */
    byte[] getPasswordHash();

    /**
     * Getter for the salt variable
     *
     * @return the salt used to calculate the user's password hash as byte array
     */
    byte[] getPasswordSalt();

    /**
     * Getter for the id variable
     *
     * @return id of the user as a String
     */
    String getUserId();
}
