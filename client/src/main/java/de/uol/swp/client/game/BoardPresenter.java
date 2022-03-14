package de.uol.swp.client.game;

import de.uol.swp.client.ImageCache;
import de.uol.swp.client.game.mapobject.Corner;
import de.uol.swp.client.game.mapobject.Edge;
import de.uol.swp.client.game.mapobject.Hexagon;
import de.uol.swp.client.game.mapobject.Robber;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.game.board.ResourceTile;
import de.uol.swp.common.game.request.build.CancelBuildRequest;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Presenter for the game board. Is included in the {@link GameSessionPresenter}
 */
public class BoardPresenter extends AbstractGamePresenter {

    /*
    Z ordering
     */
    public static final int CORNER_HIGHLIGHT_Z = 0;
    public static final int CORNER_IMAGE_Z = 1;
    public static final int CORNER_IMAGE_BACKGROUND_Z = 2;
    public static final int EDGE_HIGHLIGHT_Z = 3;
    public static final int EDGE_ROAD_Z = 4;
    public static final int EDGE_ROAD_BORDER_Z = 5;
    public static final int EDGE_Z = 6;
    private static final Logger LOG = LogManager.getLogger(BoardPresenter.class);
    /**
     * offset for settlements and roads in pixel
     */
    private static final double BORDER_OFFSET = 2;
    /**
     * Hexagon radius
     */
    private static final double RADIUS = 66;
    /**
     * Hexagon height
     */
    private static final double TILE_HEIGHT = 2 * RADIUS;
    public static final double SETTLEMENT_RADIUS = TILE_HEIGHT / 8;
    public static final double ROAD_WIDTH = TILE_HEIGHT / 15;
    /**
     * Hexagon width
     */
    private static final double TILE_WIDTH = 2 * Math.sqrt(RADIUS * RADIUS * 0.75);
    /**
     * Internal angle of a hexagon
     */
    private static final double THETA = Math.PI / 3;
    private static final int MAP_RADIUS = 2;
    private final Random randomNum = new Random();
    private final Map<Coord, Edge> edgeMap = new HashMap<>();
    private final Map<Coord, Corner> cornerMap = new HashMap<>();
    private final Map<Coord, Hexagon> hexagonMap = new HashMap<>();
    private final Map<Coord, Robber> robberMap = new HashMap<>();
    private final ParallelTransition robberHighlightTransition = new ParallelTransition();
    private final ParallelTransition cornerHighlightTransition = new ParallelTransition();
    private final ParallelTransition edgeHighlightTransition = new ParallelTransition();
    private Coord robberPosition;
    @FXML
    private Pane root;
    @FXML
    private Pane hexPane;
    @FXML
    private Pane cornerEdgePane;
    @FXML
    private ImageView boardImageView;
    @FXML
    private Button cancelBuildButton;

    /**
     * Uses the ImageCache to get an ImagePattern corresponding to the supplied String and Color
     *
     * @param type  The object-String
     * @param color The color
     * @return an ImagePattern used as fill
     */
    public static ImagePattern getTexture(PieceType type, PlayerColor color) {
        switch (type) {
            case CITY:
                return ImageCache.getPattern("objects/city_" + color.name().toLowerCase() + ".png");
            case SETTLEMENT:
                return ImageCache.getPattern("objects/settlement_" + color.name().toLowerCase() + ".png");
            default:
                LOG.error("Piece Type is not supported for this method");
                return null;
        }
    }

    /**
     * Initializes the hexagons, edges and corners.
     * And inserts them into the correct map.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void initialize() {
        //Field from -MAP_RADIUS to MAP_RADIUS: Horizontal from left to right
        for (int x = -MAP_RADIUS; x <= MAP_RADIUS; x++) {
            int rad1 = Math.max(-MAP_RADIUS, -x - MAP_RADIUS);
            int rad2 = Math.min(MAP_RADIUS, -x + MAP_RADIUS);
            //Diagonal from top left to bottom right
            for (int y = rad1; y <= rad2; y++) {
                Hexagon hex = new Hexagon(x, y);
                ObservableList<Double> hexPoints = insertPixelCoordIntoHex(hex);

                Bounds bounds = hex.getBoundsInLocal();
                Robber robber = new Robber(80, bounds);
                robber.setClickEvent(mouseEvent -> hexagonClicked(hex));
                robberMap.put(hex.getCoord(), robber);

                hexagonMap.put(hex.getCoord(), hex);
                //Gets the coordinates of the corners of the hexagon
                Coord[] cornerCoords = hex.getCorners();
                int pointIndex = 0;
                for (Coord coord : cornerCoords) {
                    Corner corner = new Corner(hexPoints.get(pointIndex), hexPoints.get(pointIndex + 1), 3D);
                    corner.setClickEvent(mouseEvent -> cornerClicked(corner, coord));
                    cornerMap.put(coord, corner);
                    pointIndex += 2;

                }
                //Gets the coordinates of the edges of corners
                Coord[] edgeCoords = hex.getEdges();
                for (Coord edgeCoord : edgeCoords) {
                    Coord[] coords = Coord.getCornersFromEdge(edgeCoord);
                    Edge edge = new Edge(edgeCoord, cornerMap.get(coords[0]), cornerMap.get(coords[1]));
                    edge.setClickEvent(mouseEvent -> edgeClicked(edge));
                    edgeMap.put(edgeCoord, edge);
                }
            }
        }
        hexPane.getChildren().addAll(hexagonMap.values());
        cornerEdgePane.getChildren().addAll(edgeMap.values());
        cornerEdgePane.getChildren().addAll(edgeMap.values().stream().map(Edge::getHighlightCircle).collect(Collectors.toSet()));
        cornerEdgePane.getChildren().addAll(cornerMap.values());
        cornerEdgePane.getChildren().addAll(cornerMap.values().stream().map(Corner::getHighlightCircle).collect(Collectors.toSet()));
        hexPane.getChildren().addAll(robberMap.values());
        //Lines and corners moved upwards
        hexPane.getChildren().stream().filter(node -> node.getClass().equals(Robber.class)).collect(Collectors.toList()).forEach(
                Node::toBack);
    }

    private ObservableList<Double> insertPixelCoordIntoHex(Hexagon hex) {
        Coord hexCoord = hex.getCoord();
        //Shift all rows: Above the centre to the left and below the centre to the right
        double xOff = hexCoord.getY() * TILE_WIDTH / 2;
        //Shift all rows: Above the centre downwards and below the centre upwards
        double yOff = -hexCoord.getY() * TILE_HEIGHT / 4;
        //Gets a list with the empty points of the hexagon
        ObservableList<Double> hexPoints = hex.getPoints();
        //Starts at 4, so that the loop begins at the topmost corner
        int[] angleOffset = {
                4, 5, 0, 1, 2, 3
        };
        //Calculates the corner positions with the angle
        for (int i : angleOffset) {
            double pointX = hexPane.getPrefWidth() / 2 + xOff + hexCoord.getX() * TILE_WIDTH + RADIUS * Math.cos((i + 0.5) * THETA);
            hexPoints.add(pointX);
            double pointY = (boardImageView.getFitHeight() / 2 + boardImageView.getTranslateY()) + yOff + hexCoord.getY() * TILE_HEIGHT + RADIUS * Math.sin(
                    (i + 0.5) * THETA);
            hexPoints.add(pointY);
        }
        return hexPoints;
    }

    /**
     * Called when an edge is clicked
     *
     * @param edge the edge that was clicked
     */
    public void edgeClicked(Edge edge) {
        LOG.debug("Edge {} was clicked in Presenter {}", edge.getEdgeCoords(), gameSessionName);
        if (edge.isActive()) {
            gameSessionService.createObjectPlacementRequest(gameSessionName, edge.getEdgeCoords(), PieceType.ROAD);
        }
    }

    /**
     * Called when a corner is clicked
     *
     * @param corner the corner that was clicked
     * @param coord  the coord of the corner
     */
    public void cornerClicked(Corner corner, Coord coord) {
        LOG.debug("Corner {} was clicked in Presenter {}", coord, gameSessionName);
        if (corner.isActive()) {
            if (corner.hasSettlement()) {
                gameSessionService.createObjectPlacementRequest(gameSessionName, coord, PieceType.CITY);
            } else {
                gameSessionService.createObjectPlacementRequest(gameSessionName, coord, PieceType.SETTLEMENT);
            }
        }
    }

    /**
     * Called when a hexagon is clicked
     *
     * @param hex the hexagon that was clicked
     */
    public void hexagonClicked(Hexagon hex) {
        LOG.debug("HEX {} was clicked in Presenter {}", hex.getCoord(), gameSessionName);
        if (hex.isActive()) {
            gameSessionService.sendRobberPlacingRequest(gameSessionName, hex.getCoord());
        }
    }

    /**
     * Draws a coin with the correct texture at the center of the Hexagon bounding box.
     * Example "coin_01" for the coin with a number 1
     *
     * @param value The coin to draw. Format has to be "coin_xx".
     * @param coord The coord at which the coin will be drawn
     */
    protected void drawFieldValue(String value, Coord coord) {

        Hexagon hexagon = hexagonMap.get(coord);
        Bounds bounds = hexagon.getBoundsInLocal();

        Circle border = new Circle(SETTLEMENT_RADIUS + BORDER_OFFSET);
        border.setFill(Color.BLACK);
        border.setCenterX(bounds.getCenterX());
        border.setCenterY(bounds.getCenterY());

        Circle coin = new Circle(SETTLEMENT_RADIUS);
        coin.setFill(ImageCache.getPattern("objects/" + value + ".png"));
        coin.setCenterX(bounds.getCenterX());
        coin.setCenterY(bounds.getCenterY());

        Platform.runLater(() -> {
            hexPane.getChildren().add(border);
            hexPane.getChildren().add(coin);
        });

    }

    /**
     * Highlights the currently legal robber locations
     */
    public void highlightLegalRobberLocations() {
        robberHighlightTransition.getChildren().clear();
        for (Map.Entry<Coord, Robber> entry : robberMap.entrySet()) {
            if (!entry.getKey().equals(robberPosition)) {
                hexagonMap.get(entry.getKey()).setActive(true);
                entry.getValue().highlight();
                robberHighlightTransition.getChildren().add(entry.getValue().getHighlightAnimation());
            }
        }
        robberHighlightTransition.play();
    }

    /**
     * Highlights the given coords for the given piece type
     *
     * @param coords    the coords that should be highlighted
     * @param pieceType the piece type that the coords represent
     */
    public void highlightNodes(Set<Coord> coords, PieceType pieceType) {
        switch (pieceType) {
            case ROAD:
                edgeHighlightTransition.getChildren().clear();
                for (Corner corner : cornerMap.values()) {
                    corner.setActive(false);
                }
                for (Coord coord : coords) {
                    edgeMap.get(coord).setActive(true);
                    edgeHighlightTransition.getChildren().add(edgeMap.get(coord).getHighlightAnimation());
                }
                edgeHighlightTransition.play();
                break;
            case SETTLEMENT:
            case CITY:
                cornerHighlightTransition.getChildren().clear();
                for (Edge edge : edgeMap.values()) {
                    edge.setActive(false);
                }
                for (Coord coord : coords) {
                    cornerMap.get(coord).setActive(true);
                    cornerHighlightTransition.getChildren().add(cornerMap.get(coord).getHighlightAnimation());
                }
                cornerHighlightTransition.play();
        }
    }

    /**
     * Cancels all highlights for the given piece type
     *
     * @param pieceType the piece type that should not be highlighted anymore
     */
    public void cancelHighlight(PieceType pieceType) {
        cancelBuildButton.setVisible(false);
        switch (pieceType) {
            case ROAD:
                edgeHighlightTransition.stop();
                for (Edge edge : edgeMap.values()) {
                    edge.setActive(false);
                }
                break;
            case SETTLEMENT:
            case CITY:
                cornerHighlightTransition.stop();
                for (Corner corner : cornerMap.values()) {
                    corner.setActive(false);
                }
                break;
        }
    }

    /**
     * Method to draw the robber on the board, send to robber on positin to the front,
     * send all other to back
     *
     * @param newPosition
     */
    protected void drawRobber(Coord newPosition) {
        robberPosition = newPosition;
        robberMap.forEach((coord, shape) -> {
            robberHighlightTransition.stop();
            shape.cancelHighlight();
            hexagonMap.get(coord).setActive(false);
            if (coord.equals(newPosition)) {
                Platform.runLater(shape::toFront);
            }
        });
    }

    /**
     * Displays a new Objet according to type on the GamePresenter.
     *
     * @param coord       the Coordinate at which an object should be placed
     * @param type        the building type
     * @param playerColor the Player Color to color the object with
     */
    protected void displayNewObject(Coord coord, PieceType type, PlayerColor playerColor) {
        switch (type) {
            case ROAD:
                edgeMap.get(coord).drawRoad(GameSessionPresenter.getFXColorFromPlayerColor(playerColor));
                break;
            case SETTLEMENT:
            case CITY:
                cornerMap.get(coord).drawCornerPiece(type, playerColor);
                break;
            default:
                LOG.error("Building Type doesnt exist");
        }
    }

    /**
     * Sets the Fill of a single Hexagon found in hexagonMap
     *
     * @param coord        The coordinate, which hex should get a fill
     * @param resourceTile The type of fill
     */
    protected void drawTileBackground(Coord coord, ResourceTile resourceTile) {

        // There are four variations of WOOL, GRAIN and LUMBER, but only three of ORE and BRICK
        String oneToFour = String.valueOf(randomNum.nextInt(4) + 1);
        String oneToThree = String.valueOf(randomNum.nextInt(3) + 1);

        switch (resourceTile) {
            case ORE:
                hexagonMap.get(coord).setFill(ImageCache.getPattern("fields/Erz-" + oneToThree + ".png"));
                break;
            case WOOL:
                hexagonMap.get(coord).setFill(ImageCache.getPattern("fields/Weide-" + oneToFour + ".png"));
                break;
            case BRICK:
                hexagonMap.get(coord).setFill(ImageCache.getPattern("fields/Lehm-" + oneToThree + ".png"));
                break;
            case GRAIN:
                hexagonMap.get(coord).setFill(ImageCache.getPattern("fields/Weizen-" + oneToFour + ".png"));
                break;
            case DESERT:
                hexagonMap.get(coord).setFill(ImageCache.getPattern("fields/Wueste.png"));
                break;
            case LUMBER:
                hexagonMap.get(coord).setFill(ImageCache.getPattern("fields/Wald-" + oneToFour + ".png"));
                break;
            default:
                LOG.error("Invalid Resource Type");
        }

    }

    @FXML
    private void onCancelBuild() {
        eventBus.post(new CancelBuildRequest(gameSessionName));
    }

    /**
     * Sets the cancel build button to visible
     */
    public void showCancelBuildButton() {
        cancelBuildButton.setVisible(true);
    }
}
