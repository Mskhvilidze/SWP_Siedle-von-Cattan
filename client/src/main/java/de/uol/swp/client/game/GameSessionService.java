package de.uol.swp.client.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceTile;
import de.uol.swp.common.game.debug.StateMessage;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.*;
import de.uol.swp.common.game.message.build.CancelBuildMessage;
import de.uol.swp.common.game.message.build.ObjectWasPlacedMessage;
import de.uol.swp.common.game.message.build.SetupLocationMessage;
import de.uol.swp.common.game.message.inventory.BuildableObjectMessage;
import de.uol.swp.common.game.message.inventory.DevCardCountMessage;
import de.uol.swp.common.game.message.inventory.DevCardDetailedCountMessage;
import de.uol.swp.common.game.message.inventory.ResourceCardCountMessage;
import de.uol.swp.common.game.message.trade.*;
import de.uol.swp.common.game.request.*;
import de.uol.swp.common.game.request.build.PlaceObjectRequest;
import de.uol.swp.common.game.request.trade.*;
import de.uol.swp.common.game.response.*;
import de.uol.swp.common.game.response.build.PlaceObjectResponse;
import de.uol.swp.common.game.response.build.StartBuildResponse;
import de.uol.swp.common.game.response.trade.NotEnoughResourcesResponse;
import de.uol.swp.common.lobby.request.GameEndedRequestLobby;
import de.uol.swp.common.lobby.request.LeaveLobbyRequest;
import de.uol.swp.common.user.User;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages game requests and responses found on the event bus.
 */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class GameSessionService {

    private static final Logger LOG = LogManager.getLogger(GameSessionService.class);
    private final EventBus eventBus;
    private final Map<String, GameSessionPresenter> gameSessions = new HashMap<>();

    /**
     * Constructor
     *
     * @param eventBus the EventBus set in ClientModule
     * @see de.uol.swp.client.di.ClientModule
     */
    @Inject
    public GameSessionService(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    private Optional<GameSessionPresenter> getPresenter(String gameSessionName) {
        return Optional.ofNullable(gameSessions.get(gameSessionName));
    }

    /**
     * Posts a request to end a turn on the EventBus
     *
     * @param gameSessionName the name of the session
     * @see TurnEndRequest
     */
    public void endTurn(String gameSessionName) {
        TurnEndRequest turnEndRequest = new TurnEndRequest(gameSessionName);
        eventBus.post(turnEndRequest);
    }


    /**
     * Posts a request to discard resources on the Eventbus
     *
     * @param gameSessionName   gameSessionName
     * @param resourceToDiscard amount of resources that will be discarded
     */
    public void postDiscardRequest(String gameSessionName, ResourceEnumMap resourceToDiscard) {
        DiscardCardsRequest request = new DiscardCardsRequest(gameSessionName, resourceToDiscard);
        eventBus.post(request);
    }

    /**
     * Posts a request to roll the Dice
     *
     * @param gameSessionName the name of the session
     * @see RollDiceRequest
     */
    public void rollDice(String gameSessionName) {
        RollDiceRequest rollDiceRequest = new RollDiceRequest(gameSessionName);
        eventBus.post(rollDiceRequest);
    }

    /**
     * Adds a given {@code GameSessionPresenter} instance to the {@link #gameSessions} map.
     *
     * @param gameSessionName      the key for the gameSessionPresenter
     * @param gameSessionPresenter the {@code GameSessionPresenter} instance added to the map
     * @see GameSessionPresenter
     */
    public void addGameSession(String gameSessionName, GameSessionPresenter gameSessionPresenter) {
        gameSessions.put(gameSessionName, gameSessionPresenter);
    }

    /**
     * Posts a request to start a trade with the Bank on the EventBus
     *
     * @param lobbyName  the name of the gameSession (lobby)
     * @param tradeOffer amount of offered and requested resources to trade
     * @see StartBankTradeRequest
     * @since 2021-05-24
     */
    public void startBankTrade(String lobbyName, TradeOffer tradeOffer) {
        StartBankTradeRequest startBankTradeRequest = new StartBankTradeRequest(lobbyName, tradeOffer);
        eventBus.post(startBankTradeRequest);
    }

    /**
     * Creates a new ObjectPlacementRequest to place on the eventbus
     *
     * @param gameSessionName the session in which a new object should be placed
     * @param coord           the coord at which a new object should be placed
     * @param type            the type of the object
     */
    public void createObjectPlacementRequest(String gameSessionName, Coord coord, PieceType type) {
        LOG.debug("Creating new ObjectPlacementRequest from Lobby {}. {} wants to place {} at {}", gameSessionName, "TESTNAME", type, coord);

        PlaceObjectRequest placeObjectRequest = new PlaceObjectRequest(gameSessionName, type, coord);
        eventBus.post(placeObjectRequest);
    }

    /**
     * this method is called when a user clicked the "leave game" button
     * the method posts a LeaveGameRequest and LeaveLobbyRequest
     *
     * @param lobbyName the name of the lobby the user wants to leave
     * @see LeaveGameRequest
     * @see LeaveLobbyRequest
     */

    public void leaveGameAndLobby(String lobbyName) {
        eventBus.post(new LeaveGameRequest(lobbyName));
    }

    /**
     * this method is called when a user clicked the "end game" button
     * the method posts a GameEndedRequestGameSession and a GameEndedRequestLobby
     * <p>
     * !!!Ist nur voruebergehend, bis das Spiel vom Server, wenn jemand gewonnen hat, selbst beendet wird!!!
     *
     * @param lobbyName the name of the lobby the user wants to leave
     * @param user      the user that ended the lobby
     * @see LeaveGameRequest
     * @see LeaveLobbyRequest
     */
    public void endGame(String lobbyName, User user) {
        eventBus.post(new GameEndedRequestGameSession(lobbyName));
        eventBus.post(new GameEndedRequestLobby(lobbyName));
    }


    /**
     * This method displays whose turn it currently is in the given game session
     *
     * @param gameSessionName the name of the game session
     * @param userName        the name of the user whose turn it is
     * @param turnTimer       the duration of the turn timer
     */
    public void updateWhoseTurn(String gameSessionName, String userName, int turnTimer) {
        Optional<GameSessionPresenter> gameSessionPresenter = getPresenter(gameSessionName);
        gameSessionPresenter.ifPresent(sessionPresenter -> sessionPresenter.nextTurn(userName));
    }

    /**
     * Sends a playerPickerRequest via Eventbus
     *
     * @param gameSessionName the gamesession
     * @param selectedPlayer  the selected Player
     */
    public void sendPlayerPickerRequest(String gameSessionName, PlayerDTO selectedPlayer) {
        PlayerPickerRequest request = new PlayerPickerRequest(gameSessionName, selectedPlayer);
        eventBus.post(request);

    }

    /**
     * Posts a request to move the robber on the EventBus
     *
     * @param gameSession the current gameSession
     * @param position    the new position for the robber
     */
    public void sendRobberPlacingRequest(String gameSession, Coord position) {
        RobberPlacingRequest request = new RobberPlacingRequest(gameSession, position);
        eventBus.post(request);
    }

    /**
     * Posts a request to start a trade with other players on the EventBus
     *
     * @param lobbyName  the name of the gameSession (lobby)
     * @param tradeOffer amount of offered and requested resources to trade
     * @see StartTradeRequest
     * @since 2021-04-27
     */
    public void startTrade(String lobbyName, TradeOffer tradeOffer) {
        StartTradeRequest startTradeRequest = new StartTradeRequest(lobbyName, tradeOffer);
        eventBus.post(startTradeRequest);
    }

    /**
     * Posts a request to to show interest in a trade on the EventBus
     *
     * @param lobbyName  the name of the gameSession (lobby)
     * @param tradeOffer amount of offered and requested resources to trade
     * @see InterestTradeOfferRequest
     * @since 2021-05-12
     */
    public void interestTradeOffer(String lobbyName, TradeOffer tradeOffer) {
        InterestTradeOfferRequest request = new InterestTradeOfferRequest(lobbyName, tradeOffer);
        eventBus.post(request);
    }

    /**
     * Posts a request to decline a trade as a receiving player
     *
     * @param lobbyName  the name of the gameSession (lobby)
     * @param tradeOffer amount of offered and requested resources to trade
     * @see DeclineTradeOfferRequest
     * @since 2021-05-12
     */
    public void declineTradeOffer(String lobbyName, TradeOffer tradeOffer) {
        DeclineTradeOfferRequest request = new DeclineTradeOfferRequest(lobbyName, tradeOffer);
        eventBus.post(request);
    }

    /**
     * Creates a new request to start a counter offer trade and posts it on the EventBus
     *
     * @param lobbyName     the name of the gameSession (lobby)
     * @param oldTradeOffer the old amount of offered and requested resources to trade
     * @param newTradeOffer the new amount of offered and requested resources to trade
     */
    public void startCounterTradeOffer(String lobbyName, TradeOffer oldTradeOffer, TradeOffer newTradeOffer) {
        StartCounterTradeOffer request = new StartCounterTradeOffer(lobbyName, oldTradeOffer, newTradeOffer);
        eventBus.post(request);
    }

    /**
     * Posts a request to cancel a trade offer as an offering player
     *
     * @param gameSessionName the name of this game session
     * @param tradeOffer      the trade offer that should be canceled
     * @see CancelTradeOfferRequest
     */
    public void cancelTradeOffer(String gameSessionName, TradeOffer tradeOffer) {
        CancelTradeOfferRequest request = new CancelTradeOfferRequest(gameSessionName, tradeOffer);
        eventBus.post(request);
    }

    /**
     * Posts a request to accept a trade on the EventBus
     *
     * @param lobbyName  the name of the gameSession (lobby)
     * @param tradeOffer amount of offered and requested resources to trade
     * @param player     the player who accepts the trade offer
     * @see AcceptTradeOfferRequest
     * @since 2021-05-12
     */
    public void acceptTradeOffer(String lobbyName, TradeOffer tradeOffer, String player) {
        AcceptTradeOfferRequest request = new AcceptTradeOfferRequest(lobbyName, tradeOffer, player);
        eventBus.post(request);
    }

    private Runnable logErrorNoPresenter(String gameSessionName) {
        return () -> LOG.error("Session {} has no Presenter", gameSessionName);
    }


    // -------------------------------------------------------------------------------
    // Subscribe Methods
    // -------------------------------------------------------------------------------

    /**
     * When this message is found on the eventbus a placeObjetRequest was approved. This calls
     * the correct Presenter to draw the object on screen.
     */
    @Subscribe
    public void onObjectWasPlacedMessage(ObjectWasPlacedMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.displayNewObject(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * this method is called when a GameSessionDroppedMessage is found in the eventbus
     * it removes the gameSession from gameSessions
     *
     * @param message the GameSessionDroppedMessage
     */
    @Subscribe
    public void onGameDroppedMessage(GameSessionDroppedMessage message) {
        gameSessions.remove(message.getGameSessionName());
    }

    /**
     * when this message is found on the eventbus the user could leave the gamesession
     * the gameSession will be removed and an LeaveLobbyRequest will be posted
     *
     * @param response the GameSessionLeftResponse
     */
    @Subscribe
    public void onGameSessionLeftResponse(GameSessionLeftResponse response) {
        gameSessions.remove(response.getGameSessionName());
        eventBus.post(new LeaveLobbyRequest(response.getGameSessionName()));

    }

    /**
     * when this response is found on the eventbus the user could not leave the gamesessoin
     * this method will call an alert
     *
     * @param response the CanNotLeaveResponse
     */
    @Subscribe
    public void onCanNotLeaveResponse(CanNotLeaveGameResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.canNotLeave(response), logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * Method subscribes to the RobberPlacingMessage and calls the startRobberPlacing method
     * of the given gameSessionPresenter.
     *
     * @param message the RobberPlacingMessage
     */
    @Subscribe
    public void onRobberPlacingMessage(RobberPlacingMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.startRobberPlacing(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Method subscribes to the RobberPositionUpdate, updtes the position for the robber in the correct
     * BoardPresenter
     *
     * @param message the RobberPositionUpdateMessage
     */
    @Subscribe
    public void onRobberPositionUpdate(RobberPositionUpdateMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.moveRobber(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * When this message is found in the eventbus, pop up The Discard Presenter.
     *
     * @param message the DiscardNotificationMessage found of the EventBus
     */
    @Subscribe
    public void onDiscardNotificationMessage(DiscardNotificationMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.showDiscardCards(message.getAmount()), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * When this message is found on the eventbus the server has evaluated the discardRequest and the client can
     * either close the DiscardPresenter or redo the request.
     *
     * @param response the DiscardCardsResponse found on the EventBus
     */
    @Subscribe
    public void onDiscardCardsResponse(DiscardCardsResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.closeDiscardCards(response.hasSuccessfullyDiscarded(), response.getMessage()),
                logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * When this message is found on the eventbus the timer of this round is over and the DiscardPresenter has to be closed
     *
     * @param response the DiscardCardsMessage found on the EventBus
     */
    @Subscribe
    public void onDiscardCardsMessage(DiscardCardsMessage response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.closeDiscardCards(response.hasSuccessfullyDiscarded(), response.getMessage()),
                logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * Log if the placement request was successful
     *
     * @param response the response
     */
    @Subscribe
    private void onPlaceObjectResponse(PlaceObjectResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> {
            presenter.getBoardViewController().cancelHighlight(response.getBuildingType());
            presenter.checkFreeRoad(true);
        }, logErrorNoPresenter(response.getGameSessionName()));
        if (response.isPlayerAllowedToBuild()) {
            LOG.debug("You can build");
        } else {
            LOG.debug("You are not allowed to build {} at {}, because {}", response.getBuildingType(), response.getCoord(), response.getMessage());
        }
    }


    /**
     * Update the Knight Display with the player supplied in the message.
     *
     * @param message the message
     */
    @Subscribe
    private void onKnightUpdateMessage(KnightUpdateMessage message) {
        Optional<GameSessionPresenter> optional = getPresenter(message.getGameSessionName());
        optional.ifPresentOrElse(presenter -> presenter.updateKnight(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles StartBuildResponse found on Eventbus and calls the associated BoardPresenter
     * to highlight the correct nodes
     *
     * @param response the StartBuildResponse
     */
    @Subscribe
    private void onStartBuildResponse(StartBuildResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> {
            presenter.getBoardViewController().highlightNodes(response.getLegalNodes(), response.getPieceType());
            presenter.getBoardViewController().showCancelBuildButton();
        }, logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * Handles CancelBuildMessage found on Eventbus and calls the associated BoardPresenter
     * to cancel the highlights
     *
     * @param message the CancelBuildMessage
     */
    @Subscribe
    private void onCancelBuildMessage(CancelBuildMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> {
            presenter.getBoardViewController().cancelHighlight(PieceType.ROAD);
            presenter.getBoardViewController().cancelHighlight(PieceType.SETTLEMENT);
        }, logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles RoadBuildingCardResponse found on Eventbus and calls the associated GameSessionPresenter
     * to check the amount of free roads and send a build request
     *
     * @param response the RoadBuildingCardResponse
     */
    @Subscribe
    private void onRoadBuildingCardResponse(RoadBuildingCardResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> {
            presenter.setFreeRoad(response.getNumOfFreeRoads());
            presenter.checkFreeRoad(false);
        }, logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * Handles SetupLocationMessage found on Eventbus and calls the associated BoardPresenter
     * to highlight the correct nodes
     *
     * @param message the SetupLocationMessage
     */
    @Subscribe
    public void onSetupLocationMessage(SetupLocationMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.getBoardViewController().highlightNodes(message.getLegalNodes(), message.getPieceType()),
                logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Sets the current state of the game
     *
     * @param message
     */
    @Subscribe
    private void onStateMessage(StateMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> {
            presenter.getDebugPresenter().setState(message.getState());
            presenter.getGameInformationPresenter().updateInformationPane(message.getState());
            presenter.buttonFunctionality(message.getState());
        }, logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles NextTurnMessage found on Eventbus and calls the associated GameSessionPresenter
     * to update the current turn
     *
     * @param message the NextTurnMessage that is sent to show the beginning of a new timer round
     * @see NextTurnMessage
     */
    @Subscribe
    public void onNextTurnMessage(NextTurnMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.nextTurn(message.getPlayer().getPlayerName()), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles TimerRestartMessage found on Eventbus and calls the associated GameSessionPresenter
     * to update the timer
     *
     * @param message the TimerRestartMessage on Eventbus
     */
    @Subscribe
    public void onTimerRestartMessage(TimerRestartMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.restartTimer(message.getTurnTimer()), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Calls the setResourceInfo method of the addressed GameSessionPresenter to alter the inventory of
     * the given player
     *
     * @param message the ResourceCardCountMessage that notifies the client about changes in a player's inventory
     * @see ResourceCardCountMessage
     */
    @Subscribe
    public void onResourceCardCountMessage(ResourceCardCountMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> {
            presenter.setResourceInfo(message);
            presenter.updateInventoryCount(message);
        }, logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handle the IsUsingDevCardAllowedResponse on the client. This method calls the playDevCard of gameSessionPresenter
     *
     * @param response the IsUsingCardAllowedResponse sent from the server to (not) allow the use of a devCard
     */
    @Subscribe
    public void onIsUsingDevCardAllowedResponse(IsUsingCardAllowedResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.playDevCard(response), logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * Handle a DevCardUpdateMessage on the client. This updates
     * the gamePresenter with the new amount of devcards
     *
     * @param message message containing the amount of dev cards
     */
    @Subscribe
    public void onDevCardRemainingMessage(DevCardRemainingMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.updateDevCardsRemaining(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handle the VPUpdateMessage on the client. This notifies the correct gamesession
     * about a change of the vp of a player.
     *
     * @param message message containing the amount of vp
     */
    @Subscribe
    public void onVPUpdateMessage(VPUpdateMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.updateVPCount(message), logErrorNoPresenter(message.getGameSessionName()));
    }


    /**
     * Handles UpdateGameSessionMessage found on the Eventbus and calls the associated gamePresenter
     * to completely update the gamePresenter
     *
     * @param message the updateGameSessionMessage that is sent, if a player leaves or rejoins a game
     */
    @Subscribe
    public void onUpdateGameSessionMessage(UpdateGameSessionMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.setGame(message.getGame(), message.getGameSessionName()),
                logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Update the correct Presenter with info about the longest road
     *
     * @param message the LongestRoad Message
     */
    @Subscribe
    public void onLongestRoadUpdate(LongestRoadUpdateMessage message) {
        LOG.debug("Player with longest road {}. ", message.getPlayerWithLongestRoad());
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.updateLongestRoad(message.getPlayerWithLongestRoad()),
                logErrorNoPresenter(message.getGameSessionName()));

    }

    /**
     * Handles DevCardDetailedCountMessage found on Eventbus and calls the associated GameSessionPresenter
     * to update the inventory visuals
     *
     * @param message the DevCardDetailedCountMessage on Eventbus
     */
    @Subscribe
    public void onDevCardDetailedCountMessage(DevCardDetailedCountMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.updateInventoryDevCardCount(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles DevCardCountMessage found on Eventbus and calls the associated GameSessionPresenter
     * to update the playerInfo
     *
     * @param message the DevCardCountMessage on Eventbus
     */
    @Subscribe
    public void onDevCardCountMessage(DevCardCountMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.setDevCardsInfo(message), logErrorNoPresenter(message.getGameSessionName()));
    }


    @Subscribe
    public void onNotEnoughResourcesResponse(NotEnoughResourcesResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(presenter -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Sie haben nicht genug Ressourcen dafür.");
            alert.showAndWait();
        }), logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * Handles InitialBoardMessage found on Eventbus and calls the
     * associated GameSessionPresenter to update the board visuals
     *
     * @param message the InitialBoardMessage on Eventbus
     */
    @Subscribe
    public void onInitBoardMessage(InitialBoardMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> {
            presenter.setFieldValues(message.getHexNumbers());
            presenter.setResourceTiles(message.getResourceTileMap());
            presenter.drawInitialRobber(findDesertCoord(message.getResourceTileMap()));
        }, logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles CardUsedMessage found on Eventbus and calls the
     * associated GameSessionPresenter to show the used card
     *
     * @param message the CardUsedMessage on Eventbus
     */
    @Subscribe
    public void onCardUsedMessage(CardUsedMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.showUsedCard(message.getPlayer().getPlayerName(), message.getDevCard()),
                logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles PlayerPickerMessage found on Eventbus and opens the PlayerPickerPresenter
     *
     * @param message message
     */
    @Subscribe
    public void onPlayerPickerMessage(PlayerPickerMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.showPlayerPicker(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    @Subscribe
    public void onRobberDoneMessage(RobberDoneMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.showRobResult(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles DiceResultMessage found on Eventbus and opens the DicePresenter
     *
     * @param message DiceResultMessage
     */
    @Subscribe
    public void onDiceResultMessage(DiceResultMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.showDiceResult(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles PlayerPickerResponse found on Eventbus and opens the PlayerPickerPresenter
     *
     * @param response response
     */
    @Subscribe
    public void onPlayerPickerResponse(PlayerPickerResponse response) {
        Optional<GameSessionPresenter> opt = getPresenter(response.getGameSessionName());
        opt.ifPresentOrElse(GameSessionPresenter::closePlayerPicker, logErrorNoPresenter(response.getGameSessionName()));
    }

    /**
     * Handles RejoinBoardMessage found on Eventbus and calls the
     * associated GameSessionPresenter to update the board visuals
     *
     * @param message the RejoinBoardMessage on Eventbus
     */
    @Subscribe
    public void onRejoinBoardMessage(RejoinBoardMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.drawPiecesToBoard(message.getPlacedPieces()), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * When a new trade is offered this method shows the trade to all players in the gameSession
     * the offer was created in
     *
     * @param message the NewTradeOfferMessage which is send when a new trade is offered.
     */
    @Subscribe
    public void onNewTradeOfferMessage(NewTradeOfferMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.getOfferViewController().displayNewTradeOffer(message),
                logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Handles AllTradesCanceledMessage found on Eventbus and calls the
     * associated OfferPresenter to clear the active trade offers
     *
     * @param message the AllTradesCanceledMessage on Eventbus
     */
    @Subscribe
    public void onAllTradesCanceledMessage(AllTradesCanceledMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.getOfferViewController().clearAllTrades(), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * When a trade is accepted, this method edits the inventory screen and removes the trade
     *
     * @param message the TradeOfferAcceptedMessage which is sent when a trade is accepted.
     */
    @Subscribe
    private void onTradeOfferAcceptedMessage(TradeOfferAcceptedMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.tradeOfferAcceptedMessage(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * When a player is interested in a trade this method shows the player who started the trade
     * that one player interests the offer
     *
     * @param message the TradeOfferInterestMessage which is sent when a player is interested in a trade.
     */
    @Subscribe
    private void onTradeOfferInterestMessage(TradeOfferInterestMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.getOfferViewController().displayTradeOfferInterest(message),
                logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * When a trade is declined this method shows the player who started the trade
     * that one player declined the offer
     *
     * @param message the TradeOfferDeclinedMessage which is sent when a trade is accepted.
     */
    @Subscribe
    private void onTradeOfferDeclinedMessage(TradeOfferDeclinedMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.getOfferViewController().displayTradeOfferDecline(message),
                logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * When a trade is canceled this method cancels the whole trade offer
     *
     * @param message the TradeOfferCanceledMessage which is sent when a trade is canceled.
     */
    @Subscribe
    private void onTradeOfferCanceledMessage(TradeOfferCanceledMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.getOfferViewController().removeTradeOffer(message.getTradeOffer()),
                logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Method called when a buildableObject is placed.
     *
     * @param message the onBuildableObjectMessage
     */
    @Subscribe
    private void onBuildableObjectMessage(BuildableObjectMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.setBuildableObjectsInfo(message), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Method called when a GameOVerMessage is received.
     *
     * @param message the GameOverMessage
     */
    @Subscribe
    private void onGameOverMessage(GameOverMessage message) {
        Optional<GameSessionPresenter> opt = getPresenter(message.getGameSessionName());
        opt.ifPresentOrElse(presenter -> presenter.endGame(message.getStandings()), logErrorNoPresenter(message.getGameSessionName()));
    }

    /**
     * Return the coord of a Desert Tile in a map of Coords and ResourceTiles
     * used to find the initial position of the robber
     *
     * @param map map containing a desert
     * @return the coord of the Desert tile
     */
    private Coord findDesertCoord(Map<Coord, ResourceTile> map) {
        for (Map.Entry<Coord, ResourceTile> entry : map.entrySet()) {
            if (entry.getValue() == ResourceTile.DESERT) {
                return entry.getKey();
            }
        }
        return null;
    }
}