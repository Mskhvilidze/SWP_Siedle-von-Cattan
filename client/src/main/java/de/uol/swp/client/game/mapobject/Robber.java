package de.uol.swp.client.game.mapobject;

import de.uol.swp.client.ImageCache;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Robber shape used to draw on the Board
 */
public class Robber extends StackPane {
    private final Rectangle robberImg;
    private final Circle selectCircle;
    private final ScaleTransition scaleTransition;

    /**
     * Constructs a new StackPane that contains a circle and a rectangle filled with the robber image
     *
     * @param size          the size of the rectangle
     * @param hexagonBounds the bounds of the hexagon that this robber is in
     */
    public Robber(double size, Bounds hexagonBounds) {
        selectCircle = new Circle((size / 2) * 1.375);
        selectCircle.setFill(Color.TRANSPARENT);
        robberImg = new Rectangle(size, size);
        robberImg.setFill(ImageCache.getPattern("objects/robber.png"));
        robberImg.setMouseTransparent(true);
        getChildren().add(selectCircle);
        getChildren().add(robberImg);
        setLayoutX(hexagonBounds.getCenterX() - selectCircle.getRadius());
        setLayoutY(hexagonBounds.getCenterY() - selectCircle.getRadius());

        scaleTransition = new ScaleTransition(Duration.millis(750), this);
        scaleTransition.setCycleCount(Animation.INDEFINITE);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setFromX(0.6);
        scaleTransition.setFromY(0.6);
        scaleTransition.setToX(0.7);
        scaleTransition.setToY(0.7);
    }

    /**
     * Scales this robber down and moves it to the front.
     * The {@link #getHighlightAnimation() Animation} should be started together with all other robber animations
     */
    public void highlight() {
        Platform.runLater(() -> {
            robberImg.setOpacity(0.6);
            toFront();
            selectCircle.setStroke(Color.BLACK);
        });
    }

    /**
     * Scales this robber up and moves it to the back.
     * The {@link #getHighlightAnimation() Animation} should be stopped together with all other robber animations
     */
    public void cancelHighlight() {
        Platform.runLater(() -> {
            setScaleX(1);
            setScaleY(1);
            robberImg.setOpacity(1);
            toBack();
            selectCircle.setStroke(null);
        });
    }

    /**
     * Returns the highlight {@code Animation} of the robber
     *
     * @return the highlight {@code Animation} of the robber
     */
    public Animation getHighlightAnimation() {
        return scaleTransition;
    }

    /**
     * Sets the event that should be handled when this robber is clicked. Will be applied to the circle
     *
     * @param event the event that should be handled when this robber is clicked
     */
    public void setClickEvent(EventHandler<? super MouseEvent> event) {
        selectCircle.setOnMouseClicked(event);
    }
}
