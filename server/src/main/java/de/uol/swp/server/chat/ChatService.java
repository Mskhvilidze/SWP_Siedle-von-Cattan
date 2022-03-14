package de.uol.swp.server.chat;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.chat.ChatMessage;
import de.uol.swp.common.chat.SendChatMessageRequest;
import de.uol.swp.common.chat.SystemMessage;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.message.SendSystemMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Handles the chat messages sent by the users
 *
 * @author Lennart Bruns
 */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class ChatService extends AbstractService {

    private static final Logger LOG = LogManager.getLogger(ChatService.class);

    private final LobbyService lobbyService;

    /**
     * Constructor
     *
     * @param bus          the EvenBus used throughout the server
     * @param lobbyService the LobbyService used on the server
     */
    @Inject
    public ChatService(EventBus bus, LobbyService lobbyService) {
        super(bus);
        this.lobbyService = lobbyService;
    }

    /**
     * Handles UpdateMessageRequests found on the EventBus
     * <p>
     * If a UpdateMessageRequest is detected on the EventBus, this method is called.
     * It sends a new ChatMessage to every connected user using the parameters from
     * the request.
     * In case of a lobbyChat message is send the message to all players connected to that lobby
     *
     * @param request the UpdateMessageRequest found on the EventBus
     * @see de.uol.swp.common.chat.ChatMessage
     */
    @Subscribe
    public void onMessageReceived(SendChatMessageRequest request) {
        ZonedDateTime now = ZonedDateTime.now();
        Optional<Session> senderSession = request.getSession();
        if (senderSession.isEmpty() || ensureTimeDifference(request.getTimestamp(), now)) {
            return;
        }
        String lobbyName = request.getLobbyName();
        Optional<GameLobby> lobby = lobbyService.getLobby(lobbyName);
        User sender = senderSession.get().getUser();
        ServerMessage returnMessage = new ChatMessage(lobbyName, request.getContent(), UserDTO.create(sender), request.getTimestamp());

        if (lobby.isPresent() && lobby.get().getUsers().contains(sender)) {
            lobbyService.sendToAllInLobby(lobbyName, returnMessage);
        } else if ("global".equals(lobbyName)) {
            sendToAll(returnMessage);
        }
        LOG.info("Chat Message received from: {}, target lobby: {}", sender, request.getLobbyName());
    }

    /**
     * Sends a system message to the given lobby or global chat
     *
     * @param message
     */
    @Subscribe
    public void onSendSystemMessage(SendSystemMessage message) {
        ZonedDateTime now = ZonedDateTime.now();
        String lobbyName = message.getLobbyName();
        SystemMessage msg = new SystemMessage(lobbyName, message.getContent(), now);
        Optional<GameLobby> lobby = lobbyService.getLobby(lobbyName);
        if (lobby.isPresent()) {
            lobbyService.sendToAllInLobby(lobbyName, msg);
        } else if ("global".equals(lobbyName)) {
            sendToAll(msg);
        }
    }

    /**
     * Returns whether the difference between the given times is greater than 5 minutes
     *
     * @param time1 the first time
     * @param time2 the second time
     * @return {@code true} if the the difference between the given times is greater than 5 minutes
     */
    private boolean ensureTimeDifference(ZonedDateTime time1, ZonedDateTime time2) {
        return Math.abs(ChronoUnit.MINUTES.between(time1, time2)) > 5;
    }
}