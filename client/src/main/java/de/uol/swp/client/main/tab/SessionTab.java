package de.uol.swp.client.main.tab;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.lang.reflect.Field;

/**
 * Custom tab used to easily switch between a lobby and game view.
 * The tab can be closed externally by calling {@link #closeTab()}
 * and notifications can be set by calling {@link #setNotified(boolean)}
 */
@SuppressWarnings({"java:S2259", "java:S1191", "java:S3011"})
public class SessionTab extends Tab {
    public static final String LOBBY_TAB = "Lobby";
    public static final String GAME_TAB = "Game";
    private final StackPane notifyPane;
    private final ScaleTransition st;
    private Pane lobbyPane;
    private Pane gamePane;

    /**
     * Constructor
     *
     * @param text the title of the tab
     */
    public SessionTab(String text) {
        super(text);

        notifyPane = new StackPane();
        Circle circle = new Circle(10, Color.rgb(200, 0, 0, .9));
        circle.setStrokeWidth(2.0);
        circle.setStyle("-fx-background-insets: 0 0 -1 0, 0, 1, 2;");
        circle.setSmooth(true);
        notifyPane.getChildren().add(circle);
        st = new ScaleTransition(Duration.seconds(1), notifyPane);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1);
        st.setToY(1);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);

        setGraphic(notifyPane);
    }

    /**
     * Represents whether this tab currently has unread notifications
     * and displays the associated graphics
     *
     * @param notified {@code true} to enable graphics, {@code false} to disable graphics
     */
    public void setNotified(boolean notified) {
        if (!selectedProperty().get() && notified) {
            notifyPane.setVisible(true);
            st.play();
        } else if (selectedProperty().get()) {
            notifyPane.setVisible(false);
            st.stop();
        }
    }

    /**
     * Switches this tab to either the lobby pane or the game pane.
     * This method expects the pane to be set
     *
     * @param tab {@value LOBBY_TAB} for Lobby or {@value GAME_TAB} for Game
     */
    public void switchPaneTo(String tab) {
        Platform.runLater(() -> {
            if (LOBBY_TAB.equals(tab)) {
                setContent(lobbyPane);
            } else if (GAME_TAB.equals(tab)) {
                setContent(gamePane);
            }
        });
    }

    /**
     * Closes this tab
     */
    public void closeTab() {
        //TODO: Reflection bad
        com.sun.javafx.scene.control.behavior.TabPaneBehavior behavior = getBehavior();
        if (behavior.canCloseTab(this)) {
            Platform.runLater(() -> {
                behavior.closeTab(this);
                setContent(null);
            });
            gamePane = null;
            lobbyPane = null;
        }
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private com.sun.javafx.scene.control.behavior.TabPaneBehavior getBehavior() {
        try {
            Field beh = TabPaneSkin.class.getDeclaredField("behavior");
            beh.setAccessible(true);
            return (com.sun.javafx.scene.control.behavior.TabPaneBehavior) beh.get(getTabPane().getSkin());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            //TODO: Reflection bad
        }
        return null;
    }

    /**
     * Sets the lobby pane stored inside this tab.
     * <p>
     * To actually show the lobby in the tab {@link #switchPaneTo(String)} has to be called with {@link #LOBBY_TAB}
     * <b>after</b> this method
     *
     * @param lobbyPane the {@code Pane} that stores the lobby nodes
     */
    public void setLobbyPane(Pane lobbyPane) {
        this.lobbyPane = lobbyPane;
    }

    /**
     * Sets the game pane stored inside this tab.
     * <p>
     * To actually show the game in the tab {@link #switchPaneTo(String)} has to be called with {@link #GAME_TAB}
     * <b>after</b> this method
     *
     * @param gamePane the {@code Pane} that stores the game nodes
     */
    public void setGamePane(Pane gamePane) {
        this.gamePane = gamePane;
    }
}