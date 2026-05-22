package com.group4.swissrouteapi.utils.validators.query;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ValidStationQuery
 *
 * <p>Ensures that either a non-blank query string or both geographic coordinates (latitude and
 * longitude) are provided. Delegates validation logic to {@link ValidStationQueryValidator}.
 *
 * <p>Can be used at the type level to enforce query parameter rules in station search requests.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidStationQueryValidator.class)
@Documented
public @interface ValidStationQuery {

  /**
   * Default validation error message when constraints are violated.
   *
   * @return the error message template
   */
  String message() default "Must provide 'query' OR both 'Latitude' and 'Longitude'";

  /**
   * Allows specification of validation groups.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Allows specification of custom payload objects for clients of the Bean Validation API.
   *
   * @return the payload type array
   */
  Class<? extends Payload>[] payload() default {};
}
