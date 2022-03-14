package de.uol.swp.server.usermanagement;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.request.RegisterUserRequest;
import de.uol.swp.common.user.response.RegistrationSuccessfulResponse;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings({"UnstableApiUsage", "PMD.CommentRequired", "PMD.DefaultPackage"})
class UserServiceTest {

    static final User USER_TO_REGISTER = new UserDTO("Marco", "Marco", "Marco@Grawunder.com");
    static final User USER_WITH_SAME_NAME = new UserDTO("Marco", "Marco2", "Marco2@Grawunder.com");

    final EventBus bus = new EventBus();
    final UserManagement userManagement = new UserManagement(new MainMemoryBasedUserStore());
    final UserService userService = new UserService(bus, userManagement);
    final CountDownLatch lock = new CountDownLatch(1);
    Object event;

    @Subscribe
    void handle(DeadEvent event) {
        this.event = event.getEvent();
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
    void registerUserTest() {
        final RegisterUserRequest request = new RegisterUserRequest(USER_TO_REGISTER);
        MessageContext ctx = mock(MessageContext.class);
        request.setMessageContext(ctx);

        // The post will lead to a call of a UserService function
        bus.post(request);
        assertEquals(ctx, ((RegistrationSuccessfulResponse) event).getMessageContext().get());

        // can only test, if something in the state has changed
        final User loggedInUser = userManagement.login(USER_TO_REGISTER.getUsername(), USER_TO_REGISTER.getPlainPassword());

        assertNotNull(loggedInUser);
        assertEquals(USER_TO_REGISTER, loggedInUser);
    }

    @Test
    void registerSecondUserWithSameName() {
        final RegisterUserRequest request = new RegisterUserRequest(USER_TO_REGISTER);
        final RegisterUserRequest request2 = new RegisterUserRequest(USER_WITH_SAME_NAME);

        bus.post(request);
        bus.post(request2);

        final User loggedInUser = userManagement.login(USER_TO_REGISTER.getUsername(), USER_TO_REGISTER.getPlainPassword());

        // old user should be still in the store
        assertNotNull(loggedInUser);
        assertEquals(USER_TO_REGISTER, loggedInUser);

        // old user should not be overwritten!
        assertNotEquals(loggedInUser.getEMail(), USER_WITH_SAME_NAME.getEMail());

    }

}