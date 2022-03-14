package de.uol.swp.client.game.trade;

import de.uol.swp.client.ImageCache;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This control is used to display a resource with a counter at the top that can be either edited by the player or not
 * <p>
 * Used by the {@link TradePresenter} for setting the trade offer resources and
 * the {@link OfferPresenter} for displaying the incoming resource counts
 */
public class ResourceControl extends Pane {
    private static final Logger LOG = LogManager.getLogger(ResourceControl.class);
    private final SimpleIntegerProperty counter = new SimpleIntegerProperty();
    @FXML
    private ImageView imageView;
    @FXML
    private Label label;
    @FXML
    private Rectangle imageShape;

    /**
     * Constructs a resource control with a non default font
     * <p>
     * Can be used in fxml files
     *
     * @param url      the full path of the resource image
     * @param fontSize the size of the counter text
     * @param editable whether the player can increase/decrease the counter by clicking on the image. <b>Default</b> value is {@code true}
     */
    public ResourceControl(@NamedArg("url") String url, @NamedArg("fontSize") int fontSize,
                           @NamedArg(value = "editable", defaultValue = "true") boolean editable) {
        this(url, editable);
        label.setFont(new Font(fontSize));
    }

    /**
     * Constructs a resource control with a default font
     * <p>
     * Can be used in fxml files
     *
     * @param url      the full path of the resource image
     * @param editable whether the player can increase/decrease the counter by clicking on the image. <b>Default</b> value is {@code true}
     */
    public ResourceControl(@NamedArg("url") String url, @NamedArg(value = "editable", defaultValue = "true") boolean editable) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/trade/ResourceControl.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            LOG.error("ResourceControl loading failed at {} because of {}", exception.getMessage(), exception.getCause());
        }
        imageShape.setFill(ImageCache.getPatternFXML(url));
        label.textProperty().bind(counter.asString());
        if (editable) {
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                int count = counter.get();
                if (event.getButton() == MouseButton.PRIMARY) {
                    counter.set(count + 1);
                } else if (event.getButton() == MouseButton.SECONDARY && count > 0) {
                    counter.set(count - 1);
                }
            });
        }
    }

    /**
     * Returns the value of the counter property
     *
     * @return the value of the counter property
     */
    public int getCounter() {
        return counter.get();
    }

    /**
     * Returns the counter property
     *
     * @return the counter property
     */
    public SimpleIntegerProperty counterProperty() {
        return counter;
    }

    /**
     * Sets the value of the counter property
     *
     * @param counter the new value of the counter property
     */
    public void setCounter(int counter) {
        this.counter.set(counter);
    }
}
