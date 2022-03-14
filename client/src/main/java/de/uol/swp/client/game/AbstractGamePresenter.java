package de.uol.swp.client.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.game.player.PlayerInfo;
import de.uol.swp.common.game.dto.PlayerDTO;

/**
 * This class is the base for creating a new Game Presenter.
 * <p>
 * This class prepares the child classes to have an EventBus set
 * that is connected to all presenters of a game session and commonly needed game info
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractGamePresenter extends AbstractPresenter {

    protected String gameSessionName;
    protected GameSessionPresenter gameSessionPresenter;
    protected EventBus secondEventBus;
    protected PlayerInfo playerInfo;
    protected PlayerDTO[] players;

    public void setSecondEventBus(EventBus secondEventBus) {
        this.secondEventBus = secondEventBus;
        secondEventBus.register(this);
    }

    @Subscribe
    public void setGameSessionPresenter(GameSessionPresenter gameSessionPresenter) {
        this.gameSessionPresenter = gameSessionPresenter;
        this.gameSessionName = gameSessionPresenter.getGameSessionName();
    }

    @Subscribe
    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    @Subscribe
    public void setPlayers(PlayerDTO[] players) {
        this.players = players.clone();
    }
}
