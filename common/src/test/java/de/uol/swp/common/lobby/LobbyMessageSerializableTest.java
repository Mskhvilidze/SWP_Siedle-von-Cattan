package de.uol.swp.common.lobby;

import de.uol.swp.common.SerializationTestHelper;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.message.UserJoinedLobbyMessage;
import de.uol.swp.common.lobby.message.UserLeftLobbyMessage;
import de.uol.swp.common.lobby.request.CreateLobbyRequest;
import de.uol.swp.common.lobby.request.JoinLobbyRequest;
import de.uol.swp.common.lobby.request.LeaveLobbyRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"PMD.CommentRequired"})
class LobbyMessageSerializableTest {

    static final UserDTO DEFAULT_USER = new UserDTO("marco", "marco", "marco@grawunder.de");

    @Test
    void testLobbyMessagesSerializable() {
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new CreateLobbyRequest("test", DEFAULT_USER, true),
                CreateLobbyRequest.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new JoinLobbyRequest("test"),
                JoinLobbyRequest.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new LeaveLobbyRequest("test"),
                LeaveLobbyRequest.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new UserJoinedLobbyMessage("test", DEFAULT_USER),
                UserJoinedLobbyMessage.class));
        assertTrue(SerializationTestHelper.checkSerializableAndDeserializable(new UserLeftLobbyMessage("test", DEFAULT_USER),
                UserLeftLobbyMessage.class));
    }


}
