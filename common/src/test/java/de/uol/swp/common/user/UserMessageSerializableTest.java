package de.uol.swp.common.user;

import de.uol.swp.common.SerializationTestHelper;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.exception.RegistrationExceptionMessage;
import de.uol.swp.common.user.message.UserLoggedInMessage;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.message.UsersListMessage;
import de.uol.swp.common.user.request.LoginRequest;
import de.uol.swp.common.user.request.LogoutRequest;
import de.uol.swp.common.user.request.RegisterUserRequest;
import de.uol.swp.common.user.request.RetrieveAllOnlineUsersRequest;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"PMD.CommentRequired"})
class UserMessageSerializableTest {

    static final User DEFAULT_USER = new UserDTO("marco", "marco", "marco@grawunder.de");

    static final int SIZE = 10;
    static final List<String> USERS = new ArrayList<>();

    static {
        for (int i = 0; i < SIZE; i++) {
            USERS.add("User" + i);
        }
    }

    @Test
    void testUserMessagesSerializable() {
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new UserLoggedInMessage("test"),
                UserLoggedInMessage.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new UserLoggedOutMessage("test"),
                UserLoggedOutMessage.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new UsersListMessage(USERS),
                UsersListMessage.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new RegistrationExceptionMessage("Error"),
                RegistrationExceptionMessage.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new LoginSuccessfulResponse(DEFAULT_USER),
                LoginSuccessfulResponse.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new LoginRequest("name", "pass"),
                LoginRequest.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new LogoutRequest(), LogoutRequest.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new RegisterUserRequest(DEFAULT_USER),
                RegisterUserRequest.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new RetrieveAllOnlineUsersRequest(),
                RetrieveAllOnlineUsersRequest.class));

    }
}
