package com.group4.swissrouteapi.utils.validators.passwords;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.WhitespaceRule;

import java.util.List;

/**
 * Custom validator implementation for the {@link ValidPassword} annotation.
 *
 * <p>This validator enforces password security policies using a set of predefined rules to ensure
 * strong authentication credentials.
 *
 * <p>The following constraints are applied:
 *
 * <ul>
 *   <li>Minimum length of 8 characters
 *   <li>At least one uppercase letter
 *   <li>At least one lowercase letter
 *   <li>At least one numeric digit
 *   <li>At least one special character
 *   <li>No whitespace characters allowed
 * </ul>
 *
 * <p>Null or blank values are considered valid in this validator. Presence validation must be
 * handled separately using {@code @NotBlank} or {@code @NotNull}.
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private final PasswordValidator validator =
            new PasswordValidator(
                    List.of(
                            new LengthRule(8, Integer.MAX_VALUE),
                            new CharacterRule(EnglishCharacterData.UpperCase, 1),
                            new CharacterRule(EnglishCharacterData.LowerCase, 1),
                            new CharacterRule(EnglishCharacterData.Digit, 1),
                            new CharacterRule(EnglishCharacterData.Special, 1),
                            new WhitespaceRule()));

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }

        return validator.validate(new PasswordData(password)).isValid();
    }
}
