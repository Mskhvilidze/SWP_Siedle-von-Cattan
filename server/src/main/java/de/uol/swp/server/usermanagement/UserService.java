package de.uol.swp.server.usermanagement;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.GameOverMessage;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.exception.RegistrationExceptionMessage;
import de.uol.swp.common.user.player.PlayerProfile;
import de.uol.swp.common.user.request.LogoutRequest;
import de.uol.swp.common.user.request.PlayerInfoRequest;
import de.uol.swp.common.user.request.RegisterUserRequest;
import de.uol.swp.common.user.request.StatisticRequest;
import de.uol.swp.common.user.response.PlayerInfoResponseMessage;
import de.uol.swp.common.user.response.RegistrationSuccessfulResponse;
import de.uol.swp.common.user.response.StatisticResponseMessage;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.message.ClientDisconnectedMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Mapping vom event bus calls to user management calls
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.AbstractService
 * @since 2019-08-05
 */
@SuppressWarnings({"UnstableApiUsage", "PMD.AvoidCatchingGenericException"})
@Singleton
public class UserService extends AbstractService {

    private static final Logger LOG = LogManager.getLogger(UserService.class);

    private final UserManagement userManagement;

    /**
     * Constructor
     *
     * @param eventBus       the EventBus used throughout the entire server (injected)
     * @param userManagement object of the UserManagement to use
     * @see de.uol.swp.server.usermanagement.UserManagement
     * @since 2019-08-05
     */
    @Inject
    public UserService(EventBus eventBus, UserManagement userManagement) {
        super(eventBus);
        this.userManagement = userManagement;
    }

    /**
     * Handles RegisterUserRequests found on the EventBus
     * <p>
     * If a RegisterUserRequest is detected on the EventBus, this method is called.
     * It tries to create a new user via the UserManagement. If this succeeds a
     * RegistrationSuccessfulResponse is posted on the EventBus otherwise a RegistrationExceptionMessage
     * gets posted there.
     *
     * @param msg the RegisterUserRequest found on the EventBus
     * @see de.uol.swp.server.usermanagement.UserManagement#createUser(User)
     * @see de.uol.swp.common.user.request.RegisterUserRequest
     * @see de.uol.swp.common.user.response.RegistrationSuccessfulResponse
     * @see de.uol.swp.common.user.exception.RegistrationExceptionMessage
     * @since 2019-09-02
     */
    @Subscribe
    private void onRegisterUserRequest(RegisterUserRequest msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got new registration message with {}", msg.getUser());
        }
        ResponseMessage returnMessage;
        try {
            userManagement.createUser(msg.getUser());
            returnMessage = new RegistrationSuccessfulResponse();
        } catch (Exception e) {
            LOG.error(e);
            returnMessage = new RegistrationExceptionMessage("Cannot create user " + msg.getUser() + " " + e.getMessage());
        }
        Optional<MessageContext> ctx = msg.getMessageContext();
        if (ctx.isPresent()) {
            returnMessage.setMessageContext(ctx.get());
        }
        post(returnMessage);
    }


    /**
     * Handles ClientDisconnectedMessage found on EventBus
     * <p>
     * When the ClientDisconnectedMessage is detected on the eventBus the User is logged out via
     * userManagement.
     *
     * @param msg The ClientDisconnectedMessage on EventBus
     */
    @Subscribe
    private void onClientDisconnectedMessage(ClientDisconnectedMessage msg) {
        LOG.debug("Client disconnected message in UserService");
        Optional<Session> optional = msg.getSession();
        if (optional.isPresent()) {
            Session session = optional.get();
            LogoutRequest logoutRequest = new LogoutRequest();
            logoutRequest.setSession(session);
            post(logoutRequest);
        } else {
            LOG.error("No Session associated with user. Message: {}.", msg);
        }
    }

    /**
     * If the Request "PlayerStatistic" is called,
     * the method delivers a list of all wins and losses from a player
     * to the client on the eventBus
     *
     * @param statistics
     */
    @Subscribe
    public void onStatisticRequest(StatisticRequest statistics) {
        List<PlayerProfile> list = userManagement.getAllPlayerStatistics();
        if (list != null) {
            StatisticResponseMessage response;
            response = new StatisticResponseMessage(list);
            response.initWithMessage(statistics);
            post(response);
        }
    }

    /**
     * When the "PlayerInfoRequest" request is called,
     * the method delivers information about player
     * to the client in the eventBus
     *
     * @param request the PlayerInfoRequest
     * @see PlayerInfoRequest
     */
    @Subscribe
    public void onPlayerInfoRequest(PlayerInfoRequest request) {
        Optional<Session> session = request.getSession();
        if (session.isPresent()) {
            PlayerProfile profile = userManagement.getPlayerInfoData(session.get().getUser());

            if (profile != null) {
                PlayerInfoResponseMessage responseMessage = new PlayerInfoResponseMessage(profile);
                responseMessage.initWithMessage(request);
                post(responseMessage);
            }
        }
    }

    /**
     * Handles GameOverMessage found on the EventBus.
     * <p>
     * If GameOverMessage the EventBus is found, game winners and player losers
     * are stored in the database via userManagement
     *
     * @param message contains the list of players
     * @see GameOverMessage
     */
    @Subscribe
    public void onGameOverMessage(GameOverMessage message) {
        String playerWon;
        List<PlayerDTO> list = message.getStandings();
        if (list != null) {
            playerWon = list.get(0).getPlayerName();
            userManagement.gameWon(playerWon);
            if (list.size() > 1) {
                for (PlayerDTO playerDTO : list) {
                    if (!playerWon.equals(playerDTO.getPlayerName())) {
                        userManagement.gameLoss(playerDTO.getPlayerName());
                    }
                }
            }
        }
    }
}