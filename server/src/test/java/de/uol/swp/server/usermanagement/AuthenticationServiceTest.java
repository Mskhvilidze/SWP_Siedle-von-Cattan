package de.uol.swp.server.usermanagement;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.request.DropAccountUserRequest;
import de.uol.swp.common.user.request.LoginRequest;
import de.uol.swp.common.user.request.LogoutRequest;
import de.uol.swp.common.user.request.RetrieveAllOnlineUsersRequest;
import de.uol.swp.common.user.response.AllOnlineUsersResponse;
import de.uol.swp.server.message.ClientAuthorizedMessage;
import de.uol.swp.server.message.ClientDroppedMessage;
import de.uol.swp.server.message.ServerExceptionMessage;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.AccessorMethodGeneration", "UnstableApiUsage", "PMD.CommentRequired", "PMD.LinguisticNaming", "PMD.DefaultPackage"})
class AuthenticationServiceTest {

    static final Logger LOG = LogManager.getLogger(AuthenticationServiceTest.class);
    final User user = new UserDTO("name", "password", "email@test.de");
    final User user2 = new UserDTO("name2", "password2", "email@test.de2");
    final User user3 = new UserDTO("name3", "password3", "email@test.de3");
    final UserStore userStore = new MainMemoryBasedUserStore();
    final EventBus bus = new EventBus();
    final UserManagement userManagement = new UserManagement(userStore);
    final AuthenticationService authService = new AuthenticationService(bus, userManagement);
    final CountDownLatch lock = new CountDownLatch(1);
    Object event;

    @Subscribe
    void handle(DeadEvent event) {
        this.event = event.getEvent();
        LOG.info("Dead event: {}\n", this.event);
        lock.countDown();
    }

    @BeforeEach
    void registerBus() {
        event = null;
        bus.register(this);
    }

    @AfterEach
    void deregisterBus() {
        bus.unregister(this);
    }

    @Test
    void logoutTest() throws InterruptedException {
        loginUser(user);
        Optional<Session> session = authService.getSession(user);

        assertTrue(session.isPresent());
        final LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setSession(session.get());

        bus.post(logoutRequest);

        lock.await(1000, TimeUnit.MILLISECONDS);

        assertFalse(userManagement.isLoggedIn(user));
        assertFalse(authService.getSession(user).isPresent());
        assertTrue(event instanceof UserLoggedOutMessage);
    }

    @Test
    void dropUserTest() throws InterruptedException {
        loginUser(user);
        Optional<Session> session = authService.getSession(user);

        assertTrue(session.isPresent());

        final DropAccountUserRequest request = new DropAccountUserRequest(user.getPlainPassword());
        request.setSession(session.get());

        authService.onDropUserAccountRequest(request);
        lock.await(1000, TimeUnit.MILLISECONDS);

        assertFalse(userManagement.isLoggedIn(user));
        assertFalse(authService.getSession(user).isPresent());
        assertTrue(event instanceof ClientDroppedMessage);
    }

    LoginRequest loginUser(User userToLogin) {
        userManagement.createUser(userToLogin);
        LoginRequest loginRequest = new LoginRequest(userToLogin.getUsername(), userToLogin.getPlainPassword());
        MessageContext ctx = mock(MessageContext.class);
        loginRequest.setMessageContext(ctx);
        bus.post(loginRequest);
        return loginRequest;
    }

    @Test
    void getSessionsForUsersTest() {
        loginUser(user);
        loginUser(user2);
        loginUser(user3);
        Set<User> users = new TreeSet<>();
        users.add(user);
        users.add(user2);
        users.add(user3);

        Optional<Session> session1 = authService.getSession(user);
        Optional<Session> session2 = authService.getSession(user2);
        Optional<Session> session3 = authService.getSession(user2);

        assertTrue(session1.isPresent());
        assertTrue(session2.isPresent());
        assertTrue(session3.isPresent());

        List<Session> sessions = authService.getSessions(users);

        assertEquals(3, sessions.size());
        assertTrue(sessions.contains(session1.get()));
        assertTrue(sessions.contains(session2.get()));
        assertTrue(sessions.contains(session3.get()));
    }

    /**
     * Test subclass for all login related functions in {@code AuthenticationService}
     */
    @Nested
    class Login {
        /**
         * This test checks if the {@code AuthenticationService} logs in a user with a valid password
         */
        @Test
        void correctPassword_ShouldLoginUser() throws InterruptedException {
            loginUser(user);
            lock.await(1000, TimeUnit.MILLISECONDS);
            assertTrue(userManagement.isLoggedIn(user));
        }

        /**
         * This test checks if the {@code AuthenticationService} sends the correct response
         * to the user who successfully logged in
         */
        @Test
        void correctPassword_ShouldSendCorrectResponse() {
            LoginRequest loginRequest = loginUser(user);
            assertTrue(event instanceof ClientAuthorizedMessage);
            assertEquals(loginRequest.getMessageContext().get(), ((ClientAuthorizedMessage) event).getMessageContext().get());
        }

        /**
         * This test checks if the {@code AuthenticationService} does not log in a user without a valid password
         */
        @Test
        void wrongPassword_ShouldNotLoginUser() throws InterruptedException {
            userManagement.createUser(user);
            bus.post(new LoginRequest(user.getUsername(), user.getPlainPassword() + "äüö"));
            lock.await(1000, TimeUnit.MILLISECONDS);

            assertFalse(userManagement.isLoggedIn(user));
            assertTrue(event instanceof ServerExceptionMessage);
        }

        /**
         * This test checks if the {@code AuthenticationService} throws a {@code LoginException}
         * if the same user tries to log in twice
         */
        @Test
        void alreadyLoggedIn_ShouldThrowException() throws InterruptedException {
            loginUser(user);
            lock.await(1000, TimeUnit.MILLISECONDS);
            assertTrue(userManagement.isLoggedIn(user));

            bus.post(new LoginRequest(user.getUsername(), user.getPlainPassword()));
            assertTrue(event instanceof ServerExceptionMessage);
            assertEquals(LoginException.class, ((ServerExceptionMessage) event).getException().getClass());
        }

        /**
         * Test subclass for all functions related to retrieving a list of all logged in users
         */
        @Nested
        class RetrieveAllOnlineUsers {
            /**
             * This test checks if the {@code AuthenticationService} returns all logged in users
             * upon receiving a RetrieveAllOnlineUsersRequest if multiple users are online
             */
            @Test
            void multipleLoggedInUsers_ShouldReturnAllLoggedInUsers() throws InterruptedException {
                List<User> users = new ArrayList<>();
                users.add(user);
                users.add(user2);
                Collections.sort(users);

                users.forEach(AuthenticationServiceTest.this::loginUser);
                RetrieveAllOnlineUsersRequest request = new RetrieveAllOnlineUsersRequest();
                bus.post(request);
                lock.await(1000, TimeUnit.MILLISECONDS);

                assertTrue(event instanceof AllOnlineUsersResponse);
                List<User> returnedUsers = new ArrayList<>(((AllOnlineUsersResponse) event).getUsers());

                assertEquals(users.size(), returnedUsers.size());
                Collections.sort(returnedUsers);
                assertEquals(users, returnedUsers);
            }

            /**
             * This test checks if the {@code AuthenticationService} returns no users
             * upon receiving a RetrieveAllOnlineUsersRequest if no user is online
             */
            @Test
            void noLoggedInUsers_ShouldReturnNoUsers() throws InterruptedException {
                RetrieveAllOnlineUsersRequest request = new RetrieveAllOnlineUsersRequest();
                bus.post(request);
                lock.await(1000, TimeUnit.MILLISECONDS);
                assertTrue(event instanceof AllOnlineUsersResponse);

                assertTrue(((AllOnlineUsersResponse) event).getUsers().isEmpty());
            }
        }
    }
}