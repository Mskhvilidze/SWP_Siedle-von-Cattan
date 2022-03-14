package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.player.PlayerProfile;

import java.util.Objects;

/**
 * A response sent by the player's info
 */
public class PlayerInfoResponseMessage extends AbstractResponseMessage {

    private final PlayerProfile profile;

    /**
     * Constructor
     *
     * @param profile the player information that should be displayed
     */
    public PlayerInfoResponseMessage(PlayerProfile profile) {
        this.profile = profile;
    }

    /**
     * Getter for the player info
     *
     * @return player's info
     */
    public PlayerProfile getProfile() {
        return profile;
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
        PlayerInfoResponseMessage that = (PlayerInfoResponseMessage) object;
        return Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), profile);
    }
}
