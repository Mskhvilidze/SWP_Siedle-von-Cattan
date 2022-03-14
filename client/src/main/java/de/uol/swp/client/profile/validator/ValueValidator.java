package de.uol.swp.client.profile.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

/**
 * Value validation that is applied on text input controls
 * such as {@link TextField} and {@link TextArea}.
 * <p>
 * This class should be used to compare a field and a set String to ensure that they're <strong>not</strong> equal.
 * <strong>No errors will be triggered if the currentValue is null.</strong>
 * <p>
 * Everytime the value that has to be compared changes, {@link #setCurrentValue(String)} has to be called.
 */
public class ValueValidator extends ValidatorBase {

    private final SimpleStringProperty currentValue = new SimpleStringProperty();

    /**
     * Constructor
     *
     * @param currentValue the value that {@link #srcControl} is being compared with.
     * @param message      the Message that will be set as the validator's {@link #message}.
     */
    public ValueValidator(String currentValue, String message) {
        setMessage(message);
        this.currentValue.set(currentValue);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Will set {@link #hasErrors} to false if the field is either empty or does not match the current value.
     */
    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl srcField = (TextInputControl) srcControl.get();
            if (srcField.getText().isBlank() || currentValue.get() == null) {
                hasErrors.set(false);
            } else {
                hasErrors.set(srcField.getText().equals(currentValue.get()));
            }
        }
    }

    /**
     * Sets which value {@link #srcControl} gets compared with.
     *
     * @param currentValue the new value for the validator
     */
    public void setCurrentValue(String currentValue) {
        this.currentValue.set(currentValue);
    }
}
