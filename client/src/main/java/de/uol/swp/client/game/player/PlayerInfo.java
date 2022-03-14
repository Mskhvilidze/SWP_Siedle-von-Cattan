package de.uol.swp.client.game.player;

import de.uol.swp.common.game.board.Port;
import de.uol.swp.common.game.board.ResourceEnumMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.EnumSet;
import java.util.Set;

/**
 * A class which includes some player information
 */
public class PlayerInfo {
    private final BooleanProperty portUpdate = new SimpleBooleanProperty();
    private final Set<Port> ports = EnumSet.noneOf(Port.class);
    private final ResourceEnumMap resources = new ResourceEnumMap();

    /**
     * This property gets updated (set to false or true) if the {@link #ports} Set has been updated
     *
     * @return the portUpdateProperty
     */
    public BooleanProperty portUpdateProperty() {
        return portUpdate;
    }

    /**
     * Returns the ports of a player
     *
     * @return returns the ports of a player
     */
    public Set<Port> getPorts() {
        return ports;
    }

    /**
     * Sets the players ports
     *
     * @param ports the ports of the player
     */
    public void setPorts(Set<Port> ports) {
        var oldPorts = this.ports;
        this.ports.clear();
        this.ports.addAll(ports);
        if (!ports.equals(oldPorts)) {
            portUpdate.set(!portUpdate.get());
        }
    }

    /**
     * Returns the resources of a player
     *
     * @return returns the resources of a player
     */
    public ResourceEnumMap getResources() {
        return resources;
    }

    /**
     * Sets the players resources
     *
     * @param resources the resources of the player
     */
    public void setResources(ResourceEnumMap resources) {
        this.resources.clear();
        this.resources.putAll(resources);
    }

}
