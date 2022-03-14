package de.uol.swp.server.game.session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.lobby.GameLobby;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles most interactions of GameSessions
 */
@Singleton
public class GameSessionManagement {
    private static final Logger LOG = LogManager.getLogger(GameSessionManagement.class);
    private final Map<String, GameSession> sessions = new HashMap<>();
    private final GameSessionFactory gameSessionFactory;

    /**
     * Constructor
     *
     * @param gameSessionFactory the instance of {@code GameSessionFactory} injected by guice
     */
    @Inject
    public GameSessionManagement(GameSessionFactory gameSessionFactory) {
        this.gameSessionFactory = gameSessionFactory;
    }

    /**
     * Creates a new session and adds it to the list
     *
     * @param lobby the lobby that is started
     * @throws IllegalArgumentException name already taken
     * @throws IllegalArgumentException name already taken
     * @implNote the primary key of the session is the name therefore the name has
     * to be unique
     * @see de.uol.swp.common.user.User
     */
    public GameSession createGameSession(GameLobby lobby) {
        if (sessions.containsKey(lobby.getName())) {
            throw new IllegalArgumentException("Session name " + lobby.getName() + " already started!");
        }
        GameSession session = gameSessionFactory.create(lobby);
        LOG.debug("New GameSession created, Session name {}", lobby.getName());
        sessions.put(lobby.getName(), session);
        return session;
    }

    /**
     * Searches for the session with the requested name
     *
     * @param name String containing the name of the session to search for
     * @return either empty Optional or Optional containing the session
     * @see Optional
     */
    public Optional<GameSession> getGameSession(String name) {
        GameSession session = null;
        for (Map.Entry<String, GameSession> entry : sessions.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                session = entry.getValue();
            }
        }
        if (session != null) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

    /**
     * Deletes session with requested name
     *
     * @param name String containing the name of the session to delete
     * @throws IllegalArgumentException there exists no session with the requested name
     */
    public void dropGameSession(String name) {
        if (!sessions.containsKey(name)) {
            throw new IllegalArgumentException("Session name " + name + " not found!");
        }
        sessions.remove(name);
    }
}
