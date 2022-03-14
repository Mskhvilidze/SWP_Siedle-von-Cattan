package de.uol.swp.client.profile.validator;

import com.jfoenix.validation.RegexValidator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.util.regex.Pattern;

/**
 * Regex validation, that is applied on text input controls
 * such as {@link TextField} and {@link TextArea}.
 * <p>
 * This class should be used instead of {@link RegexValidator} to have more control the process.
 */
public class CustomRegexValidator extends RegexValidator {

    private final Pattern regexPattern;

    /**
     * Constructor
     *
     * @param regexPattern the pattern this {@link #srcControl} is being compared with.
     * @param message      the Message that will be set as the validator's {@link #message}.
     */
    public CustomRegexValidator(Pattern regexPattern, String message) {
        this.regexPattern = regexPattern;
        setMessage(message);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Will set {@link #hasErrors} to false if the field is either empty or matches the pattern.
     */
    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl textField = (TextInputControl) srcControl.get();
            String text = (textField.getText() == null) ? "" : textField.getText(); // Treat null like empty string
            hasErrors.set(!regexPattern.matcher(text).matches());
        }
    }
}
