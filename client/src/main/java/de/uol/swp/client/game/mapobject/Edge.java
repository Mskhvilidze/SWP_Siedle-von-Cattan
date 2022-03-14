package de.uol.swp.client.game.mapobject;

import de.uol.swp.client.game.BoardPresenter;
import de.uol.swp.common.game.board.Coord;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

/**
 * A class which represents the board edges of a hexagon
 */
@SuppressWarnings("PMD.ShortClassName")
public class Edge extends Line {

    private final Coord[] cornerCoords;
    private final Coord edgeCoords;
    private final BooleanProperty active = new SimpleBooleanProperty();
    private final Circle highlightCircle;
    private final ScaleTransition transition;
    private Line roadLine;
    private Line borderLine;

    /**
     * Constructor
     *
     * @param edgeCoords board coordinates of edge
     * @param corners    the corners of the edge
     */
    public Edge(Coord edgeCoords, Corner... corners) {
        super(corners[0].getCenterX(), corners[0].getCenterY(), corners[1].getCenterX(), corners[1].getCenterY());
        this.cornerCoords = Coord.getCornersFromEdge(edgeCoords);
        this.edgeCoords = edgeCoords;

        setOpacity(0);
        setStartX(corners[0].getCenterX());
        setStartY(corners[0].getCenterY());
        setEndX(corners[1].getCenterX());
        setEndY(corners[1].getCenterY());
        Point2D startPoint = new Point2D(getStartX(), getStartY());
        Point2D endPoint = new Point2D(getEndX(), getEndY());
        Point2D midPoint = startPoint.midpoint(endPoint);

        highlightCircle = new Circle(midPoint.getX(), midPoint.getY(), 13);
        highlightCircle.setFill(Color.GREY);
        highlightCircle.setOpacity(0.8);
        highlightCircle.setVisible(false);
        highlightCircle.setViewOrder(BoardPresenter.EDGE_HIGHLIGHT_Z);
        setViewOrder(BoardPresenter.EDGE_Z);

        //This creates a Javafx bug that sometimes prevents the corner settlement image from begin fully drawn
        //Atm just live with it
        transition = new ScaleTransition(Duration.millis(750), highlightCircle);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setAutoReverse(true);
        transition.setFromX(0.9);
        transition.setFromY(0.9);
        transition.setToX(1.1);
        transition.setToY(1.1);

        active.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> highlightCircle.setVisible(newValue))
        );
    }

    /**
     * Returns whether this edge is active, which means that its highlighted
     * and clicking it should send a place building request
     *
     * @return {@code true} if this edge is active
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Sets whether this edge is active, which means that it will be highlighted
     * and clicking it will send a place building request
     *
     * @param active whether the edge will be active
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
     * Draws a road above this edge and colors it
     *
     * @param playerColor the color of the player who owns the road
     */
    public void drawRoad(Color playerColor) {
        if (roadLine == null || borderLine == null) {
            Point2D startPoint = new Point2D(getStartX(), getStartY());
            Point2D endPoint = new Point2D(getEndX(), getEndY());
            Point2D midPoint = startPoint.midpoint(endPoint);
            Point2D quarterStartPoint = midPoint.midpoint(startPoint);
            Point2D quarterEndPoint = midPoint.midpoint(endPoint);

            roadLine = new Line(quarterStartPoint.getX(), quarterStartPoint.getY(), quarterEndPoint.getX(), quarterEndPoint.getY());
            borderLine = new Line(quarterStartPoint.getX(), quarterStartPoint.getY(), quarterEndPoint.getX(), quarterEndPoint.getY());
            roadLine.setStrokeWidth(BoardPresenter.ROAD_WIDTH);
            roadLine.setStroke(playerColor);
            borderLine.setStrokeWidth(BoardPresenter.ROAD_WIDTH + 2);

            Pane parent = (Pane) getParent();
            Platform.runLater(() -> {
                borderLine.setViewOrder(BoardPresenter.EDGE_ROAD_BORDER_Z);
                roadLine.setViewOrder(BoardPresenter.EDGE_ROAD_Z);
                parent.getChildren().add(borderLine);
                parent.getChildren().add(roadLine);
            });
        }
    }

    /**
     * Sets the event that should be handled when this edge is clicked. Will be applied to the highlight circle
     *
     * @param value the event that should be handled when this edge is clicked
     */
    public void setClickEvent(EventHandler<? super MouseEvent> value) {
        highlightCircle.setOnMouseClicked(value);
    }

    /**
     * Returns the circle that gets displayed when this edge gets highlighted
     *
     * @return the circle that gets displayed when this edge gets highlighted
     */
    public Circle getHighlightCircle() {
        return highlightCircle;
    }

    /**
     * Returns the coordinates of the edge
     *
     * @return the coordinates of the edge
     */
    public Coord getEdgeCoords() {
        return edgeCoords;
    }

    /**
     * Return the coordinates of the corner
     *
     * @return the coordinates of the corner
     */
    public Coord[] getCornerCoords() {
        return cornerCoords.clone();
    }
}

