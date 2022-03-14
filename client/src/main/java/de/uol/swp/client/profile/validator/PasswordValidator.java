package de.uol.swp.client.profile.validator;

import com.jfoenix.controls.base.IFXLabelFloatControl;
import com.jfoenix.validation.base.ValidatorBase;

/**
 * This class should be used to display the error to {@link #srcControl}
 * once the server has authorized the password that has been entered in srcControl.
 * The best way to do that is by calling {@link #setErrorStatus} and then {@link IFXLabelFloatControl#validate()}.
 * <p>
 * The validator object can be obtained by iterating over {@link IFXLabelFloatControl#getValidators() getValidators()}
 */
public class PasswordValidator extends ValidatorBase {

    /**
     * Constructor
     *
     * @param message the Message that will be set as the validator's {@link #message}.
     */
    public PasswordValidator(String message) {
        setMessage(message);
    }

    /**
     * Has been overridden to prevent the class from calling {@link #eval()}
     */
    @Override
    public void validate() {
        onEval();
    }

    /**
     * Will throw an {@link UnsupportedOperationException}
     */
    @Override
    protected void eval() {
        throw new UnsupportedOperationException();
    }

    /**
     * Enables/Disables the error display for {@link #srcControl}
     *
     * @param errorStatus {@code true} Enable or {@code false} disable the error display
     */
    public void setErrorStatus(boolean errorStatus) {
        hasErrors.set(errorStatus);
    }
}
