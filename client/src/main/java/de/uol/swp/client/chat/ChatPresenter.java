package de.uol.swp.client.chat;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.jfoenix.controls.JFXButton;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.ImageCache;
import de.uol.swp.common.chat.ChatMessage;
import de.uol.swp.common.chat.SystemMessage;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.PlayerDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages the Chat Window
 * by default a created Instance of ChatPresenter uses the global chat, to associate the Chat with
 * a specific lobby, the user and the lobby have to be set after creation.
 *
 * @see de.uol.swp.client.AbstractPresenter
 */
@SuppressWarnings("UnstableApiUsage")
public class ChatPresenter extends AbstractPresenter {

    public static final String FXML = "/fxml/ChatView.fxml";

    private static final Logger LOG = LogManager.getLogger(ChatPresenter.class);
    @Inject
    private ChatService chatService;
    @FXML
    private VBox root;
    private ObservableList<HBox> chatMessages;
    @FXML
    private ListView<HBox> chatMessagesView;
    @FXML
    private TextField chatBox;
    @FXML
    private JFXButton sendButton;
    private String currentLobby;
    private PlayerDTO[] players;

    /**
     * Initializes the chat lobby and message list
     */
    public void initialize() {
        this.currentLobby = "global"; // default lobby
        chatMessages = FXCollections.observableArrayList();
        chatMessagesView.setItems(chatMessages);
    }

    /**
     * Handles new chat messages
     * <p>
     * If a ChatMessage is posted on the EventBus this method posts the message in the chat window.
     *
     * @param message the ChatMessage object seen on the EventBus
     * @see de.uol.swp.common.chat.ChatMessage
     */
    @Subscribe
    private void onChatMessage(ChatMessage message) {

        if (!currentLobby.equals(message.getLobby())) {
            return;
        }

        LOG.info("Lobby: '{}' received Chat Message from: '{}'", currentLobby, message.getUser().getUsername());
        ZonedDateTime nowZoned = message.getTimestamp().withZoneSameInstant(ZoneId.systemDefault());
        String timeStamp = nowZoned.format(DateTimeFormatter.ofPattern("HH:mm"));
        boolean isUsingColor = false;

        HBox hBox = new HBox();
        Label labelTimestamp = new Label(timeStamp + " ");
        Label labelUsername = new Label(message.getUser().getUsername());
        Label labelMessage = new Label(": " + message.getContent());

        Label labelUsernameColor = new Label(message.getUser().getUsername() + " ");
        Rectangle rectangleColor = new Rectangle(14, 18);
        Label labelMessageColor = new Label(" : " + message.getContent());

        if (!"global".equals(currentLobby) && players != null) {
            PlayerColor playerColor = getPlayerColor(message.getUser().getUsername());
            if (playerColor != null) {
                isUsingColor = true;

                rectangleColor.setArcHeight(5);
                rectangleColor.setArcWidth(5);
                rectangleColor.setStroke(Paint.valueOf("BLACK"));
                rectangleColor.setStrokeType(StrokeType.INSIDE);

                switch (playerColor) {
                    case YELLOW:
                        rectangleColor.setStyle("-fx-fill: YELLOW");
                        break;
                    case GREEN:
                        rectangleColor.setStyle("-fx-fill: GREEN");
                        break;
                    case BLUE:
                        rectangleColor.setStyle("-fx-fill: BLUE");
                        break;
                    case RED:
                        rectangleColor.setStyle("-fx-fill: RED");
                        break;
                    default:
                        LOG.error("Player has wrong color.");
                }
            } else {
                LOG.error("Player has no color.");
            }
        }

        hBox.getChildren().add(labelTimestamp);

        if (isUsingColor) {
            hBox.getChildren().add(labelUsernameColor);
            hBox.getChildren().add(rectangleColor);
            hBox.getChildren().add(labelMessageColor);
        } else {
            hBox.getChildren().add(labelUsername);
            hBox.getChildren().add(labelMessage);
        }

        Platform.runLater(() -> chatMessages.add(hBox));
    }

    /**
     * Handles new System chat messages
     * <p>
     * If a SystemMessage is posted on the EventBus this method parses the message, formats all images
     * and posts the message in the chat window
     *
     * @param message the SystemMessage object seen on the EventBus
     */
    @Subscribe
    private void onSystemMessage(SystemMessage message) {
        if (!currentLobby.equals(message.getLobbyName())) {
            return;
        }
        LOG.info("Lobby: '{}' received System Message", currentLobby);
        ZonedDateTime nowZoned = message.getTimestamp().withZoneSameInstant(ZoneId.systemDefault());
        String timeStamp = nowZoned.format(DateTimeFormatter.ofPattern("HH:mm"));
        String chatMessage = timeStamp + " SYSTEM: " + message.getContent();
        HBox hBox = formatResources(chatMessage);
        Platform.runLater(() -> chatMessages.add(hBox));
    }

    private HBox formatResources(String message) {
        HBox hBox = new HBox();
        hBox.setStyle("-fx-background-color: #eab06c; -fx-background-radius: 3");
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setMinHeight(0);
        String[] result = message.split("(?=\\{)|(?<=})");

        for (String s : result) {
            if (s.matches("\\{[^\\[]*}")) {
                String sub = s.substring(1, s.length() - 1);
                String capitalizedSub = sub.substring(0, 1).toUpperCase() + sub.substring(1);
                ImageView imageView = ImageCache.getView("cards/" + capitalizedSub + "Mini.png");
                imageView.fitHeightProperty().bind(hBox.heightProperty());
                imageView.setPreserveRatio(true);
                hBox.getChildren().add(imageView);
            } else {
                Label textLabel = new Label(s);
                hBox.getChildren().add(textLabel);
            }
        }
        return hBox;
    }

    /**
     * Method called when the sendButton is pressed
     *
     * @see de.uol.swp.client.chat.ChatService
     */
    @FXML
    private void onSendMessageButtonPressed() {
        passChatMessageContent();
    }

    /**
     * Method called when the Enter key is pressed in the chatBox
     *
     * @see de.uol.swp.client.chat.ChatService
     */
    @FXML
    public void onEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            passChatMessageContent();
        }
    }

    /**
     * When this is called the content of the chatbox in passed to the chatService to
     * send a message.
     * Besides it checks if the message contains only spaces or is empty.
     */
    private void passChatMessageContent() {
        String content = chatBox.getText();
        if (!content.isBlank()) {
            chatBox.clear();
            String lobby = currentLobby;
            chatService.sendChatMessage(lobby, content);
            LOG.info("{} submitted  a message to lobby: {} , content: {}", userInfo.getLoggedInUser(), lobby, content);
        }
    }

    /**
     * Returns the root node of this presenter. Used to forward the chat in a lobby to its game session
     *
     * @return the root node of this presenter
     */
    public VBox getRoot() {
        return root;
    }

    /**
     * Getter for the currentLobby
     *
     * @return String The current Lobby
     */
    public String getCurrentLobby() {
        return currentLobby;
    }

    /**
     * Setter for the currentLobby
     *
     * @param currentLobby The Lobby this chat is associated with
     */
    public void setCurrentLobby(String currentLobby) {
        this.currentLobby = currentLobby;
    }

    /**
     * Sets info about the game session
     *
     * @param game The GameDTO
     */
    public void setInfo(GameDTO game) {
        players = game.getPlayers();
        this.players = players.clone();
    }

    /**
     * Gets the player color of a certain player
     *
     * @param playerName The username of the player
     * @return The players color
     */
    private PlayerColor getPlayerColor(String playerName) {
        for (PlayerDTO player : players) {
            if (player.getPlayerName().equals(playerName)) {
                return player.getColor();
            }
        }
        return null;
    }
}