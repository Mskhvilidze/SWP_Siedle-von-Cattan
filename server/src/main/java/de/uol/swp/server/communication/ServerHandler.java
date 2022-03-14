package de.uol.swp.server.communication;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.request.AbstractGameRequest;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.lobby.request.AbstractLobbyRequest;
import de.uol.swp.common.message.*;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.message.ChangedUserInfoMessage;
import de.uol.swp.common.user.message.UserAccountDropMessage;
import de.uol.swp.common.user.message.UserLoggedInMessage;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.response.ChangeUserInfoSuccessfulResponse;
import de.uol.swp.common.user.response.DropAccountSuccessfulResponse;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * This class handles all client/server communication
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.communication.ServerHandlerDelegate
 * @since 2017-03-17
 */
@SuppressWarnings({"UnstableApiUsage", "PMD.AvoidCatchingGenericException", "java:S1192"})
public class ServerHandler implements ServerHandlerDelegate {

    private static final Logger LOG = LogManager.getLogger(ServerHandler.class);

    /**
     * Clients that are connected
     */
    private final List<MessageContext> connectedClients = new CopyOnWriteArrayList<>();

    /**
     * Clients with logged in sessions
     */
    private final Map<MessageContext, Session> activeSessions = new HashMap<>();

    /**
     * Event bus (injected)
     */
    private final EventBus eventBus;

    private final GameSessionManagement gameSessionManagement;
    private final LobbyManagement lobbyManagement;

    /**
     * Constructor
     *
     * @param eventBus the EventBus used throughout the entire server
     * @see EventBus
     */
    @Inject
    public ServerHandler(EventBus eventBus, GameSessionManagement gameSessionManagement, LobbyManagement lobbyManagement) {
        this.eventBus = eventBus;
        this.gameSessionManagement = gameSessionManagement;
        this.lobbyManagement = lobbyManagement;
        eventBus.register(this);
    }

    /**
     * Helper method that check if a Message has the required authorization
     *
     * @param ctx the MessageContext connected to the message to check
     * @param msg the message to check
     * @throws SecurityException authorization requirement not met
     * @since 2019-11-20
     */
    private void checkIfMessageNeedsAuthorization(MessageContext ctx, RequestMessage msg) {
        if (msg.authorizationNeeded()) {
            Optional<Session> session = getSession(ctx);
            if (session.isEmpty()) {
                throw new SecurityException("Authorization required. Client not logged in!");
            }
            msg.setSession(session.get());
        }
    }

    private void checkIfUserNeedsToBeInLobby(RequestMessage msg) {
        Optional<Session> session = msg.getSession();
        if (session.isPresent()) {
            User user = session.get().getUser();
            if (msg instanceof AbstractGameRequest) {
                Optional<GameSession> gameSession = gameSessionManagement.getGameSession(((AbstractGameRequest) msg).getGameSessionName());
                if (gameSession.isPresent() && (gameSession.get().getPlayer(user.getUsername()) == null && !gameSession.get().hasUserLeftGameSession(
                        user))) {
                    throw new SecurityException("Authorization required. Client not in correct game session");
                }
            } else if (msg instanceof AbstractLobbyRequest) {
                Optional<GameLobby> gameLobby = lobbyManagement.getLobby(((AbstractLobbyRequest) msg).getLobbyName());
                if (gameLobby.isPresent() && !gameLobby.get().getUsers().contains(user)) {
                    throw new SecurityException("Authorization required. Client not in correct lobby");
                }
            }
        }
    }

    /**
     * Handles exceptions on the Server
     * <p>
     * If an ServerExceptionMessage is detected on the EventBus, this method is called.
     * It sends the ServerExceptionMessage to the affiliated client if a client is
     * affiliated.
     *
     * @param msg the ServerExceptionMessage found on the EventBus
     * @since 2019-11-20
     */
    @Subscribe
    private void onServerException(ServerExceptionMessage msg) {
        Optional<MessageContext> ctx = getCtx(msg);
        LOG.error(msg.getException());
        ctx.ifPresent(channelHandlerContext -> sendToClient(channelHandlerContext, new ExceptionMessage(msg.getException().getMessage())));
    }

    /**
     * Handles errors produced by the EventBus
     * <p>
     * If an DeadEvent object is detected on the EventBus, this method is called.
     * It writes "DeadEvent detected " and the error message of the detected DeadEvent
     * object to the log, if the loglevel is set to WARN or higher.
     *
     * @param deadEvent the DeadEvent object found on the EventBus
     * @since 2019-11-20
     */
    @Subscribe
    private void handleEventBusError(DeadEvent deadEvent) {
        LOG.error("DeadEvent detected {}", deadEvent);
    }

    // -------------------------------------------------------------------------------
    // Handling of connected clients
    // -------------------------------------------------------------------------------
    @Override
    public void newClientConnected(MessageContext ctx) {
        LOG.debug("New client {} connected", ctx);
        connectedClients.add(ctx);
    }

    @Override
    public void clientDisconnected(MessageContext ctx) {
        LOG.debug("Client disconnected");
        Session session = this.activeSessions.get(ctx);
        if (session != null) {
            ClientDisconnectedMessage msg = new ClientDisconnectedMessage();
            msg.setSession(session);
            eventBus.post(msg);
            removeSession(ctx);
        }
        connectedClients.remove(ctx);
    }

    @Override
    public void process(RequestMessage msg) {
        LOG.debug("Received new message from client {}", msg);
        Optional<MessageContext> ctx = msg.getMessageContext();
        if (ctx.isPresent()) {
            try {
                checkIfMessageNeedsAuthorization(ctx.get(), msg);
                checkIfUserNeedsToBeInLobby(msg);
                eventBus.post(msg);
            } catch (SecurityException e) {
                LOG.error("ServerException {} {}", e.getClass().getName(), e.getMessage());
                sendToClient(ctx.get(), new ExceptionMessage(e.getMessage()));
            }
        }
    }

    // -------------------------------------------------------------------------------
    // User Management Events (from event bus)
    // -------------------------------------------------------------------------------

    /**
     * Handles ClientAuthorizedMessages found on the EventBus
     * <p>
     * If a ClientAuthorizedMessage is detected on the EventBus, this method is called.
     * It gets the MessageContext and then gives it and a new LoginSuccessfulResponse to
     * sendToClient for sending as well as giving a new UserLoggedInMessage to sendMessage
     * for notifying all connected clients.
     *
     * @param msg The ClientAuthorizedMessage found on the EventBus
     * @see de.uol.swp.server.communication.ServerHandler#sendToClient(MessageContext, ResponseMessage)
     * @see de.uol.swp.server.communication.ServerHandler#sendMessage(ServerMessage)
     * @since 2019-11-20
     */
    @Subscribe
    private void onClientAuthorized(ClientAuthorizedMessage msg) {
        Optional<MessageContext> ctx = getCtx(msg);
        Optional<Session> session = msg.getSession();
        if (ctx.isPresent() && session.isPresent()) {
            putSession(ctx.get(), session.get());
            sendToClient(ctx.get(), new LoginSuccessfulResponse(msg.getUser()));
            sendMessage(new UserLoggedInMessage(msg.getUser().getUsername()));
        } else {
            LOG.warn("No context for {}", msg);
        }
    }

    /**
     * Handles UserLoggedOutMessages found on the EventBus
     * <p>
     * If an UserLoggedOutMessage is detected on the EventBus, this method is called.
     * It gets the MessageContext and then gives the message to sendMessage in order
     * to send it to the connected client.
     *
     * @param msg the UserLoggedOutMessage found on the EventBus
     * @see de.uol.swp.server.communication.ServerHandler#sendMessage(ServerMessage)
     * @since 2019-11-20
     */
    @Subscribe
    private void onUserLoggedOutMessage(UserLoggedOutMessage msg) {
        Optional<MessageContext> ctx = getCtx(msg);
        ctx.ifPresent(this::removeSession);
    }

    /**
     * Handles ClientDroppedMessage found on the EventBus
     * <p>
     * If an ClientDroppedMessage is detected on the EventBus, this method is called.
     * It gets the MessageContext and then gives the message to sendMessage in order
     * to send it to the connected client
     *
     * @param msg the ClientDroppedMessage found on the EventBus
     * @see ClientDroppedMessage
     */
    @Subscribe
    private void onClientDropped(ClientDroppedMessage msg) {
        Optional<MessageContext> ctx = getCtx(msg);
        if (ctx.isPresent()) {
            sendToClient(ctx.get(), new DropAccountSuccessfulResponse(UserDTO.create(msg.getUser())));
            sendMessage(new UserAccountDropMessage(msg.getUser().getUsername()));
            removeSession(ctx.get());
        } else {
            LOG.warn("No context for {}", msg);
        }
    }

    /**
     * Handles ClientChangedUserInfoMessage found on the EventBus
     * <p>
     * If an ClientChangedUserInfoMessage is detected on the EventBus, this method is called.
     * It receives the MessageContext and specifies it and a new ChangeUserInfoSuccessfulResponse.
     * All connected users are notified that a user has been updated
     *
     * @param message ClientChangedUserInfoMessage found on the EventBus
     * @see ClientChangedUserInfoMessage
     * @see ChangedUserInfoMessage
     * @see ChangeUserInfoSuccessfulResponse
     */
    @Subscribe
    public void onUpdatedUser(ClientChangedUserInfoMessage message) {
        Optional<MessageContext> ctx = getCtx(message);
        Optional<Session> session = message.getSession();
        if (ctx.isPresent() && session.isPresent()) {
            putSession(ctx.get(), session.get());
            sendMessage(new ChangedUserInfoMessage(message.getOldUsername(), UserDTO.create(message.getUser())));
            sendToClient(ctx.get(), new ChangeUserInfoSuccessfulResponse(UserDTO.create(message.getUser())));
        } else {
            LOG.warn("No context for {}", message);
        }

    }
    // -------------------------------------------------------------------------------
    // ResponseEvents
    // -------------------------------------------------------------------------------

    /**
     * Handles ResponseMessages found on the EventBus
     * <p>
     * If an ResponseMessage is detected on the EventBus, this method is called.
     * It gets the MessageContext and then gives it and the ResponseMessage to
     * sendToClient for sending.
     *
     * @param msg the ResponseMessage found on the EventBus
     * @see de.uol.swp.server.communication.ServerHandler#sendToClient(MessageContext, ResponseMessage)
     * @since 2019-11-20
     */
    @Subscribe
    private void onResponseMessage(ResponseMessage msg) {
        Optional<MessageContext> ctx = getCtx(msg);
        if (ctx.isPresent()) {
            msg.setSession(null);
            msg.setMessageContext(null);
            LOG.debug("Send to client: {} Message: {}", ctx.get(), msg);
            sendToClient(ctx.get(), msg);
        }
    }

    // -------------------------------------------------------------------------------
    // ServerMessages
    // -------------------------------------------------------------------------------

    /**
     * Handles ServerMessages found on the EventBus
     * <p>
     * If an ServerMessage is detected on the EventBus, this method is called.
     * It sets the Session and MessageContext to null and then gives the message
     * to sendMessage in order to send it to all connected clients.
     *
     * @param msg the ServerMessage found on the EventBus
     * @see de.uol.swp.server.communication.ServerHandler#sendMessage(ServerMessage)
     * @since 2019-11-20
     */
    @Subscribe
    private void onServerMessage(ServerMessage msg) {
        msg.setSession(null);
        msg.setMessageContext(null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Send {} to {}", msg, (msg.getReceiver().isEmpty() || msg.getReceiver() == null ? "all" : msg.getReceiver()));
        }
        sendMessage(msg);
    }

    // -------------------------------------------------------------------------------
    // Session Management (helper methods)
    // -------------------------------------------------------------------------------

    /**
     * Adds a new Session to the activeSessions
     *
     * @param ctx        the MessageContext belonging to the Session
     * @param newSession the Session to add
     * @since 2019-11-20
     */
    private void putSession(MessageContext ctx, Session newSession) {
        activeSessions.put(ctx, newSession);
    }

    /**
     * Removes a Session specified by MessageContext from the activeSessions
     *
     * @param ctx the MessageContext
     * @since 2019-11-20
     */
    private void removeSession(MessageContext ctx) {
        activeSessions.remove(ctx);
    }

    /**
     * Gets the Session for a given MessageContext
     *
     * @param ctx the MeesageContext
     * @return an Optional containing the Session if found
     * @see de.uol.swp.common.user.Session
     * @see de.uol.swp.common.message.MessageContext
     * @since 2019-11-20
     */
    private Optional<Session> getSession(MessageContext ctx) {
        Session session = activeSessions.get(ctx);
        return session != null ? Optional.of(session) : Optional.empty();
    }

    /**
     * Gets MessageContext from Message
     *
     * @param message the Message to get the MessageContext from
     * @return an Optional containing the MessageContext if there is any
     * @see de.uol.swp.common.message.Message
     * @see de.uol.swp.common.message.MessageContext
     * @since 2019-11-20
     */
    private Optional<MessageContext> getCtx(Message message) {
        if (message.getMessageContext().isPresent()) {
            return message.getMessageContext();
        }
        Optional<Session> session = message.getSession();
        if (session.isPresent()) {
            return getCtx(session.get());
        }
        return Optional.empty();
    }

    /**
     * Gets MessageContext for specified receiver
     *
     * @param session the Session of the user to search
     * @return an Optional containing MessageContext if there is one
     * @see de.uol.swp.common.user.Session
     * @see de.uol.swp.common.message.MessageContext
     * @since 2019-11-20
     */
    private Optional<MessageContext> getCtx(Session session) {
        for (Map.Entry<MessageContext, Session> e : activeSessions.entrySet()) {
            if (e.getValue().equals(session)) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * Gets MessageContexts for specified receivers
     *
     * @param receiver a list containing the sessions of the users to search
     * @return a List of MessageContexts for the given sessions
     * @see de.uol.swp.common.user.Session
     * @see de.uol.swp.common.message.MessageContext
     * @since 2019-11-20
     */
    private List<MessageContext> getCtx(List<Session> receiver) {
        List<MessageContext> ctxs = new ArrayList<>();
        receiver.forEach(r -> {
            Optional<MessageContext> context = getCtx(r);
            context.ifPresent(ctxs::add);
        });
        return ctxs;
    }


    // -------------------------------------------------------------------------------
    // Help methods: Send only objects of type Message
    // -------------------------------------------------------------------------------

    /**
     * Sends a ResponseMessage to a client specified by a MessageContext
     *
     * @param ctx     the MessageContext containing the specified client
     * @param message the Message to send
     * @see de.uol.swp.common.message.ResponseMessage
     * @see de.uol.swp.common.message.MessageContext
     * @since 2019-11-20
     */
    private void sendToClient(MessageContext ctx, ResponseMessage message) {
        LOG.trace("Trying to sendMessage to client: {} {}", ctx, message);
        ctx.writeAndFlush(message);
    }

    /**
     * Sends a ServerMessage to either a specified receiver or all connected clients
     *
     * @param msg the ServerMessage to send
     * @see de.uol.swp.common.message.ServerMessage
     * @since 2019-11-20
     */
    private void sendMessage(ServerMessage msg) {
        if (msg.getReceiver() == null || msg.getReceiver().isEmpty()) {
            if (msg.authorizationNeeded()) {
                sendToMany(connectedClients.stream().filter(activeSessions::containsKey).collect(Collectors.toList()), msg);
            } else {
                sendToMany(connectedClients, msg);
            }
        } else {
            sendToMany(getCtx(msg.getReceiver()), msg);
        }
    }

    /**
     * Sends a ServerMessage to multiple users specified by a list of MessageContexts
     *
     * @param sendTo the List of MessageContexts to send the message to
     * @param msg    message to send
     * @see de.uol.swp.common.message.MessageContext
     * @see de.uol.swp.common.message.ServerMessage
     * @since 2019-11-20
     */
    private void sendToMany(List<MessageContext> sendTo, ServerMessage msg) {
        for (MessageContext client : sendTo) {
            try {
                client.writeAndFlush(msg);
            } catch (Exception e) {
                LOG.error(e.getStackTrace());
            }
        }
    }


}
