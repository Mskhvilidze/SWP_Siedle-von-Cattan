package de.uol.swp.client.credits;

import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.main.event.ReturnToMainMenuViewEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Class that manages the CreditsView
 *
 * @author Farin Heinje
 * @since 2020-12-06
 */

public class CreditsPresenter extends AbstractPresenter {

    public static final String FXML = "/fxml/CreditsView.fxml";

    private static final ReturnToMainMenuViewEvent RETURN_MAIN_VIEW_MESSAGE = new ReturnToMainMenuViewEvent();

    /**
     * Posts an instance of the ShowMainMenuViewEvent to the EventBus the SceneManager is subscribed to.
     *
     * @see ReturnToMainMenuViewEvent
     * @see de.uol.swp.client.SceneManager
     */
    @FXML
    private void onBackButtonPressed(ActionEvent event) {
        eventBus.post(RETURN_MAIN_VIEW_MESSAGE);
    }
}