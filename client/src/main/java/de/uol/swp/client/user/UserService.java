package de.uol.swp.client.user;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.request.*;


/**
 * This class is used to hide the communication details
 * implements de.uol.common.user.UserService
 *
 * @author Marco Grawunder
 * @see ClientUserService
 * @since 2017-03-17
 */
@SuppressWarnings("UnstableApiUsage")
public class UserService implements ClientUserService {

    private final EventBus bus;

    /**
     * Constructor
     *
     * @param bus the EventBus set in ClientModule
     * @see de.uol.swp.client.di.ClientModule
     * @since 2017-03-17
     */
    @Inject
    public UserService(EventBus bus) {
        this.bus = bus;
    }

    /**
     * Posts a login request to the EventBus
     *
     * @param username the name of the user
     * @param password the password of the user
     * @since 2017-03-17
     */
    @Override
    public void login(String username, String password) {
        LoginRequest msg = new LoginRequest(username, password);
        bus.post(msg);
    }


    @Override
    public void logout(User username) {
        LogoutRequest msg = new LogoutRequest();
        bus.post(msg);
    }

    @Override
    public void createUser(User user) {
        RegisterUserRequest request = new RegisterUserRequest(user);
        bus.post(request);
    }

    /**
     * Method for deleting this user account
     * <p>
     * This method should send a request to delete this user account
     * <p>
     * The message is sent to the server via the event bus
     *
     * @param confirmPassword to confirm
     * @see DropAccountUserRequest
     */
    @Override
    public void dropUser(String confirmPassword) {
        DropAccountUserRequest dropAccountUserRequest = new DropAccountUserRequest(confirmPassword);
        bus.post(dropAccountUserRequest);
    }

    /**
     * Method to change a users name
     * <p>
     * This method currently sends a ChangeUserInfoRequest to updates the users name, but that is open to change.
     *
     * @param newName the new username
     */
    @Override
    public void changeUserName(String newName) {
        ChangeUserInfoRequest request = new ChangeUserInfoRequest(ChangeUserInfoRequest.InfoType.USERNAME, newName);
        bus.post(request);
    }

    /**
     * Method to change a users password
     * <p>
     * This method currently sends a ChangeUserInfoRequest to updates the users password, but that is open to change.
     *
     * @param currentPassword the current password
     * @param newPassword     the new password
     */
    @Override
    public void changeUserPassword(String currentPassword, String newPassword) {
        ChangeUserInfoRequest request = new ChangeUserInfoRequest(ChangeUserInfoRequest.InfoType.PASSWORD, currentPassword, newPassword);
        bus.post(request);
    }

    /**
     * Method to change a users email
     * <p>
     * This method currently sends a ChangeUserInfoRequest to updates the users email, but that is open to change.
     *
     * @param newEmail the new email
     */
    @Override
    public void changeUserEmail(String newEmail) {
        ChangeUserInfoRequest request = new ChangeUserInfoRequest(ChangeUserInfoRequest.InfoType.EMAIL, newEmail);
        bus.post(request);
    }

    @Override
    public void retrieveAllUsers() {
        RetrieveAllOnlineUsersRequest cmd = new RetrieveAllOnlineUsersRequest();
        bus.post(cmd);
    }

    @Override
    public void retrievePlayerProfile() {
        PlayerInfoRequest response = new PlayerInfoRequest();
        bus.post(response);
    }
}
