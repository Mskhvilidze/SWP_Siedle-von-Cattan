package de.uol.swp.server.game.state;

import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.game.session.GameSession;

/**
 * Interface for all game states. Used to separate state details from the {@code GameSession} class
 */
public interface GameState {

    /**
     * Switches from the active state to the next state.
     * Should only be used when you have to force a state to end.
     * Like for example when the state timer ends
     *
     * @param gameSession the game session that called this method
     * @param context     the state context of the game session. Some states need this to determine the next state
     */
    void next(GameSession gameSession, StateContext context);

    /**
     * Does the action of this state.
     * This includes sending messages, switching states and changing game values
     *
     * @param gameSession the game session that called this method
     * @param context     the state context of the game session. All states need this to determine the request
     * @throws GameStateException if the context contains a RequestMessage that cannot be handled by the state
     */
    void action(GameSession gameSession, StateContext context) throws GameStateException;

    /**
     * Handles the end of a turn for this state
     * This includes sending messages, switching states and changing game values
     *
     * @param gameSession the game session that called this method
     * @param context     the state context of the game session
     * @implNote should call this method with {@code GameState.super.endTurn(gameSession, context);} at the end of method
     */
    default void endTurn(GameSession gameSession, StateContext context) {
        EndState.INSTANCE.endTurn(gameSession, context);
    }

    /**
     * Returns the timer duration of the state in seconds
     *
     * @return the timer duration of the state in seconds
     */
    default int getTimer() {
        return 0;
    }

    default void beginState(GameSession gameSession, StateContext context) {

    }

    /*
    Setup Begin:Beginn des Game setups Vor der Ersten Runde
    Setup Complete:Ende der zweiten Runde
    Dice or Dev Card: Beginn des Zuges. Würfeln oder Dev Card spielen
    Robber Discard: Bei einer 7 Karten abwerfen
    Placing Robber: Bei einer 7 Räuber platzieren
    Play: Nach Würfelwurf oder Räuber wenn Dev bereits gespielt wurde. Auf Building, Trading oder End warten.
    Building: Bauen
    Trading: Handeln
    End: Ende des Zuges -> Dice or Dev Card
     */
}
