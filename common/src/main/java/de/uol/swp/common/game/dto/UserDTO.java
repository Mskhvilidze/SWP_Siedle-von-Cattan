package de.uol.swp.common.game.dto;

import de.uol.swp.common.user.User;

import java.util.Objects;

/**
 * Objects of this class are used to transfer user data between the server and the
 * clients.
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.user.User
 * @see de.uol.swp.common.user.request.RegisterUserRequest
 * @see de.uol.swp.common.user.response.AllOnlineUsersResponse
 * @since 2019-08-13
 */
public class UserDTO implements User {

    private final String username;
    private final String eMail;
    private final byte[] hash;
    private final byte[] salt;
    private final String userId;
    private String plainPassword;

    /**
     * Constructor
     *
     * @param username username of the user
     * @param hash     the hash of the password
     * @param salt     the salt used to calculate the password hash
     * @param eMail    email address the user is registered to
     * @since 2019-08-13
     */
    public UserDTO(String userId, String username, byte[] hash, byte[] salt, String eMail) {
        if (userId == null) {
            throw new AssertionError("id must not be null");
        }
        if (username == null) {
            throw new AssertionError("username must not be null");
        }
        if (hash == null || salt == null) {
            throw new AssertionError("password must not be null");
        }
        this.userId = userId;
        this.username = username;
        this.hash = hash.clone();
        this.salt = salt.clone();
        this.eMail = eMail;
    }

    /**
     * Creates a UserDTO object with an userId of "-1" and with a plain text password
     * <p>
     * This constructor should only be used to store client inputs.
     *
     * @param username      username of the user
     * @param plainPassword plain text password of the user
     * @param eMail         email address the user is registered to
     */
    public UserDTO(String username, String plainPassword, String eMail) {
        if (username == null) {
            throw new AssertionError("username must not be null");
        }
        if (plainPassword == null) {
            throw new AssertionError("password must not be null");
        }
        this.userId = "-1";
        this.hash = new byte[0];
        this.salt = new byte[0];
        this.username = username;
        this.plainPassword = plainPassword;
        this.eMail = eMail;
    }

    /**
     * Copy constructor
     *
     * @param user the User object to copy the values of
     * @return a UserDTO copy of User object
     * @since 2019-08-13
     */
    public static UserDTO create(User user) {
        return new UserDTO(user.getUserId(), user.getUsername(), user.getPasswordHash(), user.getPasswordSalt(), user.getEMail());
    }

    /**
     * Copy constructor for UserDTO objects with user inputs
     *
     * @param user the User object to copy the values of
     * @return a UserDTO copy of User object
     * @since 2019-08-13
     */
    public static UserDTO createWithPlainPassword(User user) {
        return new UserDTO(user.getUsername(), user.getPlainPassword(), user.getEMail());
    }

    /**
     * Copy constructor leaving password variable empty
     * <p>
     * This constructor is used for the user list, because it would be a major security
     * flaw to send all user data including passwords to everyone connected.
     *
     * @param user the User object to copy the values of
     * @return a UserDTO copy of User object having the password variable left empty
     * @since 2019-08-13
     */
    public static UserDTO createWithoutPassword(User user) {
        return new UserDTO(user.getUserId(), user.getUsername(), new byte[0], new byte[0], user.getEMail());
    }

    /**
     * Creates a new UserDTO object with empty byte arrays for the hash and salt fields
     * <p>
     * This constructor is used for the user list, because it would be a major security
     * flaw to send all user data including passwords to everyone connected.
     *
     * @param userId   the userId of the user
     * @param username the name of the user
     * @param email    the email address of the user
     * @return a UserDTO with empty byte arrays for the hash and salt fields
     */
    public static UserDTO createWithoutPassword(String userId, String username, String email) {
        return new UserDTO(userId, username, new byte[0], new byte[0], email);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPlainPassword() {
        return plainPassword;
    }

    @Override
    public String getEMail() {
        return eMail;
    }

    @Override
    public User getWithoutPassword() {
        return new UserDTO(userId, username, new byte[0], new byte[0], eMail);
    }

    @Override
    public byte[] getPasswordHash() {
        return hash.clone();
    }

    @Override
    public byte[] getPasswordSalt() {
        return salt.clone();
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public int compareTo(User object) {
        return username.compareTo(object.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserDTO userDTO = (UserDTO) obj;
        return username.equals(userDTO.username);
    }
}
