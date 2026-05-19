package com.group4.swissrouteapi.utils.validators.passwords;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Custom Bean Validation annotation used to enforce password security requirements.
 *
 * <p>This constraint delegates validation logic to {@link PasswordConstraintValidator}, which
 * applies a configurable set of password complexity rules.
 *
 * <p>The enforced rules typically include:
 *
 * <ul>
 *   <li>Minimum length of 8 characters
 *   <li>At least one uppercase letter
 *   <li>At least one lowercase letter
 *   <li>At least one numeric digit
 *   <li>At least one special character
 *   <li>No whitespace characters
 * </ul>
 */
@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    /**
     * Default validation error message returned when the password does not satisfy security
     * requirements.
     *
     * @return the validation error message
     */
    String message() default
            """
              Invalid password. The password must have a minimum of 8 characters
              and include at least one uppercase letter, one lowercase letter,
              a number, and a special character.
            """;

    /**
     * Allows specification of validation groups.
     *
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload that can be attached to the constraint. Used by Bean Validation clients to associate
     * metadata.
     *
     * @return the payload associated with the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
