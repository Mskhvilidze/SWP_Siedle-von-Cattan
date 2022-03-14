package de.uol.swp.client.intro;

import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.main.event.ReturnToMainMenuViewEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Manages the Intro
 */
@SuppressWarnings("UnstableApiUsage")
public class IntroPresenter extends AbstractPresenter {

    public static final String FXML = "/fxml/Intro.fxml";

    private static final ReturnToMainMenuViewEvent RETURN_MAIN_VIEW_MESSAGE = new ReturnToMainMenuViewEvent();

    /**
     * Method called when the MainMenu button is pressed.
     * <p>
     * It posts an instance of the ShowMainMenuViewEvent
     * to the EventBus the SceneManager is subscribed to.
     *
     * @see ReturnToMainMenuViewEvent
     * @see de.uol.swp.client.SceneManager
     */
    @FXML
    private void onMenuButtonPressed(ActionEvent event) {
        eventBus.post(RETURN_MAIN_VIEW_MESSAGE);
    }
}
