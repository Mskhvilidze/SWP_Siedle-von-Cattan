package de.uol.swp.client.profile.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

/**
 * Compare validation that is applied on text input controls
 * such as {@link TextField} and {@link TextArea}.
 * <p>
 * This class should be used to compare two fields to ensure that they're equal.
 * <p>
 * It will only display errors to {@link #srcControl} but listeners should be added
 * to both input controls to ensure that {@link #srcControl#validate()} can be called twice.
 */
public class CompareFieldValidator extends ValidatorBase {

    protected SimpleObjectProperty<TextInputControl> comparison = new SimpleObjectProperty<>();

    /**
     * Constructor
     *
     * @param comparison the TextInputControl object that is being compared with.
     * @param message    the Message that will be set as the validator's {@link #message}.
     */
    public CompareFieldValidator(TextInputControl comparison, String message) {
        this.comparison.set(comparison);
        setMessage(message);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Will set {@link #hasErrors} to false if both fields are either empty or equal.
     */
    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            String srcField = ((TextInputControl) srcControl.get()).getText();
            String compareField = comparison.get().getText();
            if (srcField.isBlank() && compareField.isBlank()) {
                hasErrors.set(false);
            } else {
                hasErrors.set(!compareField.equals(srcField));
            }
        }
    }
}
