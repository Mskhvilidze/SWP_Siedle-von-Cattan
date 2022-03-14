package de.uol.swp.server.game.state;

import de.uol.swp.server.game.session.GameSession;

/**
 * This state is called after the PlayState and starts the new turn
 */
public enum EndState implements GameState {

    INSTANCE;

    @Override
    public void next(GameSession gameSession, StateContext context) {
        context.setDevCardPlayedBeforeDice(false);
        if (!context.isSetupPhaseCompleted()) {
            gameSession.setCurrentState(SetupState.INSTANCE);
        } else {
            gameSession.setCurrentState(DiceState.INSTANCE);
        }
    }

    @Override
    public void action(GameSession gameSession, StateContext context) {
    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        gameSession.startNewTurnTimer();
        next(gameSession, context);
    }
}