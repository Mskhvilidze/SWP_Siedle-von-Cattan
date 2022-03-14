package de.uol.swp.client.main.tab;

import com.google.common.eventbus.Subscribe;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.AudioCache;
import de.uol.swp.client.game.event.PlaySoundEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.Optional;

/**
 * Stores all currently open tabs
 */
public class MainTabPresenter extends AbstractPresenter {

    @FXML
    private AnchorPane root;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabMain;

    /**
     * Initializes the tab selected and notified properties
     */
    public void initialize() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof SessionTab) {
                ((SessionTab) newValue).setNotified(false);
            }
        });
    }

    /**
     * Sets the content of the main tab
     *
     * @param pane the root pane of the new content
     */
    public void setMainTabContent(Pane pane) {
        tabMain.setContent(pane);
    }

    /**
     * This method first adds a new tab to the tab pane. Afterwards it adds the lobby pane to the tab
     * and calls {@link SessionTab#switchPaneTo(String) switchPaneTo("Lobby")}
     *
     * @param name      the name of the tab
     * @param lobbyPane the pane containing all lobby nodes
     * @return the newly created tab
     */
    public SessionTab addNewLobbyTab(String name, Pane lobbyPane) {
        SessionTab tab = new SessionTab(name);
        tab.setLobbyPane(lobbyPane);
        tab.switchPaneTo(SessionTab.LOBBY_TAB);
        Platform.runLater(() -> {
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        });
        return tab;
    }

    /**
     * This method adds the game pane to the tab with the given name
     *
     * @param name     the name of the tab
     * @param gamePane the pane containing all game nodes
     */
    public void addNewGameTab(String name, Pane gamePane) {
        Optional<Tab> tab = getLobbyTab(name);
        if (tab.isPresent() && tab.get() instanceof SessionTab) {
            ((SessionTab) tab.get()).setGamePane(gamePane);
            ((SessionTab) tab.get()).switchPaneTo("Game");
        }
    }

    /**
     * Handles PlaySoundEvents found on the EventBus
     * <p>
     * If the game session the event is from is currently selected the sound is played
     *
     * @param event the PlaySoundEvent posted by a game session
     */
    @Subscribe
    private void onPlaySoundEvent(PlaySoundEvent event) {
        var opt = getLobbyTab(event.getGameSessionName());
        if (opt.isPresent()) {
            if (opt.get().isSelected()) {
                AudioCache.getAudio(event.getAudioKey()).play();
            }
        }
    }

    private Optional<Tab> getLobbyTab(String name) {
        return tabPane.getTabs().stream().filter(tab -> name.equals(tab.getText())).findFirst();
    }

    /**
     * Closes the tab with the given name
     *
     * @param name the name of the tab
     */
    public void closeLobbyTab(String name) {
        Optional<Tab> tab = getLobbyTab(name);
        if (tab.isPresent() && tab.get() instanceof SessionTab) {
            ((SessionTab) tab.get()).closeTab();
        }
    }
}