package de.uol.swp.client.user;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.request.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This a test of the class is used to hide the communication details
 *
 * @author Marco Grawunder
 * @see de.uol.swp.client.user.UserService
 * @since 2019-10-10
 */
@SuppressWarnings({"UnstableApiUsage", "PMD.DefaultPackage"})
class UserServiceTest {

    static final Logger LOG = LogManager.getLogger(UserServiceTest.class);
    final User defaultUser = new UserDTO("Marco", "test", "marco@test.de");
    final EventBus bus = new EventBus();
    final CountDownLatch lock = new CountDownLatch(1);
    final UserService userService = new UserService(bus);
    Object event;

    /**
     * Handles DeadEvents detected on the EventBus
     * <p>
     * If a DeadEvent is detected the event variable of this class gets updated
     * to its event and its event is printed to the console output.
     *
     * @param event the DeadEvent detected on the EventBus
     * @since 2019-10-10
     */
    @Subscribe
    void handle(DeadEvent event) {
        this.event = event.getEvent();
        LOG.info("Dead event: {}\n", this.event);
        lock.countDown();
    }

    /**
     * Helper method run before each test case
     * <p>
     * This method resets the variable event to null and registers the object of
     * this class to the EventBus.
     *
     * @since 2019-10-10
     */
    @BeforeEach
    void registerBus() {
        event = null;
        bus.register(this);
    }

    /**
     * Helper method run after each test case
     * <p>
     * This method only unregisters the object of this class from the EventBus.
     *
     * @since 2019-10-10
     */
    @AfterEach
    void deregisterBus() {
        bus.unregister(this);
    }

    /**
     * Subroutine used for tests that need a logged in user
     * <p>
     * This subroutine creates a new UserService object registered to the EventBus
     * of this test class and class the objects login method for the default user.
     *
     * @throws InterruptedException thrown by lock.await()
     * @since 2019-10-10
     */
    private void loginUser() throws InterruptedException {
        userService.login(defaultUser.getUsername(), defaultUser.getPlainPassword());
        lock.await(1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Test for the login method
     * <p>
     * This test first calls the loginUser subroutine. Afterwards it checks if a
     * LoginRequest object got posted to the EventBus and if its content is the
     * default users information.
     * The test fails if any of the checks fail.
     *
     * @throws InterruptedException thrown by loginUser()
     * @since 2019-10-10
     */
    @Test
    @DisplayName("LoginRequest should contain the correct username and password")
    void loginRequest_ShouldContainCorrectInfo() throws InterruptedException {
        loginUser();

        assertTrue(event instanceof LoginRequest);

        LoginRequest loginRequest = (LoginRequest) event;
        assertEquals(loginRequest.getUsername(), defaultUser.getUsername());
        assertEquals(loginRequest.getPassword(), defaultUser.getPlainPassword());
    }

    /**
     * Test for the logout method
     * <p>
     * This test calls the logout function of the userService using the defaultUser as parameter
     * and waits for it to post an LogoutRequest object on the EventBus. It then
     * checks if authorization is needed to logout the user.
     * The test fails if no LogoutRequest is posted within one second or the request
     * says that no authorization is needed
     *
     * @throws InterruptedException thrown by loginUser() and lock.await()
     * @since 2019-10-10
     */
    @Test
    @DisplayName("LogoutRequest should require authorization")
    void logoutRequest_ShouldRequireAuthorization() throws InterruptedException {
        userService.logout(defaultUser);
        lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(event instanceof LogoutRequest);

        LogoutRequest request = (LogoutRequest) event;

        assertTrue(request.authorizationNeeded());
    }

    /**
     * Test for the createUser routine
     * <p>
     * This Test creates a new UserService object registered to the EventBus of
     * this test class. It then calls the createUser function of the object using
     * the defaultUser as parameter and waits for it to post an updateUserRequest
     * object on the EventBus.
     * If this happens within one second, it checks if the user in the request object
     * is the same as the default user and if authorization is needed.
     * Authorization should not be needed.
     * If any of these checks fail or the method takes to long, this test is unsuccessful.
     *
     * @throws InterruptedException thrown by lock.await()
     * @since 2019-10-10
     */
    @Test
    @DisplayName("RegisterUserRequest should contain the correct username, password and email")
    void registerUserRequest_ShouldContainCorrectInfo() throws InterruptedException {
        userService.createUser(defaultUser);

        lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(event instanceof RegisterUserRequest);

        RegisterUserRequest request = (RegisterUserRequest) event;

        assertEquals(request.getUser().getUsername(), defaultUser.getUsername());
        assertEquals(request.getUser().getPlainPassword(), defaultUser.getPlainPassword());
        assertEquals(request.getUser().getEMail(), defaultUser.getEMail());
        assertFalse(request.authorizationNeeded());
    }

    /**
     * Test for the dropUser routine
     * <p>
     * This test first calls a newly created UserServiceObject, then calls the dropUser subroutine.
     * that is registered in the EventBus of this test class. It then
     * calls the delete function of the object with the parameter defaultUser as a parameter
     * and waits for a DropAccountUserRequest object to be published in the EventBus.
     * The test fails if no DropUserAccountRequest is sent within a second or when the request
     * says no authorization is required
     *
     * @throws InterruptedException thrown by lock.await()
     * @since 2019-10-10
     */
    @Test
    @DisplayName("DropAccountUserRequest should contain the correct password and require no auth")
    void dropAccountUserRequest_ShouldContainCorrectInfo() throws InterruptedException {
        userService.dropUser(defaultUser.getPlainPassword());

        lock.await(1000, TimeUnit.MILLISECONDS);
        assertTrue(event instanceof DropAccountUserRequest);

        DropAccountUserRequest request = (DropAccountUserRequest) event;
        assertEquals(defaultUser.getPlainPassword(), request.getPassword());
        assertTrue(request.authorizationNeeded());
    }

    /**
     * Test for the retrievePlayerProfile method
     * <p>
     * This test calls the retrievePlayerProfile function of the userService
     * and waits for it to post an PlayerInfoRequest object on the EventBus. It then
     * checks if authorization is needed to logout the user.
     * The test fails if no PlayerInfoRequest is posted within one second or the request
     * says that no authorization is needed
     *
     * @throws InterruptedException thrown lock.await()
     */
    @Test
    @DisplayName("PlayerInfoRequest should require authorization")
    void playerInfoRequest_ShouldRequireAuthorization() throws InterruptedException {
        UserService userService = new UserService(bus);
        userService.retrievePlayerProfile();

        lock.await(1000, TimeUnit.MILLISECONDS);
        assertTrue(event instanceof PlayerInfoRequest);
        assertTrue(((PlayerInfoRequest) event).authorizationNeeded());
    }

    /**
     * Test for the retrieveAllUsers method
     * <p>
     * This test calls the retrieveAllUsers function of the userService
     * and waits for it to post an RetrieveAllOnlineUsersRequest object on the EventBus. It then
     * checks if authorization is needed to logout the user.
     * The test fails if no RetrieveAllOnlineUsersRequest is posted within one second or the request
     * says that no authorization is needed
     *
     * @throws InterruptedException thrown lock.await()
     */
    @Test
    @DisplayName("RetrieveAllOnlineUsersRequest should require authorization")
    void retrieveAllOnlineUsersRequest_ShouldRequireAuthorization() throws InterruptedException {
        UserService userService = new UserService(bus);
        userService.retrieveAllUsers();

        lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(event instanceof RetrieveAllOnlineUsersRequest);
    }
}