package de.uol.swp.client.lobby;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.client.main.tab.SessionTab;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.request.RejoinGameSessionRequest;
import de.uol.swp.common.game.response.CanNotRejoinGameResponse;
import de.uol.swp.common.lobby.dto.LobbyOption;
import de.uol.swp.common.lobby.message.*;
import de.uol.swp.common.lobby.request.*;
import de.uol.swp.common.lobby.response.LobbyLeftSuccessfulResponse;
import de.uol.swp.common.user.User;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages lobby requests and responses found on the event bus.
 */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class LobbyService {
    private final EventBus eventBus;

    /**
     * Stores all {@code LobbyPresenter} instances by their name.
     * Purpose of this map is to forward all event bus messages
     * to their corresponding lobby.
     */
    private final Map<String, LobbyPresenter> lobbies = new HashMap<>();
    private final Map<String, SessionTab> sessionTabs = new HashMap<>();

    /**
     * Constructor
     *
     * @param eventBus the EventBus set in ClientModule
     * @see de.uol.swp.client.di.ClientModule
     */
    @Inject
    public LobbyService(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    /**
     * Adds a given {@code LobbyPresenter} instance to the {@link #lobbies} map.
     * Only used for unit tests since those cant implement fx tabs
     *
     * @param lobbyName      the key for the lobbyPresenter
     * @param lobbyPresenter the {@code LobbyPresenter} instance added to the map
     * @see LobbyPresenter
     */
    public void addLobby(String lobbyName, LobbyPresenter lobbyPresenter) {
        lobbies.put(lobbyName, lobbyPresenter);
    }

    /**
     * Takes a {@code LobbyContainer} and adds the included {@code LobbyPresenter} and {@code Tab} instances
     * to the {@link #lobbies} and {@link #sessionTabs} maps
     *
     * @param lobbyName      the key for the lobbyPresenter
     * @param lobbyContainer the {@code LobbyContainer} instance of the new lobby
     */
    public void addLobby(String lobbyName, LobbyContainer lobbyContainer) {
        lobbies.put(lobbyName, lobbyContainer.getPresenter());
        sessionTabs.put(lobbyName, lobbyContainer.getSessionTab());
    }

    // -------------------------------------------------------------------------------
    // Subscribe Methods
    // -------------------------------------------------------------------------------

    /**
     * Handles displaying a new lobby notification
     * <p>
     * If an {@code LobbyNotificationMessage} object is detected on the EventBus this method is called.
     * It gets the correct lobby {@code Tab} from the {@link #sessionTabs} map and calls
     * {@link SessionTab#setNotified(boolean) setNotification(boolean)}
     *
     * @param lobbyMessage the LobbyNotificationMessage object detected on the EventBus
     */
    @Subscribe
    public void onLobbyNotificationMessage(LobbyNotificationMessage lobbyMessage) {
        SessionTab tab = sessionTabs.get(lobbyMessage.getLobbyName());
        if (tab != null) {
            tab.setNotified(true);
        }
    }

    /**
     * Handles a user joining this clients lobby
     * <p>
     * If an {@code UserJoinedLobbyMessage} object is detected on the EventBus this method is called.
     * It gets the correct {@code LobbyPresenter} from the {@link #lobbies} map and calls
     * {@link LobbyPresenter#addLobbyUser addLobbyUser(User)}.
     *
     * @param message the UserJoinedLobbyMessage object detected on the EventBus
     * @see LobbyPresenter
     */
    @Subscribe
    private void onUserJoinedLobbyMessage(UserJoinedLobbyMessage message) {
        LobbyPresenter lobbyPresenter = lobbies.get(message.getLobbyName());
        if (lobbyPresenter != null) {
            lobbyPresenter.addLobbyUser(message.getUser());
        }
    }

    @Subscribe
    private void onAddedBotToLobbyMessage(AddedBotToLobbyMessage message) {
        LobbyPresenter lobbyPresenter = lobbies.get(message.getLobbyName());
        if (lobbyPresenter != null) {
            lobbyPresenter.addBot();
        }
    }

    @Subscribe
    private void onRemovedBotToLobbyMessage(RemovedBotFromLobbyMessage message) {
        LobbyPresenter lobbyPresenter = lobbies.get(message.getLobbyName());
        if (lobbyPresenter != null) {
            lobbyPresenter.removeBot();
        }
    }

    /**
     * Handles a user leaving this clients lobby
     * <p>
     * If an {@code UserLeftLobbyMessage} object is detected on the EventBus this method is called.
     * It gets the correct {@code LobbyPresenter} from the {@link #lobbies} map and calls
     * {@link LobbyPresenter#removeLobbyUser(User)  removeLobbyUser(User)}.
     *
     * @param message the UserLeftLobbyMessage object detected on the EventBus
     * @see LobbyPresenter
     */
    @Subscribe
    private void onUserLeftLobbyMessage(UserLeftLobbyMessage message) {
        LobbyPresenter lobbyPresenter = lobbies.get(message.getLobbyName());
        if (lobbyPresenter != null) {
            lobbyPresenter.removeLobbyUser(message.getUser());
        }
    }

    /**
     * Handles the readyStatus of an user
     * <p>
     * If an {@code UserReadyMessage} object is detected on the EventBus this method is called.
     * It gets the correct {@code LobbyPresenter} from the {@link #lobbies} map and calls
     * {@link LobbyPresenter#setUserReady setUserReady(User, boolean)}.
     *
     * @param message the UserReadyMessage object detected on the EventBus
     * @see LobbyPresenter
     */
    @Subscribe
    private void onUserReadyMessage(UserReadyMessage message) {
        LobbyPresenter lobbyPresenter = lobbies.get(message.getLobbyName());
        if (lobbyPresenter != null) {
            lobbyPresenter.setUserReady(message.getUser(), message.isUserReady());
        }
    }

    /**
     * Handles a {@code LobbyUpdatedMessage} found on the EventBus
     * <p>
     * If an {@code LobbyUpdatedMessage} object is detected on the EventBus this method is called.
     * It gets the correct {@code LobbyPresenter} from the {@link #lobbies} map and calls
     * {@link LobbyPresenter#updateLobby updateLobby(LobbyOption, Object)}.
     *
     * @param message the LobbyUpdatedMessage found on the EventBus
     * @param <T>     the type of the option value
     * @see LobbyOption
     */
    @Subscribe
    private <T extends Serializable> void onLobbyUpdatedMessage(LobbyUpdatedMessage<T> message) {
        Optional<LobbyPresenter> lobbyPresenter = Optional.ofNullable(lobbies.get(message.getLobbyName()));
        lobbyPresenter.ifPresent(presenter -> presenter.updateLobby(message.getOption(), message.getNewValue()));
    }

    /**
     * Handles a {@code LobbyOwnerUpdatedMessage} found on the EventBus
     * <p>
     * This method gets the correct {@code LobbyPresenter} from the {@link #lobbies} map and calls
     * {@link LobbyPresenter#updateOwner updateOwner(UserDTO)}.
     *
     * @param message the LobbyOwnerUpdatedMessage found on the EventBus
     */
    @Subscribe
    private void onLobbyOwnerUpdatedMessage(LobbyOwnerUpdatedMessage message) {
        Optional<LobbyPresenter> lobbyPresenter = Optional.ofNullable(lobbies.get(message.getLobbyName()));
        lobbyPresenter.ifPresent(presenter -> presenter.updateOwner(message.getNewOwner()));
    }

    @Subscribe
    private void onCanNotRejoinResponse(CanNotRejoinGameResponse response){
        LobbyPresenter lobbyPresenter = lobbies.get(response.getLobbyName());
        lobbyPresenter.canNotRejoin(response.getReason());
    }

    /**
     * Handles this client leaving a lobby
     * <p>
     * If an {@code LobbyLeftSuccessfulResponse} object is detected on the EventBus this method is called.
     * It tries to remove the correct {@code LobbyPresenter} and {@code SessionTab}
     * from the {@link #lobbies} and {@link #sessionTabs} maps and calls
     * {@link SessionTab#closeTab()} if the {@code SessionTab} was in the map.
     *
     * @param response the LobbyLeftSuccessfulResponse object detected on the EventBus
     * @see LobbyPresenter
     */
    @Subscribe
    public void onLobbyLeftSuccessfulResponse(LobbyLeftSuccessfulResponse response) {
        lobbies.remove(response.getLobbyName());
        SessionTab tab = sessionTabs.remove(response.getLobbyName());
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * Handles when the game has ended
     * <p>
     * If an {@code GameEndedMessage} object is detected on the EventBus this method is called.
     * It tries to remove the correct {@code LobbyPresenter} and {@code SessionTab}
     * from the {@link #lobbies} and {@link #sessionTabs} maps and calls
     * {@link SessionTab#closeTab()} if the {@code SessionTab} was in the map.
     *
     * @param message the GameEndedMessage object detected on the EventBus
     * @see LobbyPresenter
     */
    @Subscribe
    public void onGameEndedMessage(GameEndedMessage message){
        lobbies.remove(message.getLobbyName());
        SessionTab tab = sessionTabs.remove(message.getLobbyName());
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * Handles a {@code LobbyKickMessage} found on the EventBus
     * <p>
     * It tries to remove the correct {@code LobbyPresenter} and {@code SessionTab}
     * from the {@link #lobbies} and {@link #sessionTabs} maps and calls
     * {@link SessionTab#closeTab()} if the {@code SessionTab} was in the map.
     *
     * @param message the KickedFromLobbyMessage found on the EventBus
     */
    @Subscribe
    public void onKickedFromLobbyMessage(KickedFromLobbyMessage message) {
        lobbies.remove(message.getLobbyName());
        SessionTab tab = sessionTabs.remove(message.getLobbyName());
        if (tab != null) {
            tab.closeTab();
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Du wurdest vom Admin rausgeworfen");
            alert.showAndWait();
        });
    }
    // -------------------------------------------------------------------------------
    // Request Methods
    // -------------------------------------------------------------------------------

    /**
     * Posts a request to create a lobby on the EventBus
     *
     * @param name      the name chosen for the new lobby
     * @param user      the user who wants to create the new lobby
     * @param isPrivate boolean to create a private lobby
     * @see CreateLobbyRequest
     * @since 2019-11-20
     */
    public void createNewLobby(String name, UserDTO user, boolean isPrivate) {
        CreateLobbyRequest createLobbyRequest = new CreateLobbyRequest(name, user, isPrivate);
        eventBus.post(createLobbyRequest);
    }

    /**
     * Posts a request to join a specified lobby on the EventBus
     *
     * @param name the name of the lobby the user wants to join
     * @see JoinLobbyRequest
     * @since 2019-11-20
     */
    public void joinLobby(String name) {
        JoinLobbyRequest joinUserRequest = new JoinLobbyRequest(name);
        eventBus.post(joinUserRequest);
    }

    /**
     * Posts a request to leave a specified lobby on the EventBus
     *
     * @param nameLobby the name of the lobby the user wants to leave
     * @see LeaveLobbyRequest
     * @see LeaveLobbyRequest
     */
    public void leaveLobby(String nameLobby) {
        LeaveLobbyRequest leaveLobbyRequest = new LeaveLobbyRequest(nameLobby);
        eventBus.post(leaveLobbyRequest);
    }

    /**
     * Posts a request to update a lobby option on the EventBus
     *
     * @param name   the name of the lobby that is being changed
     * @param option the {@code LobbyOption} for the option that is being changed
     * @param value  the new option value
     * @param <T>    the type of the option value
     * @see UpdateLobbyRequest
     */
    public <T extends Serializable> void updateLobby(String name, LobbyOption<T> option, T value) {
        UpdateLobbyRequest<T> request = new UpdateLobbyRequest<>(name, option, value);
        eventBus.post(request);
    }

    /**
     * Posts a request to mark a user as ready or not, depending on the given boolean ready
     *
     * @param name the name of the lobby the user is in
     * @see SetUserReadyRequest
     */
    public void setUserReady(String name, boolean ready) {
        SetUserReadyRequest setUserReadyRequest = new SetUserReadyRequest(name, ready);
        eventBus.post(setUserReadyRequest);
    }

    /**
     * Posts a request to give owner rights to a user
     *
     * @param lobbyName the name of the lobby the user is in
     * @param user      the user who will get the owner rights
     * @see UpdateLobbyOwnerRequest
     */
    public void makeOwner(String lobbyName, User user) {
        UpdateLobbyOwnerRequest request = new UpdateLobbyOwnerRequest(lobbyName, UserDTO.create(user));
        eventBus.post(request);
    }

    /**
     * Posts a request to kick a user from a lobby
     *
     * @param lobbyName the name of the lobby the user is in
     * @param user      the user who will be kicked
     * @see LobbyKickRequest
     */
    public void kickPlayer(String lobbyName, User user) {
        LobbyKickRequest request = new LobbyKickRequest(lobbyName, UserDTO.create(user));
        eventBus.post(request);
    }

    /**
     * Posts a request to Start a game session.
     *
     * @param lobbyName Lobby name used for GameSession name
     */
    public void startGameSession(String lobbyName) {
        StartGameSessionRequest request = new StartGameSessionRequest(lobbyName);
        eventBus.post(request);
    }

    /**
     * Getter for all LobbyPresenters
     *
     * @return an UnmodifiableMap containing all LobbyPresenters
     */
    public Map<String, LobbyPresenter> getLobbies() {
        return Collections.unmodifiableMap(lobbies);
    }

    /**
     * posts a request to rejoin a game session
     *
     * @param name of the game session the user wants to rejoin
     */
    public void rejoinGameSession(String name) {
        RejoinGameSessionRequest request = new RejoinGameSessionRequest(name);
        eventBus.post(request);
    }

    /**
     * Retrieve the list of all currently public lobbies
     */
    public void retrieveAllLobbies() {
        RetrieveLobbyListRequest request = new RetrieveLobbyListRequest();
        eventBus.post(request);
    }
}
