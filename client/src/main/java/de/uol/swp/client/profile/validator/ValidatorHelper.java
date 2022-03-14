package de.uol.swp.client.profile.validator;

import com.jfoenix.controls.base.IFXLabelFloatControl;
import javafx.scene.control.TextInputControl;

import java.util.regex.Pattern;

/**
 * Helper class that can be used to create basic validators that only need an error message
 */
public final class ValidatorHelper {

    public static final String ILLEGAL_ARGUMENT_MESSAGE = " should be a subclass of TextInputControl";

    private ValidatorHelper() {
    }

    /**
     * Creates a {@link CustomRegexValidator} object with default error message and adds it to the srcControl.
     *
     * @param pattern    the pattern this srcControl is being compared with.
     * @param srcControl the field this validator is being attached to. <strong>Has</strong> to inherit {@link TextInputControl}
     * @return the validator that has been added to the srcControl
     * @throws IllegalArgumentException if srcControl does not inherit {@link TextInputControl}
     */
    public static CustomRegexValidator createRegexValidator(Pattern pattern, IFXLabelFloatControl srcControl) {
        return createRegexValidator(pattern, srcControl, "Must mach pattern: " + pattern.toString());
    }

    /**
     * Creates a {@link CustomRegexValidator} object with custom error message and adds it to the srcControl.
     *
     * @param pattern      the pattern this srcControl is being compared with.
     * @param srcControl   the field this validator is being attached to. <strong>Has</strong> to inherit {@link TextInputControl}
     * @param errorMessage the error message
     * @return the validator that has been added to the srcControl
     * @throws IllegalArgumentException if srcControl does not inherit {@link TextInputControl}
     */
    public static CustomRegexValidator createRegexValidator(Pattern pattern, IFXLabelFloatControl srcControl, String errorMessage) {
        if (srcControl instanceof TextInputControl) {
            CustomRegexValidator validator = new CustomRegexValidator(pattern, errorMessage);
            srcControl.getValidators().add(validator);
            return validator;
        } else {
            throw new IllegalArgumentException(srcControl.getClass() + ILLEGAL_ARGUMENT_MESSAGE);
        }
    }

    /**
     * Creates a {@link ValueValidator} object and adds it to the srcControl.
     *
     * @param message    the error message that will be displayed
     * @param srcControl the field this validator is being attached to. <strong>Has</strong> to inherit {@link TextInputControl}
     * @param newValue   the value the field will be compared with. Entering {@code null}
     * @return the validator that has been added to the srcControl
     * @throws IllegalArgumentException if srcControl does not inherit {@link TextInputControl}
     */
    public static ValueValidator createValueValidator(String message, IFXLabelFloatControl srcControl, String newValue) {
        if (srcControl instanceof TextInputControl) {
            ValueValidator validator = new ValueValidator(newValue, message);
            srcControl.getValidators().add(validator);
            return validator;
        } else {
            throw new IllegalArgumentException(srcControl.getClass() + ILLEGAL_ARGUMENT_MESSAGE);
        }
    }

    /**
     * Creates a {@link CompareFieldValidator} object and adds it to the srcControl.
     *
     * @param message        the {@code null} error message that will be displayed
     * @param srcControl     the field this validator is being attached to. <strong>Has</strong> to inherit {@link TextInputControl}
     * @param compareControl the field srcControl is being compared with
     * @return the validator that has been added to the srcControl
     * @throws IllegalArgumentException if srcControl does not inherit {@link TextInputControl}
     */
    public static CompareFieldValidator createCompareFieldValidator(String message, IFXLabelFloatControl srcControl,
                                                                    TextInputControl compareControl) {
        if (srcControl instanceof TextInputControl) {
            CompareFieldValidator validator = new CompareFieldValidator(compareControl, message);
            srcControl.getValidators().add(validator);
            return validator;
        } else {
            throw new IllegalArgumentException(srcControl.getClass() + ILLEGAL_ARGUMENT_MESSAGE);
        }
    }

    /**
     * Creates a {@link PasswordValidator} object and adds it to the srcControl.
     *
     * @param message    the error message that will be displayed
     * @param srcControl the field this validator is being attached to. <strong>Has</strong> to inherit {@link TextInputControl}
     *                   and should be a {@link com.jfoenix.controls.JFXPasswordField JFXPasswordField}
     * @return the validator that has been added to the srcControl
     * @throws IllegalArgumentException if srcControl does not inherit {@link TextInputControl}
     */
    public static PasswordValidator createPasswordValidator(String message, IFXLabelFloatControl srcControl) {
        if (srcControl instanceof TextInputControl) {
            PasswordValidator validator = new PasswordValidator(message);
            srcControl.getValidators().add(validator);
            return validator;
        } else {
            throw new IllegalArgumentException(srcControl.getClass() + ILLEGAL_ARGUMENT_MESSAGE);
        }
    }
}
