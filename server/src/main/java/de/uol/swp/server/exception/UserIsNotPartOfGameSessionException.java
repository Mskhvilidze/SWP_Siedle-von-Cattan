package de.uol.swp.server.exception;

import de.uol.swp.server.game.session.GameSession;

/**
 * A {@code InvalidGameStateException} is thrown by a {@link GameSession} if a user leaves the GameSession who is no part of this GameSession
 */
public class UserIsNotPartOfGameSessionException extends Exception{
}
