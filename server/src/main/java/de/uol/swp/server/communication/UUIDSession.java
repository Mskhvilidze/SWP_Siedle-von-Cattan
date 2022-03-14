package de.uol.swp.server.communication;

import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.request.LoginRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Class used to store connected clients and Users in an identifiable way
 *
 * @author Marco Grawunder
 * @author Marco Grawunder
 * @see de.uol.swp.server.usermanagement.AuthenticationService#onLoginRequest(LoginRequest)
 * @see de.uol.swp.common.user.Session
 * @since 2017-03-17
 */

@SuppressWarnings({"PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal"})
public class UUIDSession implements Session {

    private final String sessionId;
    private final List<String> lobbies;
    private User user;

    /**
     * private Constructor
     *
     * @param user the user connected to the session
     * @since 2017-03-17
     */
    private UUIDSession(User user) {
        synchronized (UUIDSession.class) {
            this.sessionId = String.valueOf(UUID.randomUUID());
            this.user = user;
            this.lobbies = new ArrayList<>();
        }
    }

    /**
     * Builder for the UUIDSession
     * <p>
     * Builder exposed to every class in the server, used since the constructor is private
     *
     * @param user the user connected to the session
     * @return a new UUIDSession object for the user
     * @since 2019-08-07
     */
    public static Session create(User user) {
        return new UUIDSession(user);
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public User updateUser(User updatedUser) {
        this.user = updatedUser;
        return this.user;
    }

    @Override
    public List<String> getLobbies() {
        return lobbies;
    }

    @Override
    public void addLobby(String lobbyname) {
        lobbies.add(lobbyname);
    }

    @Override
    public void removeLobby(String lobbyname) {
        lobbies.remove(lobbyname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        UUIDSession session = (UUIDSession) object;
        return Objects.equals(sessionId, session.sessionId);
    }

    @Override
    public String toString() {
        return "SessionId: " + sessionId;
    }

}
