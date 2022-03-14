package de.uol.swp.client.profile.validator;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ValidatorHelper
 */

@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.DefaultPackage"})
@Tag("javafx")
class ValidatorHelperTest {

    static final Pattern DEFAULT_PATTERN = Pattern.compile("[a-zA-Z0-9]{4,10}");
    static final String BAD_PATTERN = "a.";
    static final String GOOD_PATTERN = "aGT4L";
    static final String VALUE = "value";

    @BeforeAll
    static void setUp() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @Test
    void createRegexValidator_WrongInputControlGiven_ShouldThrowException() {
        JFXComboBox<String> illegalControl = new JFXComboBox<>();
        assertThrows(IllegalArgumentException.class, () -> ValidatorHelper.createRegexValidator(DEFAULT_PATTERN, illegalControl));
    }

    @Test
    void customRegexValidator_WrongInputGiven_ShouldReturnError() {
        JFXTextField textInputControl = new JFXTextField(BAD_PATTERN);
        CustomRegexValidator validator = ValidatorHelper.createRegexValidator(DEFAULT_PATTERN, textInputControl);
        textInputControl.validate();
        assertTrue(validator.getHasErrors());
    }

    @Test
    void customRegexValidator_CorrectInputGiven_ShouldNotReturnError() {
        JFXTextField textInputControl = new JFXTextField(GOOD_PATTERN);
        CustomRegexValidator validator = ValidatorHelper.createRegexValidator(DEFAULT_PATTERN, textInputControl);
        textInputControl.validate();
        assertFalse(validator.getHasErrors());
    }

    @Test
    void customRegexValidator_EmptyInputGiven_ShouldReturnError() {
        JFXTextField textInputControl = new JFXTextField("");
        CustomRegexValidator validator = ValidatorHelper.createRegexValidator(DEFAULT_PATTERN, textInputControl);
        textInputControl.validate();
        assertTrue(validator.getHasErrors());
    }

    @Test
    void createValueValidatorTest_WrongInputControlGiven_ShouldThrowException() {
        JFXComboBox<String> illegalControl = new JFXComboBox<>();
        assertThrows(IllegalArgumentException.class, () -> ValidatorHelper.createValueValidator("", illegalControl, ""));
    }

    @Test
    void valueValidator_DifferentInputGiven_ShouldNotReturnError() {
        JFXTextField textInputControl = new JFXTextField("a");
        JFXTextField textInputControl2 = new JFXTextField("");
        ValueValidator validator = ValidatorHelper.createValueValidator("", textInputControl, VALUE);
        ValueValidator validator2 = ValidatorHelper.createValueValidator("", textInputControl2, VALUE);
        textInputControl.validate();
        textInputControl2.validate();
        assertFalse(validator.getHasErrors());
        assertFalse(validator2.getHasErrors());
    }

    @Test
    void valueValidator_SameInputGiven_ShouldReturnError() {
        JFXTextField textInputControl = new JFXTextField(VALUE);
        ValueValidator validator = ValidatorHelper.createValueValidator("", textInputControl, null);

        textInputControl.validate();
        assertFalse(validator.getHasErrors());

        validator.setCurrentValue(VALUE);
        textInputControl.validate();
        assertTrue(validator.getHasErrors());
    }

    @Test
    void createCompareFieldValidatorTest_WrongInputControlGiven_ShouldThrowException() {
        JFXTextField validControl = new JFXTextField();
        JFXComboBox<String> illegalControl = new JFXComboBox<>();
        assertThrows(IllegalArgumentException.class,
                () -> ValidatorHelper.createCompareFieldValidator("", illegalControl, validControl));
    }

    @Test
    void compareValidator_DifferentInputGiven_ShouldReturnError() {
        JFXTextField textInputControl = new JFXTextField("a");
        JFXTextField textInputControl2 = new JFXTextField("");
        CompareFieldValidator validator = ValidatorHelper.createCompareFieldValidator("", textInputControl, textInputControl2);
        textInputControl.validate();
        assertTrue(validator.getHasErrors());
        textInputControl.setText("");
        textInputControl.validate();
        assertFalse(validator.getHasErrors());
    }

    @Test
    void compareValidator_SameInputGiven_ShouldNotReturnError() {
        JFXTextField textInputControl = new JFXTextField(VALUE);
        JFXTextField textInputControl2 = new JFXTextField(VALUE);
        CompareFieldValidator validator = ValidatorHelper.createCompareFieldValidator("", textInputControl, textInputControl2);
        textInputControl.validate();
        assertFalse(validator.getHasErrors());
    }

    @Test
    void createPasswordValidatorTest_WrongInputControlGiven_ShouldThrowException() {
        JFXComboBox<String> illegalControl = new JFXComboBox<>();
        assertThrows(IllegalArgumentException.class, () -> ValidatorHelper.createPasswordValidator("", illegalControl));
    }

    @Test
    void passwordValidator_DifferentPasswordGiven_ShouldReturnError() {
        JFXTextField textInputControl = new JFXTextField("a");
        PasswordValidator validator = ValidatorHelper.createPasswordValidator("", textInputControl);
        validator.setErrorStatus(!textInputControl.getText().equals(VALUE));
        textInputControl.validate();
        assertTrue(validator.getHasErrors());
    }

    @Test
    void passwordValidator_SamePasswordGiven_ShouldNotReturnError() {
        JFXTextField textInputControl = new JFXTextField(VALUE);
        PasswordValidator validator = ValidatorHelper.createPasswordValidator("", textInputControl);
        validator.setErrorStatus(!textInputControl.getText().equals(VALUE));
        textInputControl.validate();
        assertFalse(validator.getHasErrors());
    }

    @Test
    void passwordValidator_evalCalled_ShouldThrowException() {
        JFXTextField textInputControl = new JFXTextField(VALUE);
        PasswordValidator validator = ValidatorHelper.createPasswordValidator("", textInputControl);
        assertThrows(UnsupportedOperationException.class, validator::eval);
    }
}