package de.uol.swp.server.exception;

import de.uol.swp.server.game.state.GameState;
import de.uol.swp.server.game.state.StateContext;

/**
 * A {@code InvalidGameStateException} is thrown by a {@link GameState} if its {@link StateContext} contains
 * an invalid {@code RequestMessage}
 */
public class InvalidGameStateException extends GameStateException {
}