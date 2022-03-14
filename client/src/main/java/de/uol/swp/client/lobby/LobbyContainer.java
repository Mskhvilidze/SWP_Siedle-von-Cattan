package de.uol.swp.client.lobby;

import de.uol.swp.client.main.tab.SessionTab;

/**
 * This class is used to store a LobbyPresenter and the tab that displays its content
 */
public class LobbyContainer {
    private final LobbyPresenter presenter;
    private final SessionTab sessionTab;

    /**
     * Constructor
     *
     * @param presenter  the {@code LobbyPresenter} of this container
     * @param sessionTab the {@code SessionTab} that displays the presenters content
     */
    public LobbyContainer(LobbyPresenter presenter, SessionTab sessionTab) {
        this.presenter = presenter;
        this.sessionTab = sessionTab;
    }

    /**
     * Returns the {@code LobbyPresenter} of this container
     *
     * @return the {@code LobbyPresenter} of this container
     */
    public LobbyPresenter getPresenter() {
        return presenter;
    }

    /**
     * Returns the {@code SessionTab} that displays the content of the presenter in this container
     *
     * @return the {@code SessionTab} that displays the content of the presenter in this container
     */
    public SessionTab getSessionTab() {
        return sessionTab;
    }
}
