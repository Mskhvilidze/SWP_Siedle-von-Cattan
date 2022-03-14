package de.uol.swp.client.user;

import com.google.inject.Singleton;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.player.PlayerProfile;

/**
 * This class stores the currently logged in user and profile info
 */
@Singleton
public class UserInfo {
    private User loggedInUser;
    private PlayerProfile loggedInProfile = new PlayerProfile("", "", "0", "0");

    /**
     * Returns the currently logged in user
     *
     * @return the currently logged in user
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Returns the player info of the currently logged in user
     *
     * @return the player info of the currently logged in user
     */
    public PlayerProfile getLoggedInProfile() {
        return loggedInProfile;
    }

    /**
     * Sets the currently logged in user and updates the logged in profile accordingly
     *
     * @param loggedInUser the currently logged in user
     */
    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        if (loggedInUser != null) {
            loggedInProfile = new PlayerProfile(loggedInUser.getUsername(), loggedInUser.getEMail(), loggedInProfile.getWon(),
                    loggedInProfile.getLoss());
        } else {
            loggedInProfile = null;
        }
    }

    /**
     * Sets the currently logged in profile
     *
     * @param loggedInProfile the currently logged in profile
     */
    public void setLoggedInProfile(PlayerProfile loggedInProfile) {
        this.loggedInProfile = loggedInProfile;
    }
}
