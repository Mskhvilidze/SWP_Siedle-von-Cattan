package de.uol.swp.client.game.mapobject;

import de.uol.swp.client.game.BoardPresenter;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.board.PieceType;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * A class which represents the board corners of a hexagon
 */
public class Corner extends Circle {

    private final BooleanProperty active = new SimpleBooleanProperty();
    private final Circle highlightCircle;
    private final ScaleTransition transition;
    private boolean settlement;

    /**
     * Constructor
     *
     * @param xCoord the horizontal position of the center of the circle in pixels
     * @param yCoord the vertical position of the center of the circle in pixels
     * @param radius the radius of the circle in pixels
     */
    public Corner(double xCoord, double yCoord, double radius) {
        super(xCoord, yCoord, radius);
        setOpacity(0);
        highlightCircle = new Circle(getCenterX(), getCenterY(), BoardPresenter.SETTLEMENT_RADIUS);
        highlightCircle.setFill(Color.GREY);
        highlightCircle.setOpacity(0.8);
        highlightCircle.setVisible(false);
        highlightCircle.setViewOrder(BoardPresenter.CORNER_HIGHLIGHT_Z);
        setViewOrder(BoardPresenter.CORNER_IMAGE_Z);

        transition = new ScaleTransition(Duration.millis(750), highlightCircle);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setAutoReverse(true);
        transition.setFromX(0.9);
        transition.setFromY(0.9);
        transition.setToX(1.1);
        transition.setToY(1.1);
        active.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> highlightCircle.setVisible(newValue)));
    }

    /**
     * Returns whether this corner is active, which means that its highlighted
     * and clicking it should send a place building request
     *
     * @return {@code true} if this corner is active
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Sets whether this corner is active, which means that it will be highlighted
     * and clicking it will send a place building request
     *
     * @param active whether the corner will be active
     */
    public void setActive(boolean active) {
        this.active.set(active);
    }

    /**
     * Returns the highlight {@code Animation} of the robber
     *
     * @return the highlight {@code Animation} of the robber
     */
    public Animation getHighlightAnimation() {
        return transition;
    }

    /**
     * Fills this circle with a texture according to type and playerColor and draws a black background
     *
     * @param type        the piece type that gets drawn
     * @param playerColor the color of the player who owns the piece type
     */
    public void drawCornerPiece(PieceType type, PlayerColor playerColor) {
        if (getOpacity() == 0) {
            setRadius(BoardPresenter.SETTLEMENT_RADIUS);
            setOpacity(1);
            Circle circleBorder = new Circle(getCenterX(), getCenterY(), BoardPresenter.SETTLEMENT_RADIUS + 2, Color.BLACK); // draw a black border
            Pane parent = (Pane) getParent();
            Platform.runLater(() -> {
                parent.getChildren().add(circleBorder);
                circleBorder.setViewOrder(BoardPresenter.CORNER_IMAGE_BACKGROUND_Z);
            });
            settlement = true;
        }
        setFill(BoardPresenter.getTexture(type, playerColor));
    }

    /**
     * Sets the event that should be handled when this corner is clicked. Will be applied to the highlight circle
     *
     * @param value the event that should be handled when this corner is clicked
     */
    public void setClickEvent(EventHandler<? super MouseEvent> value) {
        highlightCircle.setOnMouseClicked(value);
    }

    /**
     * Returns the circle that gets displayed when this corner gets highlighted
     *
     * @return the circle that gets displayed when this corner gets highlighted
     */
    public Circle getHighlightCircle() {
        return highlightCircle;
    }

    /**
     * Returns whether this corner already has a settlement on it. Used to decide if the click should draw a city
     *
     * @return {@code true} if this corner already has a settlement on it
     */
    public boolean hasSettlement() {
        return settlement;
    }
}
