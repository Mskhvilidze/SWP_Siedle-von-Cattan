package de.uol.swp.client.main.tab;

/**
 * Event used to close a specific tab. Currently unused
 */
public class CloseTabEvent {
    private final String tabName;

    /**
     * Constructor
     *
     * @param tabName the name of the lobby that the tab represents
     */
    public CloseTabEvent(String tabName) {
        this.tabName = tabName;
    }

    /**
     * Returns the name of the tab that should be closed
     *
     * @return the name of the tab that should be closed
     */
    public String getTabName() {
        return tabName;
    }
}
