package de.uol.swp.client.game.trade;

import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.common.game.PlayerColor;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This control is used to represent a player in an incoming trade offer.
 * It can be crossed out or marked for acceptance
 */
public class PlayerSymbolControl extends Pane {
    private static final Logger LOG = LogManager.getLogger(PlayerSymbolControl.class);
    private final SimpleObjectProperty<PlayerColor> playerColor = new SimpleObjectProperty<>(PlayerColor.BLUE);
    private final SimpleBooleanProperty accepted = new SimpleBooleanProperty();
    private final SimpleBooleanProperty declined = new SimpleBooleanProperty();
    @FXML
    private Circle circle;
    @FXML
    private Group acceptedGroup;
    @FXML
    private Group declinedGroup;

    /**
     * Constructor
     */
    public PlayerSymbolControl() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/trade/PlayerSymbolControl.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            LOG.error("PlayerSymbolControl loading failed at {} because of {}", exception.getMessage(), exception.getCause());
        }
        ObjectBinding<Color> bin = Bindings.createObjectBinding(() -> GameSessionPresenter.getFXColorFromPlayerColor(playerColor.get()), playerColor);
        circle.fillProperty().bind(bin);
        acceptedGroup.visibleProperty().bind(accepted);
        declinedGroup.visibleProperty().bind(declined);
    }

    /**
     * Returns whether this player has accepted the trade offer
     *
     * @return whether this player has accepted the trade offer
     */
    public boolean isAccepted() {
        return accepted.get();
    }

    /**
     * Sets whether this player has accepted the trade offer
     *
     * @param accepted whether this player has accepted the trade offer
     */
    public void setAccepted(boolean accepted) {
        this.accepted.set(accepted);
        this.declined.set(!accepted);
    }

    /**
     * Sets whether this player has declined the trade offer
     *
     * @param declined whether this player has declined the trade offer
     */
    public void setDeclined(boolean declined) {
        this.declined.set(declined);
        this.accepted.set(!declined);
    }

    /**
     * Returns the color of the player that is represented by this symbol as {@link PlayerColor}
     *
     * @return the color of the player that is represented by this symbol as {@link PlayerColor}
     */
    public PlayerColor getPlayerColor() {
        return playerColor.get();
    }

    /**
     * Returns the color of the player that is represented by this symbol as {@link Color}
     *
     * @return the color of the player that is represented by this symbol as {@link Color}
     */
    public Color getColor() {
        return (Color) circle.getFill();
    }

    /**
     * Sets the color of this symbol if it hasn't been set before
     *
     * @param playerColor the new player color of the symbol
     */
    public void setColor(PlayerColor playerColor) {
        if (this.playerColor.get() != null) {
            this.playerColor.set(playerColor);
        }
    }
}
