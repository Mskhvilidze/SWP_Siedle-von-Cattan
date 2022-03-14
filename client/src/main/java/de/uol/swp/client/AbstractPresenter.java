package de.uol.swp.client;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.client.game.GameSessionService;
import de.uol.swp.client.lobby.LobbyService;
import de.uol.swp.client.user.ClientUserService;
import de.uol.swp.client.user.UserInfo;

/**
 * This class is the base for creating a new Presenter.
 * <p>
 * This class prepares the child classes to have various services and EventBus set
 * in order to reduce unnecessary code repetition.
 *
 * @author Marco Grawunder
 * @since 2019-08-29
 */
@SuppressWarnings("UnstableApiUsage")
public class AbstractPresenter {

    @Inject
    protected ClientUserService userService;
    @Inject
    protected GameSessionService gameSessionService;
    @Inject
    protected LobbyService lobbyService;
    @Inject
    protected UserInfo userInfo;

    protected EventBus eventBus;

    /**
     * Sets the field eventBus
     * <p>
     * This method sets the field eventBus to the EventBus given via parameter.
     * Afterwards it registers this class to the new EventBus.
     *
     * @param eventBus the EventBus this class should use.
     * @implNote This method does not unregister this class from any EventBus it
     * may already be registered to.
     * @since 2019-08-29
     */
    @Inject
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    /**
     * Clears the field eventBus
     * <p>
     * This method clears the field eventBus. Before clearing it unregisters this
     * class from EventBus previously used.
     *
     * @implNote This method does not check whether the field eventBus is null.
     * The field is cleared by setting it to null.
     * @since 2019-08-29
     */
    public void clearEventBus() {
        this.eventBus.unregister(this);
        this.eventBus = null;
    }
}
