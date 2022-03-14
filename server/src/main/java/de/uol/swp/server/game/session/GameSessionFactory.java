package de.uol.swp.server.game.session;

import de.uol.swp.common.lobby.GameLobby;

/**
 * Factory used to inject the {@link GameSessionService} into a GameSession via guice
 */
public interface GameSessionFactory {

    /**
     * Creates an instance of the class GameSession
     *
     * @param lobby the {@code GameLobby} instance that starts a new game session
     * @return a new GameSession object
     */
    GameSession create(GameLobby lobby);
}
