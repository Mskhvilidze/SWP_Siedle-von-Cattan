package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.player.PlayerProfile;

import java.util.List;
import java.util.Objects;

/**
 * A response, that the statistics from player got send
 *
 * @see de.uol.swp.common.message.AbstractResponseMessage
 */
public class StatisticResponseMessage extends AbstractResponseMessage {

    private final List<PlayerProfile> list;

    /**
     * Constructor
     *
     * @param list a list containing the statistics that should be displayed
     */
    public StatisticResponseMessage(List<PlayerProfile> list) {
        this.list = list;
    }

    /**
     * Getter for List
     *
     * @return list with playerStatistic
     */
    public List<PlayerProfile> getList() {
        return list;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        StatisticResponseMessage that = (StatisticResponseMessage) object;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), list);
    }
}
