package de.uol.swp.client.game;

import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.chat.ChatPresenter;
import de.uol.swp.client.game.debug.DebugPresenter;
import de.uol.swp.client.game.event.PlaySoundEvent;
import de.uol.swp.client.game.player.GameProfilePresenter;
import de.uol.swp.client.game.player.PlayerInfo;
import de.uol.swp.client.game.popup.*;
import de.uol.swp.client.game.trade.BankTradePresenter;
import de.uol.swp.client.game.trade.OfferPresenter;
import de.uol.swp.client.game.trade.TradePresenter;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.board.ResourceTile;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.PieceDTO;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.*;
import de.uol.swp.common.game.message.build.ObjectWasPlacedMessage;
import de.uol.swp.common.game.message.inventory.BuildableObjectMessage;
import de.uol.swp.common.game.message.inventory.DevCardCountMessage;
import de.uol.swp.common.game.message.inventory.DevCardDetailedCountMessage;
import de.uol.swp.common.game.message.inventory.ResourceCardCountMessage;
import de.uol.swp.common.game.message.trade.TradeOfferAcceptedMessage;
import de.uol.swp.common.game.request.PlayerFinishedLoadingRequest;
import de.uol.swp.common.game.request.build.StartBuildRequest;
import de.uol.swp.common.game.response.CanNotLeaveGameResponse;
import de.uol.swp.common.game.response.IsUsingCardAllowedResponse;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Main presenter for the game window
 */
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
public class GameSessionPresenter extends AbstractPresenter {

    public static final String FXML = "/fxml/game/GameSessionView.fxml";
    private static final Logger LOG = LogManager.getLogger(GameSessionPresenter.class);
    private final Map<String, PlayerInfo> playerMap = new HashMap<>();
    private final IntegerProperty turnTimerInSeconds = new SimpleIntegerProperty();
    private EventBus localEventBus;
    @FXML
    private BoardPresenter boardViewController;
    @FXML
    private TradePresenter tradeViewController;
    @FXML
    private BankTradePresenter bankTradeViewController;
    @FXML
    private BuyBuildPresenter buyBuildViewController;
    @FXML
    private OfferPresenter offerViewController;
    @FXML
    private DebugPresenter debugViewController;
    @FXML
    private InventoryPresenter inventoryViewController;
    @FXML
    private GameProfilePresenter gameProfileViewController;
    @FXML
    private PlayerPickerPresenter playerPickerViewController;
    @FXML
    private DicePresenter diceViewController;
    @FXML
    private RobberPresenter robberViewController;
    @FXML
    private CardPresenter cardViewController;
    @FXML
    private GameOverPresenter gameOverViewController;
    @FXML
    private DiscardCardsPresenter discardCardsViewController;
    @FXML
    private GameInformationPresenter gameInformationViewController;
    @FXML
    private LeaveGamePresenter leaveGameViewController;

    @FXML
    private Label myTurnLabel;
    @FXML
    private Label rollResultLabel;
    @FXML
    private JFXButton turnEndButton;
    @FXML
    private JFXButton tradeButton;
    @FXML
    private JFXNodesList tradeNodeList;
    @FXML
    private JFXButton endGameButton;
    @FXML
    private AnchorPane root;
    @FXML
    private Pane tradePane;
    @FXML
    private Pane bankTradePane;
    @FXML
    private Pane buyBuildPane;
    @FXML
    private JFXButton rollDiceButton;
    @FXML
    private JFXButton debugButton;
    @FXML
    private Pane debugPane;
    @FXML
    private Pane boardPane;
    @FXML
    private Pane gameOverPane;
    @FXML
    private Pane monopolyPane;
    @FXML
    private Pane yearOfPlentyPane;
    @FXML
    private Pane chatPane;
    @FXML
    private Pane gameInformationPane;
    @FXML
    private Pane discardCardsPane;
    @FXML
    private Pane playerPickerPane;
    @FXML
    private Pane dicePane;
    @FXML
    private Pane robberPane;
    @FXML
    private Pane leaveGamePane;

    @FXML
    private Pane cardPane;
    private GameDTO game;
    @FXML
    private Label turnTimerLabel;
    private Timer turnTimer = new Timer(true);
    private String gameSessionName;
    private String playerNameOnTurn = "";
    private int noOfFreeRoads = 0;
    private String state;

    /**
     * Returns the {@link Color} object that is associated with the given PlayerColor
     *
     * @param color the PlayerColor that should be converted
     * @return the {@link Color} object that is associated with the given PlayerColor
     */
    public static Color getFXColorFromPlayerColor(PlayerColor color) {
        switch (color) {
            case YELLOW:
                return Color.rgb(243, 206, 27);
            case GREEN:
                return Color.rgb(17, 134, 0);
            case BLUE:
                return Color.rgb(0, 142, 255);
            case RED:
                return Color.rgb(197, 0, 0);
            default:
                throw new IllegalArgumentException("PlayerColor " + color + " should be accounted for");
        }
    }

    /**
     * Sets the local EventBus used in this game session
     *
     * @param localEventBus the local EventBus used in this game session
     */
    public void setLocalEventBus(EventBus localEventBus) {
        this.localEventBus = localEventBus;
    }

    /**
     * Returns the name of the player whose turn it currently is
     *
     * @return the name of the player whose turn it currently is
     */
    public String getWhoseTurn() {
        return playerNameOnTurn;
    }

    /**
     * Initializes the order of included panes
     */
    public void initialize() {
        StringConverter<Number> converter = new StringConverter<>() {
            @Override
            public String toString(Number object) {
                return String.format("%02d:%02d", object.intValue() / 60, object.intValue() % 60);
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        };
        Bindings.bindBidirectional(turnTimerLabel.textProperty(), turnTimerInSeconds, converter);
    }

    /**
     * Sets the instance for the game
     *
     * @param game the instance of the game
     */
    public void setGame(GameDTO game, String gameSessionName) {
        String[] playerNames = Arrays.stream(game.getPlayers()).map(PlayerDTO::getPlayerName).toArray(String[]::new);
        for (String playerName : playerNames) {
            playerMap.put(playerName, new PlayerInfo());
        }
        this.gameSessionName = gameSessionName;
        debugViewController.setPlayers(playerNames);
        localEventBus.post(this);
        localEventBus.post(playerMap.get(userInfo.getLoggedInUser().getUsername()));
        localEventBus.post(game.getPlayers());
        buyBuildViewController.setInfo(inventoryViewController);
        gameProfileViewController.initialisePlayers();
        eventBus.post(new PlayerFinishedLoadingRequest(gameSessionName));
        this.game = game;
    }

    /**
     * Shows the used card if the given player if not the currently logged in player
     *
     * @param playerName the name of the player who played the card
     * @param devCard    the card that was played
     */
    public void showUsedCard(String playerName, DevCard devCard) {
        if (!userInfo.getLoggedInUser().getUsername().equals(playerName)) {
            cardViewController.updateInfo(playerName, devCard);
            cardPane.setVisible(true);
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(5));
            pauseTransition.setOnFinished(event -> cardPane.setVisible(false));
            pauseTransition.play();
        }
    }

    /**
     * Sets the correct ChatPresenter
     *
     * @param chatPresenter the ChatPresenter instance that is stored in the LobbyPresenter
     */
    public void setChatLobby(ChatPresenter chatPresenter) {
        Platform.runLater(() -> chatPane.getChildren().add(chatPresenter.getRoot()));
        chatPresenter.setInfo(game);
    }

    /**
     * sets the game results in gameOverController and displays the gameOverPane
     *
     * @param standings the standings to display
     */
    public void endGame(List<PlayerDTO> standings) {
        Platform.runLater(() -> {
                    gameOverViewController.displayStandings(standings);
                    gameOverPane.setVisible(true);
                }
        );
    }

    /**
     * Pops up the Discard Presenter if the player has resources to discard
     *
     * @param amount the amount the player needs to discard
     */
    public void showDiscardCards(int amount) {
        if (amount > 0) {
            eventBus.post(new PlaySoundEvent(gameSessionName, "robberSound.wav"));
            Platform.runLater(() -> {
                discardCardsViewController.setAmountToDiscard(amount);
                discardCardsViewController.checkCondition();
                discardCardsPane.setVisible(true);
            });
        }
    }

    /**
     * Closes the Discard Presenter
     * TODO vlt mit argument welches bei fehlschlag eine Nachricht in den presenter packt
     */
    public void closeDiscardCards(boolean successfullDiscard, String message) {
        Platform.runLater(
                () -> {
                    if (successfullDiscard) {
                        discardCardsPane.setVisible(false);
                    } else {
                        discardCardsViewController.setResponseMessage(message);
                        LOG.debug("Not enough Resources to discard!");

                    }
                });
    }

    /**
     * When a trade is accepted, this method edits the inventory screen and removes the trade
     *
     * @param message the TradeOfferAcceptedMessage
     */
    public void tradeOfferAcceptedMessage(TradeOfferAcceptedMessage message) {
        offerViewController.removeTradeOffer(message.getTradeOffer());
    }

    /**
     * If a user can not leave the game, an alert will be showed, with the reason why they can not leave
     *
     * @param response the CanNotLeaveGameResponse
     */
    public void canNotLeave(CanNotLeaveGameResponse response) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, response.getReason());
            alert.showAndWait();
        });
    }

    /**
     * Method called when the end turn button is pressed
     *
     * @see GameSessionService
     */
    @FXML
    public void onTurnEndButtonPressed() {
        gameSessionService.endTurn(gameSessionName);
    }

    /**
     * Method called when the roll dice button is pressed
     *
     * @see GameSessionService
     */
    @FXML
    public void onRollDiceButtonPressed() {
        gameSessionService.rollDice(gameSessionName);
    }

    /**
     * Restarts the displayed turn timer with the given duration
     *
     * @param roundTimer the duration of the new timer
     */
    public void restartTimer(int roundTimer) {
        turnTimer.cancel();
        turnTimer = new Timer(true);
        turnTimer.scheduleAtFixedRate(new TimerTask() {
            int counter = roundTimer;

            @Override
            public void run() {
                counter--;
                if (counter <= 0) {
                    turnTimer.cancel();
                }
                Platform.runLater(() -> turnTimerInSeconds.set(counter));
            }
        }, 0, 1000);
    }

    /**
     * changes the myTurn value and label depending on, if the loggedInUser has the turn
     * if the loggedInUser has not the turn, display which user has the turn
     * if the game has not started yet, it display that the game is about to start
     */
    public void nextTurn(String name) {
        this.playerNameOnTurn = name;
        if (name == null) {
            Platform.runLater(() ->
                    myTurnLabel.setText(
                            "Das Spiel startet gleich!")
            );
        } else if (userInfo.getLoggedInUser().getUsername().equals(name)) {
            Platform.runLater(() -> myTurnLabel.setText("Sie sind dran!"));
            disableCurrentTurnButtons();
        } else {
            Platform.runLater(() -> myTurnLabel.setText(name + " ist dran!"));
            disableCurrentTurnButtons();
            tradeViewController.close();
            bankTradeViewController.close();
            buyBuildPane.setVisible(true);
        }
    }

    /**
     * Sets the possibility to klick a button depending on the current state of the game
     *
     * @param state
     */
    public void buttonFunctionality(String state) {
        this.state = state;
        Platform.runLater(() -> {
            if (userInfo.getLoggedInUser().getUsername().equals(playerNameOnTurn)) {
                debugButton.setVisible(game.isDebugEnabled());
                endGameButton.setVisible(game.isDebugEnabled());
                switch (state) {
                    case "SetupState":
                    case "EndState":
                    case "RobberPlacingState":
                    case "RobberDiscardState":
                    case "BuildState":
                    case "TradeState":
                        disableCurrentTurnButtons();
                        buyBuildViewController.setMenuDisable(true);
                        break;
                    case "DiceState":
                        eventBus.post(new PlaySoundEvent(gameSessionName, "diceSound.wav"));
                        rollDiceButton.setDisable(false);
                        tradeButton.setDisable(true);
                        tradeNodeList.animateList(false);
                        turnEndButton.setDisable(true);
                        buyBuildViewController.setMenuDisable(true);
                        break;
                    case "PlayState":
                        rollDiceButton.setDisable(true);
                        tradeButton.setDisable(false);
                        turnEndButton.setDisable(false);
                        buyBuildViewController.setMenuDisable(false);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } else {
                disableCurrentTurnButtons();
                buyBuildViewController.setMenuDisable(true);
            }
        });
    }

    private void disableCurrentTurnButtons() {
        rollDiceButton.setDisable(true);
        tradeButton.setDisable(true);
        tradeNodeList.animateList(false);
        turnEndButton.setDisable(true);
    }

    /**
     * Call the drawRobber method in the Board with the new Position given in the Request
     *
     * @param message from server
     */
    public void moveRobber(RobberPositionUpdateMessage message) {
        boardViewController.drawRobber(message.getUpdatedPosition());
    }

    /**
     * Calls the setResourceLabels method of the addressed GameProfileViewPresenter to alter the inventory of
     * the given player
     *
     * @param message the ResourceCardCountMessage that notifies the client about changes in a player's inventory
     * @see ResourceCardCountMessage
     */
    public void setResourceInfo(ResourceCardCountMessage message) {
        gameProfileViewController.setResourceLabels(message);
    }

    /**
     * Calls the setVPLabels method of the addressed GameProfileViewPresenter to alter the victory points
     * the given player
     *
     * @param message the VPUpdateMessage that notifies the client about changes for their victory points
     * @see VPUpdateMessage
     */
    public void updateVPCount(VPUpdateMessage message) {
        gameProfileViewController.setVPLabels(message);
    }

    /**
     * Calls the setDevCardsInfo method of the addressed GameProfileViewPresenter
     *
     * @param message the DevCardCountMessage which notifies the client
     */
    public void setDevCardsInfo(DevCardCountMessage message) {
        gameProfileViewController.setDevCardLabels(message);
    }

    /**
     * Opens the trade window that allows the user to start a new trade
     */
    @FXML
    public void onOpenTradeWindowButtonPressed() {
        tradeNodeList.animateList(false);
        bankTradeViewController.close();
        tradePane.setVisible(true);
    }

    /**
     * Opens the bank trade window that allows the user to start a new bank trade
     */
    @FXML
    public void onOpenBankTradeWindowButtonPressed() {
        tradeNodeList.animateList(false);
        tradeViewController.close();
        bankTradePane.setVisible(true);
    }

    /**
     * Toggles the debug menu in the game window
     */
    @FXML
    private void onToggleDebugMenuButtonPressed() {
        debugPane.setVisible(!debugPane.isVisible());
    }

    /**
     * Toggles the counter offer trade window
     *
     * @param currentTradeOffer the trade offer the player starts a counter offer to
     */
    public void openCounterOfferTradeWindow(TradeOffer currentTradeOffer) {
        onOpenBankTradeWindowButtonPressed();
        tradeViewController.setEdit(true);
        tradeViewController.setCurrentTradeOffer(currentTradeOffer);
    }

    /**
     * TODO remove debug
     *
     * @return
     */
    public DebugPresenter getDebugPresenter() {
        return debugViewController;
    }

    /**
     * gets the game information presenter to show the current state
     *
     * @return
     */
    public GameInformationPresenter getGameInformationPresenter() {
        return gameInformationViewController;
    }

    /**
     * Toggles the monopoly view in the game window
     */
    public void onToggleMonopolyCard() {
        monopolyPane.setVisible(!monopolyPane.isVisible());
        yearOfPlentyPane.setVisible(false);
    }

    /**
     * Toggles the yearOfPlenty view in the game window
     */
    public void onToggleYearOfPlentyCard() {
        yearOfPlentyPane.setVisible(!yearOfPlentyPane.isVisible());
        monopolyPane.setVisible(false);
    }

    /**
     * Toggles the leaveGame view in the game window and enables / disables every button
     */
    @FXML
    public void onToggleLeaveGame() {
        leaveGamePane.setVisible(!leaveGamePane.isVisible());
        if (leaveGamePane.isVisible()) {
            leaveGamePane.toFront();
            rollDiceButton.setDisable(true);
            tradeButton.setDisable(true);
            tradeNodeList.animateList(false);
            turnEndButton.setDisable(true);
            buyBuildViewController.setMenuDisable(true);
        } else {
            leaveGamePane.toBack();
            buttonFunctionality(state);
        }
    }

    /**
     * Takes a map of values for each hex and calls the draw function for each of them
     *
     * @param hexNumbers Map of Integer and Coord
     */
    public void setFieldValues(Multimap<Integer, Coord> hexNumbers) {
        hexNumbers.forEach((k, v) -> boardViewController.drawFieldValue(integerToCoinString(k), v));
    }

    /**
     * Helper function to convert Integer to a coin string in form of
     * "coin_xx"
     *
     * @param integer the coin number
     * @return coin string
     */
    private String integerToCoinString(Integer integer) {
        return "coin_" + String.format("%02d", integer);
    }

    /**
     * Set the Hexagon fill with the Resource given for each Tile in the given Map
     *
     * @param resourceTileMap The ResourceTileMap
     */
    public void setResourceTiles(Map<Coord, ResourceTile> resourceTileMap) {
        resourceTileMap.forEach(boardViewController::drawTileBackground);
    }

    /**
     * Displays a new Piece on the game board
     *
     * @param message the {@code ObjectWasPlacedMessage} object found on the EventBus
     */
    public void displayNewObject(ObjectWasPlacedMessage message) {
        boardViewController.displayNewObject(message.getCoord(), message.getBuildingType(), message.getPlayer().getColor());
        playerMap.get(message.getPlayer().getPlayerName()).setPorts(message.getPlayer().getPorts());
    }

    /**
     * Calls the cardCount method from the inventoryViewController to
     * show the correct number of resources in the inventory
     */
    public void updateInventoryCount(ResourceCardCountMessage message) {
        if (inventoryViewController != null && userInfo.getLoggedInUser().getUsername().equals(message.getPlayer().getPlayerName())) {
            inventoryViewController.updateResourceCount(message);
        }
    }

    /**
     * Calls the devCardCount method from the inventoryViewController to
     * show the correct number of DevCards in the inventory
     */
    public void updateInventoryDevCardCount(DevCardDetailedCountMessage message) {
        if (inventoryViewController != null) {
            inventoryViewController.updateDevCardCount(message);
        }
    }

    /**
     * Returns the {@link OfferPresenter} included in this presenter
     *
     * @return the {@link OfferPresenter} included in this presenter
     */
    public OfferPresenter getOfferViewController() {
        return offerViewController;
    }

    /**
     * Method called when the end game button is pressed
     * <p>
     * !!!Ist nur vorübergehend, bis das Spiel vom Server, wenn jemand gewonnen hat, selbst beendet wird!!!
     *
     * @see GameSessionService
     */
    @FXML
    public void endGame() {
        gameSessionService.endGame(gameSessionName, userInfo.getLoggedInUser());
    }

    /**
     * Put the initial Robber position on the map.
     *
     * @param coord coord of the robber
     */
    public void drawInitialRobber(Coord coord) {
        boardViewController.drawRobber(coord);
    }

    /**
     * Displays all given pieces on the board
     *
     * @param pieces a map containing the position and pieces to be displayed
     */
    public void drawPiecesToBoard(Map<Coord, PieceDTO> pieces) {
        pieces.forEach((coord, piece) -> boardViewController.displayNewObject(coord, piece.getPieceType(), piece.getOwner().getColor()));
    }

    /**
     * Inform the user that their next click in a hexagon will send a RobberPlacingRequest to the server
     *
     * @param message the message containing the user who will do the placing
     */
    public void startRobberPlacing(RobberPlacingMessage message) {
        boardViewController.highlightLegalRobberLocations();
        LOG.debug("You can now place the robber");
    }

    /**
     * Returns the {@link BoardPresenter} included in this presenter
     *
     * @return the {@link BoardPresenter} included in this presenter
     */
    public BoardPresenter getBoardViewController() {
        return boardViewController;
    }

    /**
     * Show the playerPicker in the current Presenter and supply it with a message
     *
     * @param message message containing playerList
     */
    public void showPlayerPicker(PlayerPickerMessage message) {
        playerPickerViewController.setPlayers(message.getPlayersGettingRobbed());
        Platform.runLater(() -> {
            playerPickerViewController.initialisePlayers();
            playerPickerPane.setVisible(true);
        });
    }

    /**
     * This method shows a dicePane with the player who rolled the dice and the value of the dice roll.
     * After 2 seconds the pane will be closed
     * If the turn timer is lower than 3 seconds, the dice view will not be shown
     *
     * @param message The DiceResultMessage with the player name who rolled the dice and the value of the dices
     */
    public void showDiceResult(DiceResultMessage message) {
        if (turnTimerInSeconds.getValue() > 3) {
            Platform.runLater(() -> {
                diceViewController.updateValues(message.getPlayerName(), message.getDiceResult());
                dicePane.setVisible(true);
            });
            PauseTransition wait = new PauseTransition(Duration.seconds(2));
            wait.setOnFinished(event -> dicePane.setVisible(false));
            wait.play();
        }
    }

    /**
     * This method shows a robberPane with the player who robbed another player, the victim of the robber, and the resource that has been robbed
     * After 3 seconds the pane will be closed
     * If the turn timer is lower than 4 seconds, the robber view will not be shown
     *
     * @param message The RobberDoneMessage with the player name who is the robber, the player name of the victim and the resource that has been robbed
     */
    public void showRobResult(RobberDoneMessage message) {
        if (turnTimerInSeconds.getValue() > 4) {
            Platform.runLater(() -> {
                robberViewController.updateValues(message.getRobber(), message.getVictim());
                robberPane.setVisible(true);
            });
            PauseTransition wait = new PauseTransition(Duration.seconds(3));
            wait.setOnFinished(event -> robberPane.setVisible(false));
            wait.play();
        }
    }

    /**
     * Close the playerPicker in the current Presenter.
     */
    public void closePlayerPicker() {
        Platform.runLater(() -> playerPickerPane.setVisible(false));
    }

    public String getGameSessionName() {
        return gameSessionName;
    }

    /**
     * Sets message for buildableObjects to inventoryPresenter only when the update message
     * contains the logged in player
     *
     * @param message message containing the updated Building Inventory
     */
    public void setBuildableObjectsInfo(BuildableObjectMessage message) {
        if (userInfo.getLoggedInUser().getUsername().equals(message.getPlayer().getPlayerName())) {
            buyBuildViewController.setRemainingBuildables(message.getPieceType());
        }
    }

    /**
     * Displays and updates the Player with the larges Army
     *
     * @param message
     */
    public void updateKnight(KnightUpdateMessage message) {
        gameProfileViewController.displayKnight(message.getPlayerWithArmyBonus());

    }

    /**
     * Displays and updates the Player with the longest Road
     *
     * @param playerWithLongestRoad
     */
    public void updateLongestRoad(PlayerDTO playerWithLongestRoad) {
        gameProfileViewController.displayLongestRoad(playerWithLongestRoad);
    }

    /**
     * Updates the amount of development cards remaining in the presenter
     *
     * @param message message containing the amount remaining
     */
    public void updateDevCardsRemaining(DevCardRemainingMessage message) {
        buyBuildViewController.setDevCardRemaining(message.getAmountOfDevCards());
    }

    /**
     * Sets the amount of free roads this player can place
     *
     * @param noOfFreeRoads the amount of free roads this player can place
     */
    public void setFreeRoad(int noOfFreeRoads) {
        this.noOfFreeRoads = noOfFreeRoads;
    }

    /**
     * Sends a StartBuildRequest if this player can still place free roads
     * <p>
     * If update is true reduces the number of free roads by one
     *
     * @param update if {@code true} reduces the number of free roads by one
     */
    public void checkFreeRoad(boolean update) {
        if (update) noOfFreeRoads--;
        if (noOfFreeRoads > 0) {
            eventBus.post(new StartBuildRequest(gameSessionName, PieceType.ROAD));
        }
    }

    /**
     * this method calls the playCard method of the InventoryPresenter
     *
     * @param response the IsUsingCardAllowedResponse sent from the server to (not) allow the use of a devCard
     */
    public void playDevCard(IsUsingCardAllowedResponse response) {
        inventoryViewController.playCard(response);
    }
}