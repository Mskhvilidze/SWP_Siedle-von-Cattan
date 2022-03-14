package de.uol.swp.server.lobby;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.chat.ChatMessage;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.lobby.dto.LobbyOptions;
import de.uol.swp.common.lobby.message.*;
import de.uol.swp.common.lobby.request.*;
import de.uol.swp.common.lobby.response.*;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.message.ClientChangedUserInfoMessage;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

/**
 * Handles the lobby requests send by the users
 *
 * @author Marco Grawunder
 * @since 2019-10-08
 */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class LobbyService extends AbstractService {


    private static final Logger LOG = LogManager.getLogger(LobbyService.class);

    private final LobbyManagement lobbyManagement;
    private final AuthenticationService authenticationService;
    private final GameSessionService gameSessionService;

    /**
     * Constructor
     *
     * @param lobbyManagement       the management class for creating, storing and deleting
     *                              lobbies
     * @param authenticationService the user management
     * @param eventBus              the server-wide EventBus
     * @since 2019-10-08
     */
    @Inject
    public LobbyService(LobbyManagement lobbyManagement, AuthenticationService authenticationService, GameSessionService gameSessionService,
                        EventBus eventBus) {
        super(eventBus);
        this.lobbyManagement = lobbyManagement;
        this.authenticationService = authenticationService;
        this.gameSessionService = gameSessionService;
    }

    /**
     * Handles LobbyInformationRequest found on the EventBus
     * <p>
     * If a LobbyInformationRequest is detected on the EventBus, this method is called.
     * It gets the name of the requested lobby, the owner, the set of users inside the lobby,
     * the amount of ready users, the amount of victory points and the timer duration of the requested lobby.
     *
     * @param request the LobbyInformationRequest found on the EventBus
     * @see LobbyInformationResponse
     */
    @Subscribe
    public void onLobbyInformationRequest(LobbyInformationRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        if (lobby.isPresent()) {
            ResponseMessage response = new LobbyInformationResponse(LobbyDTO.create(lobby.get()));
            response.initWithMessage(request);
            post(response);
        }
    }

    /**
     * Handles CreateLobbyRequests found on the EventBus
     * <p>
     * If a CreateLobbyRequest is detected on the EventBus, this method is called.
     * It checks if the name of the lobby is already taken, if so, a
     * LobbyNameAlreadyTakenResponse sends to the client.
     * If so not a new Lobby will be created via the LobbyManagement
     * using the parameters from the request and sends a LobbyCreatedMessage to every connected user
     *
     * @param request the CreateLobbyRequest found on the EventBus
     * @see de.uol.swp.server.lobby.LobbyManagement#createLobby(String, User, boolean)
     * @see LobbyCreatedMessage
     * @since 2019-10-08
     */
    @Subscribe
    public void onCreateLobbyRequest(CreateLobbyRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        ResponseMessage response;
        if (lobby.isPresent() && lobby.get().getName().equalsIgnoreCase(request.getLobbyName()) || "global".equalsIgnoreCase(
                request.getLobbyName())) {
            response = new LobbyNameAlreadyTakenResponse(request.getLobbyName());
            response.initWithMessage(request);
            post(response);
        } else {
            lobbyManagement.createLobby(request.getLobbyName(), request.getOwner(), request.isPrivateLobby());
            lobby = lobbyManagement.getLobby(request.getLobbyName());

            if (lobby.isPresent()) {
                if (!request.isPrivateLobby()) {
                    sendToAll(new LobbyCreatedMessage(request.getLobbyName(), LobbyDTO.create(lobby.get())));
                }
                lobby.get().joinUser(request.getOwner());
                updateSessionLobbyListForUser(request.getOwner(), lobby.get().getName(), false);
                response = new LobbyJoinSuccessfulResponse(LobbyDTO.create(lobby.get()));
                response.initWithMessage(request);
                post(response);
            }
        }
    }

    /**
     * Handles LobbyJoinUserRequests found on the EventBus
     * <p>
     * If a LobbyJoinUserRequest is detected on the EventBus, this method is called.
     * If the Lobby is not present, a LobbyNotFoundResponse will be send
     * If the Game already started and the user did not leave the started game, a GameAlreadyStartedResponse will be send
     * If the User already joined this Lobby, a LobbyAlreadyJoinedResponse will be send
     * If the Lobby already has 4 Users, a LobbyFullResponse will be send
     * Otherwise the Lobby is present it adds a user to a Lobby stored in the LobbyManagement and sends a UserJoinedLobbyMessage
     * to every user in the lobby.
     *
     * @param request the LobbyJoinUserRequest found on the EventBus
     * @see de.uol.swp.common.lobby.Lobby
     * @see de.uol.swp.common.lobby.message.UserJoinedLobbyMessage
     * @since 2019-10-08
     */
    @Subscribe
    public void onJoinLobbyRequest(JoinLobbyRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        ResponseMessage response;
        if (lobby.isEmpty() || session.isEmpty()) {
            response = new LobbyNotFoundResponse();
        } else if(lobby.get().isGameStarted() && !lobby.get().getLeftUsers().contains(session.get().getUser())){
            response = new GameAlreadyStartedResponse();
        } else if (lobby.get().getUsers().contains(session.get().getUser())) {
            response = new LobbyAlreadyJoinedResponse();
        } else if (lobby.get().isFull()) {
            response = new LobbyFullResponse();
        } else {
            sendToAllInLobby(request.getLobbyName(), new UserJoinedLobbyMessage(request.getLobbyName(), UserDTO.create(session.get().getUser())));
            lobby.get().joinUser(session.get().getUser());
            updateSessionLobbyListForUser(session.get().getUser(), request.getLobbyName(), false);
            response = new LobbyJoinSuccessfulResponse(LobbyDTO.create(lobby.get()));
            sendPlayerJoinedLobby(lobby.get().getName(), session.get().getUser().getUsername());
            postLobbyListUpdatedMessage();
        }
        response.initWithMessage(request);
        post(response);
    }

    /**
     * Sends an {@link ChatMessage} to the lobby of the player
     */
    public void sendPlayerJoinedLobby(String gameSessionName, String playerName) {
        gameSessionService.sendLogMessage(gameSessionName, playerName + " ist der Lobby beigetreten.");
    }

    /**
     * Handles LobbyLeaveUserRequests found on the EventBus
     * <p>
     * If a LobbyLeaveUserRequest is detected on the EventBus, this method is called.
     * It removes a user from a Lobby stored in the LobbyManagement.
     * If the last user has left the lobby, it deletes the lobby and sends a LobbyDroppedMessage to every user.
     * If there are still users left in the lobby it instead sends a UserLeftLobbyMessage to them
     *
     * @param request the LobbyJoinUserRequest found on the EventBus
     * @see de.uol.swp.common.lobby.Lobby
     * @see de.uol.swp.common.lobby.message.UserLeftLobbyMessage
     * @since 2019-10-08
     */
    @Subscribe
    public void onLeaveLobbyRequest(LeaveLobbyRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        if (lobby.isPresent() && session.isPresent()) {
            if (lobby.get().getUsers().size() == 1) {
                lobbyManagement.dropLobby(request.getLobbyName());
                sendToAll(new LobbyDroppedMessage(request.getLobbyName(), LobbyDTO.create(lobby.get())));
            } else {
                User user = session.get().getUser();
                lobby.get().leaveUser(user);
                updateSessionLobbyListForUser(user, request.getLobbyName(), false);
                sendToAllInLobby(request.getLobbyName(), new UserLeftLobbyMessage(request.getLobbyName(), UserDTO.create(user)));
                sendPlayerLeftLobby(lobby.get().getName(), user.getUsername());
                postLobbyListUpdatedMessage();
            }
            LobbyLeftSuccessfulResponse response = new LobbyLeftSuccessfulResponse(request.getLobbyName());
            response.initWithMessage(request);
            post(response);
        } else {
            LOG.error("{} does not exist", request.getLobbyName());
        }
    }

    /**
     * Sends an {@link ChatMessage} to the lobby of the player
     */
    public void sendPlayerLeftLobby(String gameSessionName, String playerName) {
        gameSessionService.sendLogMessage(gameSessionName, playerName + " hat die Lobby verlassen.");
    }

    /**
     * Handles ChangedUserInLobby found of the EventBus
     * <p>
     * updated users are added and old ones deleted.
     * If the user to be deleted is the owner, the right is transferred to the updated user
     *
     * @param messageUser the ChangedUserInLobby found of the EventBus
     */
    @Subscribe
    public void addUpdatedUser(ClientChangedUserInfoMessage messageUser) {
        for (int i = 0; i < messageUser.getLobbies().size(); i++) {
            Optional<GameLobby> lobby = lobbyManagement.getLobby(messageUser.getLobbies().get(i));
            if (lobby.isPresent()) {
                if (lobby.get().getOwner().getUsername().equals(messageUser.getOldUsername())) {
                    lobby.get().updateUser(messageUser.getUser(), messageUser.getOldUsername());
                    lobby.get().updateOwner(messageUser.getUser());
                } else {
                    lobby.get().updateUser(messageUser.getUser(), messageUser.getOldUsername());
                }
            }
        }
    }

    /**
     * Handles GameEndedRequestLobby found on the EventBus
     * <p>
     * If a GameEndedRequestLobby is detected on the EventBus, this method is called.
     * It sends a LobbyDroppedMessage to every user
     * It sends a GameEndedMessage to every user in the lobby
     * It deletes the lobby after
     *
     * @param request the LobbyJoinUserRequest found on the EventBus
     * @see de.uol.swp.common.lobby.Lobby
     * @see de.uol.swp.common.lobby.message.LobbyDroppedMessage
     * @see de.uol.swp.common.lobby.message.GameEndedMessage
     * @since 2019-10-08
     */
    @Subscribe
    public void onGameEndedRequestLobby(GameEndedRequestLobby request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby((request.getLobbyName()));
        if (lobby.isPresent() && lobby.get().isDebugEnabled()) {
            sendToAllInLobby(request.getLobbyName(), new GameEndedMessage(request.getLobbyName()));
            sendToAll(new LobbyDroppedMessage(request.getLobbyName(), LobbyDTO.create(lobby.get())));
            lobbyManagement.dropLobby(request.getLobbyName());
        }
    }

    /**
     * Handles UpdateLobbyRequests found on the EventBus
     * <p>
     * It updates the correct lobby option and sends a LobbyUpdatedMessage to all users in the lobby.
     * Should the privacy option be changed it will also call {@link LobbyManagement#toggleLobbyPrivacy toggleLobbyPrivacy}
     *
     * @param request the UpdateLobbyRequest found on the EventBus
     * @param <T>     the type of the option value
     */
    @Subscribe
    private <T extends Serializable> void onUpdateLobbyRequest(UpdateLobbyRequest<T> request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        if (lobby.isPresent() && session.isPresent() && lobby.get().getOwner().equals(session.get().getUser())) {
            if (request.getOption().equals(LobbyOptions.PRIVATE_LOBBY)) {
                lobbyManagement.toggleLobbyPrivacy(lobby.get().getName());
            }
            lobby.get().updateLobby(request.getOption(), request.getNewValue());
            LobbyUpdatedMessage<T> message = new LobbyUpdatedMessage<>(request.getLobbyName(), request.getOption(), request.getNewValue());
            sendToAllInLobby(request.getLobbyName(), message);
            postLobbyListUpdatedMessage();
        }
    }

    /**
     * Handles LobbyKickRequest found on the eventBus
     * <p>
     * If a LobbyKickRequest is found on the EventBus, this method is called.
     * It removes the user who is kicked from the lobby and sends
     * a LobbyKickRequest to every user in the lobby
     *
     * @param request the LobbyKickRequest found on the EventBis
     * @see LobbyKickRequest
     */
    @Subscribe
    public void onKickPlayerRequest(LobbyKickRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> kicked = authenticationService.getSession(request.getKickUser());
        Optional<Session> owner = request.getSession();
        if (lobby.isPresent() && kicked.isPresent() && owner.isPresent() && lobby.get().getOwner().equals(owner.get().getUser())) {
            lobby.get().leaveUser(request.getKickUser());
            sendToAllInLobby(request.getLobbyName(), new UserLeftLobbyMessage(request.getLobbyName(), request.getKickUser()));
            sendPlayerKickedLobby(request.getLobbyName(), request.getKickUser().getUsername());
            KickedFromLobbyMessage message = new KickedFromLobbyMessage(request.getLobbyName());
            message.getReceiver().add(kicked.get());
            post(message);
            postLobbyListUpdatedMessage();
        }
    }

    /**
     * Sends an {@link ChatMessage} to the lobby of the player
     */
    public void sendPlayerKickedLobby(String gameSessionName, String kickedPlayerName) {
        gameSessionService.sendLogMessage(gameSessionName, kickedPlayerName + " wurde aus der Lobby entfernt.");
    }

    /**
     * Handles UpdateLobbyOwnerRequests found on the EventBus
     * <p>
     * If a UpdateLobbyOwnerRequest is found on the EventBus, this method is called.
     * It updates the owner of the correct lobby and sends
     * a LobbyOwnerUpdatedMessage to every user in the lobby.
     *
     * @param request the UpdateLobbyOwnerRequest found on the EventBus
     * @see LobbyOwnerUpdatedMessage
     */
    @Subscribe
    private void onUpdateLobbyAdminPermissionRequest(UpdateLobbyOwnerRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        if (lobby.isPresent() && session.isPresent() && lobby.get().getOwner().equals(session.get().getUser()) && lobby.get().getUsers().contains(
                request.getNewOwner())) {
            lobby.get().updateOwner(request.getNewOwner());
            sendPlayerOwnerChangedLobby(request.getLobbyName(), request.getNewOwner().getUsername());
            LobbyOwnerUpdatedMessage message = new LobbyOwnerUpdatedMessage(request.getLobbyName(), request.getNewOwner());
            sendToAllInLobby(request.getLobbyName(), message);
        }
    }

    /**
     * Sends an {@link ChatMessage} to the lobby of the player
     */
    public void sendPlayerOwnerChangedLobby(String gameSessionName, String adminPlayerName) {
        gameSessionService.sendLogMessage(gameSessionName, adminPlayerName + " ist nun der Admin der Lobby.");
    }

    /**
     * Handles SetUserReadyRequests found on the EventBus
     * <p>
     * If a SetUserReadyRequest is found on the EventBus, this method is called.
     * It updates the ready status of an user in the correct lobby and sends
     * a ReadyUsersMessage to every user in the lobby.
     *
     * @param request the SetUserReadyRequest found on the EventBus
     * @see de.uol.swp.common.lobby.Lobby
     * @see UserReadyMessage
     */
    @Subscribe
    public void onSetUserReadyRequest(SetUserReadyRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        if (lobby.isPresent() && session.isPresent()) {
            lobby.get().setUserReady(session.get().getUser(), request.isUserReady());
            sendToAllInLobby(request.getLobbyName(),
                    new UserReadyMessage(request.getLobbyName(), UserDTO.create(session.get().getUser()), request.isUserReady()));
        }
    }

    private void postLobbyListUpdatedMessage() {
        var message = new LobbyListUpdatedMessage(new HashSet<>(lobbyManagement.getLobbies().values()));
        sendToAll(message);
    }

    /**
     * Handles RetrieveLobbyListRequests found on the EventBus
     * <p>
     * It sends a response with a set of all public lobbies to the requesting client
     *
     * @param request the RetrieveLobbyListRequest found on the EventBus
     */
    @Subscribe
    private void onRetrieveLobbyListRequest(RetrieveLobbyListRequest request) {
        RetrieveLobbyListResponse response = new RetrieveLobbyListResponse(new HashSet<>(lobbyManagement.getLobbies().values()));
        response.initWithMessage(request);
        post(response);
    }

    @Subscribe
    private void onAddBotToLobbyRequest(AddBotToLobbyRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        if (lobby.isPresent() && session.isPresent() && session.get().getUser().equals(lobby.get().getOwner()) && !lobby.get().isFull()) {
            lobby.get().addBot();

            postLobbyListUpdatedMessage();
            sendToAllInLobby(request.getLobbyName(), new AddedBotToLobbyMessage(request.getLobbyName()));
        }
    }

    @Subscribe
    private void onRemoveBotFromLobbyRequest(RemoveBotFromLobbyRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        if (lobby.isPresent() && session.isPresent() && session.get().getUser().equals(lobby.get().getOwner()) && lobby.get().getNoOfBots() > 0) {
            lobby.get().removeBot();

            postLobbyListUpdatedMessage();
            sendToAllInLobby(request.getLobbyName(), new RemovedBotFromLobbyMessage(request.getLobbyName()));
        }
    }

    /**
     * Prepares a given ServerMessage to be send to all players in the lobby and
     * posts it on the EventBus
     *
     * @param lobbyName the name of the lobby the players are in
     * @param message   the message to be send to the users
     * @see de.uol.swp.common.message.ServerMessage
     * @since 2019-10-08
     */
    public void sendToAllInLobby(String lobbyName, ServerMessage message) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(lobbyName);

        if (lobby.isPresent()) {
            message.setReceiver(authenticationService.getSessions(lobby.get().getUsers()));
            post(message);
        } else {
            LOG.error("{} does not exist", lobbyName);
        }
    }

    /**
     * Handles StartGameSessionRequest found on the EventBus
     * <p>
     * If a StartGameSessionRequest is detected on the EventBus, this method is called.
     * It checks is the lobby is present and if the lobby can be started (if amount of players who are ready
     * is the same amount as the lobbySize).
     * If the Lobby can be started, it sets the gameHasStarted value of GameLobby to true
     *
     * @param request the StartGameSessionRequest found on the EventBus
     * @see de.uol.swp.common.lobby.Lobby
     * @see de.uol.swp.common.lobby.message.StartGameSessionMessage
     */
    @Subscribe
    public void onStartGameSessionRequest(StartGameSessionRequest request) {
        Optional<GameLobby> lobby = lobbyManagement.getLobby(request.getLobbyName());
        Optional<Session> session = request.getSession();
        if (lobby.isPresent() && lobby.get().canLobbyBeStarted() && session.isPresent() && session.get().getUser().equals(
                lobby.get().getOwner()) && lobby.get().canLobbyBeStarted()) {
            GameSession gameSession = gameSessionService.createGameSession(lobby.get());
            Player[] players = gameSession.getPlayers();
            PlayerDTO[] playerDTOS = Arrays.stream(players).map(Player::createDTO).toArray(PlayerDTO[]::new);
            lobby.get().setGameHasStarted(true);
            sendToAllInLobby(request.getLobbyName(),
                    new StartGameSessionMessage(request.getLobbyName(),
                            new GameDTO(request.getLobbyName(), lobby.get().isDebugEnabled(), playerDTOS)));
            gameSessionService.sendInitialBoard(request.getLobbyName(), null);
        }
    }


    /**
     * Remove a lobby or add a lobby to the session associated with a user.
     *
     * @param user      the user to which session the lobby is added.
     * @param lobbyname the lobbyname added to the list
     * @param remove    specifies whether the entry should be removed or added.
     */
    private void updateSessionLobbyListForUser(User user, String lobbyname, boolean remove) {
        Optional<Session> optional = authenticationService.getSession(user);
        if (optional.isPresent()) {
            Session session = optional.get();
            if (remove) {
                LOG.debug("Lobby {} removed from user {}.", lobbyname, user.getUsername());
                session.removeLobby(lobbyname);
            } else {
                LOG.debug("Lobby {} added to user {}.", lobbyname, user.getUsername());
                session.addLobby(lobbyname);
            }
        } else {
            LOG.error("No Session associated with user {}.", user.getUsername());
        }
    }

    /**
     * Searches for the lobby with the requested name
     *
     * @param lobbyName the name of the lobby to search for
     * @return the lobby with the requested name if it exists
     */
    public Optional<GameLobby> getLobby(String lobbyName) {
        return lobbyManagement.getLobby(lobbyName);
    }
}
