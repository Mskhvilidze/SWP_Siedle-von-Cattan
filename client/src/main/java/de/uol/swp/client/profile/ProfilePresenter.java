package de.uol.swp.client.profile;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.base.IFXLabelFloatControl;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.auth.events.ShowLoginViewEvent;
import de.uol.swp.client.main.event.ReturnToMainMenuViewEvent;
import de.uol.swp.client.profile.event.ConfirmPasswordErrorEvent;
import de.uol.swp.client.profile.validator.PasswordValidator;
import de.uol.swp.client.profile.validator.ValidatorHelper;
import de.uol.swp.client.profile.validator.ValueValidator;
import de.uol.swp.common.user.player.PlayerProfile;
import de.uol.swp.common.user.response.*;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Manages the profile window
 *
 * @see de.uol.swp.client.AbstractPresenter
 */
@SuppressWarnings("UnstableApiUsage")
public class ProfilePresenter extends AbstractPresenter {

    public static final String FXML = "/fxml/ProfileView.fxml";
    private static final Logger LOG = LogManager.getLogger(ProfilePresenter.class);

    public static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9]{4,16}");
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("[a-zA-Z0-9]{4,16}");
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,6}");
    private static final ReturnToMainMenuViewEvent RETURN_MAIN_VIEW_MESSAGE = new ReturnToMainMenuViewEvent();
    private static final ShowLoginViewEvent SHOW_LOGIN_VIEW_EVENT = new ShowLoginViewEvent();

    /**
     * All the boolean properties being used to disable confirm buttons.
     * Only real purpose of this list is to ensure that the properties dont get collected by gc.
     */
    private static final List<SimpleBooleanProperty> BOOLEAN_PROPERTIES = new ArrayList<>();

    private ValueValidator usernameValueValidator;
    private ValueValidator emailValueValidator;

    @FXML
    private JFXPasswordField validatePasswordField;
    @FXML
    private AnchorPane root;
    @FXML
    private JFXTextField newUsernameField;
    @FXML
    private JFXTextField newEmailField;
    @FXML
    private JFXPasswordField currentPasswordField;
    @FXML
    private JFXPasswordField newPasswordField;
    @FXML
    private JFXPasswordField confirmPasswordField;
    @FXML
    private Pane panePlayerInfo;
    @FXML
    private Pane usernamePane;
    @FXML
    private Pane passwordPane;
    @FXML
    private Pane emailPane;
    @FXML
    private Pane deleteAccountPane;
    @FXML
    private JFXButton confirmUsernameButton;
    @FXML
    private JFXButton confirmPasswordButton;
    @FXML
    private JFXButton confirmEmailButton;
    @FXML
    private Label userName;
    @FXML
    private Label eMail;
    @FXML
    private Label won;
    @FXML
    private Label lost;
    @FXML
    private Label ratio;
    /**
     * Gets enabled if one of the edit buttons is clicked. Its purpose is to block all other scene elements that
     * shouldn't be clicked.
     */
    @FXML
    private Region veil;

    /**
     * Method called during the creation of the controller
     * <p>
     * It initializes the validators via {@link ValidatorHelper} and the properties used in this class
     */
    public void initialize() {
        Platform.runLater(() -> root.requestFocus());
        root.setOnMousePressed(e -> root.requestFocus());
        initValidators();
        initProperties();
        LOG.debug("ProfilePresenter fully initialized");
        updateProfileInfo(userInfo.getLoggedInProfile());
    }

    /**
     * Gets called during initialization and initializes all properties for the presenter.
     * Afterwards adds them to {@link #BOOLEAN_PROPERTIES} and binds them to the correct properties.
     * <p>
     * These boolean properties are being used to disable a confirm button
     * should the corresponding text fields be empty.
     */
    private void initProperties() {
        BOOLEAN_PROPERTIES.add(initBooleanProperty(confirmUsernameButton));
        BOOLEAN_PROPERTIES.add(initBooleanProperty(confirmPasswordButton));
        BOOLEAN_PROPERTIES.add(initBooleanProperty(confirmEmailButton));
        BOOLEAN_PROPERTIES.get(0).bind(newUsernameField.textProperty().isEmpty());
        BooleanBinding passwordBinding = currentPasswordField.textProperty().isEmpty().or(newPasswordField.textProperty().isEmpty()).or(
                confirmPasswordField.textProperty().isEmpty());
        BOOLEAN_PROPERTIES.get(1).bind(passwordBinding);
        BOOLEAN_PROPERTIES.get(2).bind(newEmailField.textProperty().isEmpty());
        LOG.debug("ProfilePresenter properties fully initialized");
    }

    /**
     * Gets called during initialization and initializes all validators for the presenter via {@link ValidatorHelper}.
     * Afterwards adds listeners to all fields with a validator by calling {@link #addFocusListener(TextInputControl)}.
     */
    private void initValidators() {
        ValidatorHelper.createRegexValidator(USERNAME_PATTERN, newUsernameField);
        ValidatorHelper.createRegexValidator(PASSWORD_PATTERN, newPasswordField);
        ValidatorHelper.createRegexValidator(EMAIL_PATTERN, newEmailField);
        usernameValueValidator = ValidatorHelper.createValueValidator("New Username cannot be old Username", newUsernameField, null);
        emailValueValidator = ValidatorHelper.createValueValidator("New Email cannot be old Email", newEmailField, null);
        ValidatorHelper.createCompareFieldValidator("Passwords must match", confirmPasswordField, newPasswordField);
        ValidatorHelper.createPasswordValidator("Wrong Password", currentPasswordField);
        ValidatorHelper.createPasswordValidator("Wrong Password", validatePasswordField);
        addFocusListener(newUsernameField);
        addFocusListener(newPasswordField);
        addFocusListener(confirmPasswordField);
        addFocusListener(newEmailField);
        LOG.debug("ProfilePresenter validators fully initialized");
    }

    /**
     * Posts an instance of the ShowMainMenuViewEvent to the EventBus the SceneManager is subscribed to.
     *
     * @see ReturnToMainMenuViewEvent
     * @see de.uol.swp.client.SceneManager
     */
    @FXML
    private void onMenuButtonPressed() {
        eventBus.post(RETURN_MAIN_VIEW_MESSAGE);
    }

    /**
     * Method called when one of the edit user info buttons is pressed.
     * <p>
     * It enables the correct pane and resets its nodes.
     *
     * @param event the ActionEvent created by pressing the button.
     */
    @FXML
    private void onEditButtonPressed(ActionEvent event) {
        Button button = (Button) event.getSource();
        Pane correctPane = getCorrectPane(button.getId());
        toggleView(correctPane, true);
        resetNodes(correctPane);
    }

    /**
     * Method called when one of the cancel buttons is pressed.
     * <p>
     * Hides the pane attached to the cancel button by calling {@link #toggleView}.
     *
     * @param event the ActionEvent created by pressing the cancel button
     */
    @FXML
    private void onCancelButtonPressed(ActionEvent event) {
        Button button = (Button) event.getSource();
        Pane correctPane = (Pane) button.getParent();
        toggleView(correctPane, false);
    }

    /**
     * Ensures that the validators of the username text field have no error.
     * <p>
     * If they dont then this method edits the users username via the UserService
     * and hides the usernamePane by calling {@link #toggleView}.
     */
    @FXML
    private void onConfirmUsernameButtonPressed() {
        String newUsername = newUsernameField.getText();
        if (newUsernameField.getActiveValidator() == null) {
            toggleView(usernamePane, false);
            userService.changeUserName(newUsername);
        }
    }

    /**
     * Ensures that the validators of all password fields have no error.
     * <p>
     * If they dont then this method edits the users password via the UserService but does
     * not handle the servers answer.
     */
    @FXML
    private void onConfirmPasswordButtonPressed() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        if (newPasswordField.getActiveValidator() == null && confirmPasswordField.getActiveValidator() == null) {
            userService.changeUserPassword(currentPassword, newPassword);
        }
    }

    /**
     * Ensures that the validators of the email text field have no error.
     * <p>
     * If they dont then this method edits the users email via the UserService
     * and hides the emailPane by calling {@link #toggleView}.
     */
    @FXML
    private void onConfirmEmailButtonPressed() {
        if (newEmailField.getActiveValidator() == null) {
            toggleView(emailPane, false);
            userService.changeUserEmail(newEmailField.getText().toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Method called when one of the delete buttons is pressed.
     * <p>
     * It enables the correct pane
     *
     * @param event This ActionEvent creates a delete button
     */
    @FXML
    public void onDeleteAccountButtonPressed(ActionEvent event) {
        Button button = (Button) event.getSource();
        Pane correctPane = getCorrectPane(button.getId());
        toggleView(correctPane, true);
    }

    /**
     * Method called when the button to confirm the deletion of an account is pressed
     * <p>
     * After pressing the button, the account is deleted via the UserService
     *
     * @see de.uol.swp.client.user.UserService
     */
    @FXML
    public void onConfirmDeleteAccountButtonPressed() {
        if (Strings.isNullOrEmpty(validatePasswordField.getText())) {
            eventBus.post(new ConfirmPasswordErrorEvent("Password cannot ne empty"));
        } else {
            userService.dropUser(validatePasswordField.getText());
        }
    }

    /**
     * Method called when the "playerInfo" button is pressed
     * <p>
     * users are sent to the userService
     */
    @FXML
    public void onPlayerInfoRequestButtonPressed() {
        userService.retrievePlayerProfile();
        toggleView(panePlayerInfo, true);
    }

    /**
     * Method for hiding userProfile
     */
    @FXML
    public void onClosePlayerInfo() {
        toggleView(panePlayerInfo, false);
    }

    /**
     * Handles, if message is found in EventBus
     * <p>
     * the information is used in labels
     *
     * @param message the PlayerInfoResponseMessage
     * @see PlayerInfoResponseMessage
     */
    @Subscribe
    public void onPlayerInfoMessage(PlayerInfoResponseMessage message) {
        userInfo.setLoggedInProfile(message.getProfile());
        updateProfileInfo(message.getProfile());
    }

    /**
     * Player data is updated
     *
     * @param profile contains information about players
     */
    private void updateProfileInfo(PlayerProfile profile) {
        Platform.runLater(() -> {
            this.userName.setText(profile.getPlayerName());
            this.eMail.setText(profile.getEmail());
            this.won.setText(profile.getWon());
            this.lost.setText(profile.getLoss());
            this.ratio.setText(profile.getRatio());
        });
    }

    /**
     * if password is confirmed, then a graphic error message is displayed
     *
     * @param response password is confirmed
     * @see DropAccountSuccessfulResponse
     */
    @Subscribe
    public void returnToLogin(DropAccountSuccessfulResponse response) {
        eventBus.post(SHOW_LOGIN_VIEW_EVENT);
        validatePasswordField.clear();
        toggleView(deleteAccountPane, false);
    }

    /**
     * Handles if password is not confirmed
     * When a new NotConfirmationPasswordResponse object is published in the EventBus
     *
     * @param response NotConfirmationPasswordResponse object
     * @see InvalidPasswordResponse
     */
    @Subscribe
    public void onInvalidPassword(InvalidPasswordResponse response) {
        validatePasswordField.getValidators().forEach(validatorBase -> {
            if (validatorBase instanceof PasswordValidator) {
                ((PasswordValidator) validatorBase).setErrorStatus(true);
                validatePasswordField.validate();
            }
        });
    }

    /**
     * Handles EditPasswordResponses found on the EventBus
     * <p>
     * If the response says the password in currentPassField was invalid this method sets the error status of
     * the correct PasswordValidator by iterating over the fields validator.
     * <p>
     * If instead the response is successful this method hides the passwordPane by calling {@link #toggleView}.
     *
     * @param message the EditPasswordResponse found on the EventBus
     * @see de.uol.swp.common.user.response.EditPasswordResponse
     */
    @Subscribe
    private void onEditPasswordMessage(EditPasswordResponse message) {
        if (message.isCorrectPassword()) {
            toggleView(passwordPane, false);
        } else {
            currentPasswordField.getValidators().forEach(validatorBase -> {
                if (validatorBase instanceof PasswordValidator) {
                    ((PasswordValidator) validatorBase).setErrorStatus(true);
                    currentPasswordField.validate();
                }
            });
        }
    }

    // -------------------------------------------------------------------------------
    // helper methods
    // -------------------------------------------------------------------------------

    /**
     * Creates a new SimpleBooleanProperty that calls {@link Region#setDisable(boolean) setDisable} on the given node
     * object should the property value change.
     *
     * @param node the node that is supposed to be disabled
     * @return a new SimpleBooleanProperty with the default value {@code false}
     */
    private SimpleBooleanProperty initBooleanProperty(Region node) {
        SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty();
        booleanProperty.addListener((observable, oldValue, newValue) -> node.setDisable(newValue));
        booleanProperty.set(false);
        return booleanProperty;
    }

    /**
     * Adds a listener to the given field that gets called everytime the field gets/loses focus.
     * <p>
     * Validates the given field if the value of the {@link Node#focusedProperty() focusedProperty()} is false
     *
     * @param inputControl the input field that is being observed. It should implement {@link IFXLabelFloatControl}
     */
    private void addFocusListener(TextInputControl inputControl) {
        if (inputControl instanceof IFXLabelFloatControl) {
            inputControl.focusedProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (Boolean.FALSE.equals(newValue)) {
                            ((IFXLabelFloatControl) inputControl).validate();
                            if (inputControl.equals(newPasswordField)) {
                                confirmPasswordField.validate();
                            }
                        }
                    }
            );
        }
    }

    /**
     * Helper method that returns the correct Pane corresponding to the paneId
     *
     * @param paneId a string that has to be either "username", "password" or "email"
     * @return the pane corresponding to the paneId
     * @throws IllegalArgumentException if the string does not match a pane
     */
    private Pane getCorrectPane(String paneId) {
        Pane pane;
        switch (paneId) {
            case "username":
                pane = usernamePane;
                break;
            case "password":
                pane = passwordPane;
                break;
            case "email":
                pane = emailPane;
                break;
            case "deleteAccount":
                pane = deleteAccountPane;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return pane;
    }

    /**
     * Helper method that resets all important nodes of a given pane.
     *
     * @param pane a {@code Pane} that has to be in this class
     * @throws IllegalArgumentException if the pane is invalid
     */
    private void resetNodes(Pane pane) {
        switch (pane.getId()) {
            case "usernamePane":
                newUsernameField.clear();
                break;
            case "passwordPane":
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                break;
            case "emailPane":
                newEmailField.clear();
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Helper method that sets the given pane and the veil Region
     * visible/invisible depending on the parameter
     *
     * @param pane    the pane that is being shown/hidden
     * @param visible if {@code true}:show, otherwise:hide
     */
    private void toggleView(Pane pane, boolean visible) {
        pane.setVisible(visible);
        veil.setVisible(visible);
    }

    /**
     * Handles successful login
     * <p>
     * If a LoginSuccessfulResponse is posted to the EventBus the validators are updated
     *
     * @param message the LoginSuccessfulResponse object seen on the EventBus
     * @see de.uol.swp.common.user.response.LoginSuccessfulResponse
     */
    @Subscribe
    private void loginSuccessful(LoginSuccessfulResponse message) {
        updateUserValidator();
    }

    private void updateUserValidator() {
        emailValueValidator.setCurrentValue(userInfo.getLoggedInUser().getEMail());
        usernameValueValidator.setCurrentValue(userInfo.getLoggedInUser().getUsername());
    }
}
