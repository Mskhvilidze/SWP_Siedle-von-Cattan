package de.uol.swp.server.usermanagement;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.request.LeaveGameRequest;
import de.uol.swp.common.lobby.request.LeaveLobbyRequest;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.request.*;
import de.uol.swp.common.user.response.AllOnlineUsersResponse;
import de.uol.swp.common.user.response.ChangeUserInfoSuccessfulResponse;
import de.uol.swp.common.user.response.EditPasswordResponse;
import de.uol.swp.common.user.response.InvalidPasswordResponse;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.communication.UUIDSession;
import de.uol.swp.server.message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.*;

/**
 * Mapping authentication event bus calls to user management calls
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.AbstractService
 * @since 2019-08-30
 */
@SuppressWarnings({"UnstableApiUsage", "PMD.AvoidCatchingGenericException"})
@Singleton
public class AuthenticationService extends AbstractService {
    private static final Logger LOG = LogManager.getLogger(AuthenticationService.class);

    /**
     * The list of current logged in users
     */
    private final Map<Session, User> userSessions = new HashMap<>();

    private final UserManagement userManagement;

    /**
     * Constructor
     *
     * @param bus            the EventBus used throughout the entire server
     * @param userManagement object of the UserManagement to use
     * @see de.uol.swp.server.usermanagement.UserManagement
     * @since 2019-08-30
     */
    @Inject
    public AuthenticationService(EventBus bus, UserManagement userManagement) {
        super(bus);
        this.userManagement = userManagement;
    }

    /**
     * a new session is created and saved with the user
     * @param user who will be inserted
     */
    public void createSessionForTest(User user){
        Session newSession = UUIDSession.create(user);
        userSessions.put(newSession, user);
    }

    /**
     * Searches the Session for a given user
     *
     * @param user user whose Session is to be searched
     * @return either empty Optional or Optional containing the Session
     * @see de.uol.swp.common.user.Session
     * @see de.uol.swp.common.user.User
     * @since 2019-09-04
     */
    public Optional<Session> getSession(User user) {
        Optional<Map.Entry<Session, User>> entry = userSessions.entrySet().stream().filter(e -> e.getValue().equals(user)).findFirst();
        return entry.map(Map.Entry::getKey);
    }

    /**
     * Searches the Sessions for a Set of given users
     *
     * @param users a Set of users whose Sessions are to be searched
     * @return a List containing the Sessions that where found
     * @see de.uol.swp.common.user.Session
     * @see de.uol.swp.common.user.User
     * @since 2019-10-08
     */
    public List<Session> getSessions(Set<User> users) {
        List<Session> sessions = new ArrayList<>();
        users.forEach(u -> {
            Optional<Session> session = getSession(u);
            session.ifPresent(sessions::add);
        });
        return sessions;
    }

    /**
     * Handles LoginRequests found on the EventBus
     * <p>
     * If a LoginRequest is detected on the EventBus, this method is called. It
     * tries to login a user via the UserManagement. If this succeeds the user and
     * his Session are stored in the userSessions Map and a ClientAuthorizedMessage
     * is posted on the EventBus otherwise a ServerExceptionMessage gets posted
     * there.
     *
     * @param msg the LoginRequest
     * @see de.uol.swp.common.user.request.LoginRequest
     * @see de.uol.swp.server.message.ClientAuthorizedMessage
     * @see de.uol.swp.server.message.ServerExceptionMessage
     * @since 2019-08-30
     */
    @Subscribe
    public void onLoginRequest(LoginRequest msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got new auth message with User: {}, Pass: {}", msg.getUsername(), msg.getPassword());
        }
        ServerInternalMessage returnMessage;
        try {
            User newUser = userManagement.login(msg.getUsername(), msg.getPassword());
            returnMessage = new ClientAuthorizedMessage(newUser);
            Session newSession = UUIDSession.create(newUser);
            userSessions.put(newSession, newUser);
            returnMessage.setSession(newSession);
        } catch (SecurityException e) {
            LOG.error(e);
            returnMessage = new ServerExceptionMessage(new LoginException("Cannot auth user " + msg.getUsername()));
        } catch (UserManagementException e) {
            LOG.error(e);
            returnMessage = new ServerExceptionMessage(new LoginException("User already logged in " + msg.getUsername()));
        }
        Optional<MessageContext> ctx = msg.getMessageContext();
        if (ctx.isPresent()) {
            returnMessage.setMessageContext(ctx.get());
        }
        post(returnMessage);
    }

    /**
     * Handles LogoutRequests found on the EventBus
     * <p>
     * If a LogoutRequest is detected on the EventBus, this method is called. It
     * tries to logout a user via the UserManagement. If this succeeds the user and
     * his Session are removed from the userSessions Map and a UserLoggedOutMessage
     * is posted on the EventBus.
     *
     * @param msg the LogoutRequest
     * @see de.uol.swp.common.user.request.LogoutRequest
     * @see de.uol.swp.common.user.message.UserLoggedOutMessage
     * @since 2019-08-30
     */
    @Subscribe
    public void onLogoutRequest(LogoutRequest msg) {
        Optional<Session> session = msg.getSession();
        if (session.isPresent()) {
            User userToLogOut = userSessions.get(session.get());

            // Could be already logged out
            if (userToLogOut != null) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Logging out user {}", userToLogOut.getUsername());
                }

                userManagement.logout(userToLogOut);
                userSessions.remove(session.get());
                Session sessionToLogOutOf = session.get();
                for (String lobby : sessionToLogOutOf.getLobbies()) {
                    LeaveLobbyRequest leaveLobbyRequest = new LeaveLobbyRequest(lobby);
                    LeaveGameRequest leaveGameRequest = new LeaveGameRequest(lobby);
                    leaveLobbyRequest.setSession(sessionToLogOutOf);
                    leaveGameRequest.setSession(sessionToLogOutOf);
                    post(leaveLobbyRequest);
                    post(leaveGameRequest);
                }
                ServerMessage returnMessage = new UserLoggedOutMessage(userToLogOut.getUsername());
                post(returnMessage);

            }
        }
    }

    /**
     * Handles DropAccountUserRequest found on the EventBus
     * <p>
     * If a DropAccountUserRequest is detected on the EventBus, this method is called.
     * An attempt is being made to delete a user via UserManagement. If the user is deleted successfully and
     * His session is removed from the userSessions map and a UserAccountDropMessage
     * will be published in the EventBus.
     * <p>
     * If the passwords are not the same, the client will be notified via NotConfirmationPasswordResponse
     *
     * @param msg Delete request for account
     * @see DropAccountUserRequest
     * @see de.uol.swp.common.user.message.UserAccountDropMessage
     * @see InvalidPasswordResponse
     */
    @Subscribe
    public void onDropUserAccountRequest(DropAccountUserRequest msg) {
        Optional<Session> session = msg.getSession();
        if (session.isPresent()) {
            User userToDrop = userSessions.get(session.get());

            //Could be already deleted
            if (userToDrop != null) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Delete User {}", userToDrop.getUsername());
                }

                boolean isValidateUser = userManagement.isValidateUser(userToDrop.getUsername(), msg.getPassword());

                if (isValidateUser) {
                    userManagement.dropUser(userToDrop);
                    userSessions.remove(session.get());
                    Optional<MessageContext> ctx = msg.getMessageContext();

                    ServerInternalMessage response = new ClientDroppedMessage(userToDrop);
                    if (ctx.isPresent()) {
                        response.setMessageContext(ctx.get());

                        response.setSession(session.get());
                    }
                    post(response);
                } else {
                    InvalidPasswordResponse response = new InvalidPasswordResponse();
                    response.initWithMessage(msg);
                    post(response);
                }
            }
        }
    }

    /**
     * Handles ChangeUserInfoRequest found on the EventBus
     * <p>
     * If a ChangeUserInfoRequest is recognized in the EventBus, this method is called. It
     * tries to update a user via UserManagement. After the update,
     * the old session is deleted and a new one is created, then it is posted to EventBus
     *
     * @param changeUserInfoRequest the ChangeUserInfoRequest
     * @see de.uol.swp.common.user.response.ChangeUserInfoSuccessfulResponse
     * @see de.uol.swp.common.user.message.ChangedUserInfoMessage
     * @see ClientChangedUserInfoMessage
     * @see Session
     */
    @Subscribe
    public void onChangeUserInfo(ChangeUserInfoRequest changeUserInfoRequest) {
        Optional<Session> session = changeUserInfoRequest.getSession();
        if (ChangeUserInfoRequest.InfoType.USERNAME == changeUserInfoRequest.getInfoType() && session.isPresent()) {
            User userToUpdate = session.get().getUser();
            String oldUserName = userToUpdate.getUsername();

            LOG.debug("Update userName {}", userToUpdate.getUsername());

            User updatedUser = userManagement.updateUserName(userToUpdate, changeUserInfoRequest.getNewValue());
            Optional<MessageContext> ctx = changeUserInfoRequest.getMessageContext();

            session.get().updateUser(updatedUser);
            userSessions.put(session.get(), updatedUser);
            ServerInternalMessage response = new ClientChangedUserInfoMessage(oldUserName, UserDTO.create(updatedUser), session.get().getLobbies());

            if (ctx.isPresent()) {
                response.setMessageContext(ctx.get());
                response.setSession(session.get());
            }
            post(response);

        } else if (ChangeUserInfoRequest.InfoType.EMAIL == changeUserInfoRequest.getInfoType() && session.isPresent()) {
            User userToUpdate = session.get().getUser();
            session.get().updateUser(userToUpdate);

            User updatedUser = userManagement.updateUserEmail(userToUpdate, changeUserInfoRequest.getNewValue());
            userSessions.put(session.get(), updatedUser);
            ChangeUserInfoSuccessfulResponse response = new ChangeUserInfoSuccessfulResponse(UserDTO.create(updatedUser));
            response.initWithMessage(changeUserInfoRequest);
            post(response);

        } else {
            if (session.isPresent()) {
                User userToUpdate = session.get().getUser();

                boolean updatedUserPassword = userManagement.updateUserPassword(userToUpdate, changeUserInfoRequest.getOldValue(),
                        changeUserInfoRequest.getNewValue());
                EditPasswordResponse response = new EditPasswordResponse(updatedUserPassword);
                response.initWithMessage(changeUserInfoRequest);
                post(response);
            }
        }
    }

    /**
     * Handles RetrieveAllOnlineUsersRequests found on the EventBus
     * <p>
     * If a RetrieveAllOnlineUsersRequest is detected on the EventBus, this method
     * is called. It posts a AllOnlineUsersResponse containing user objects for
     * every logged in user on the EvenBus.
     *
     * @param msg the RetrieveAllOnlineUsersRequest found on the EventBus
     * @see de.uol.swp.common.user.request.RetrieveAllOnlineUsersRequest
     * @see de.uol.swp.common.user.response.AllOnlineUsersResponse
     * @since 2019-08-30
     */
    @Subscribe
    public void onRetrieveAllOnlineUsersRequest(RetrieveAllOnlineUsersRequest msg) {
        AllOnlineUsersResponse response = new AllOnlineUsersResponse(userSessions.values());
        response.initWithMessage(msg);
        post(response);
    }
    
}