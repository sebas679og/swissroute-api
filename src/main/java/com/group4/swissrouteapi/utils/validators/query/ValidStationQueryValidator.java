package com.group4.swissrouteapi.utils.validators.query;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ValidStationQueryValidator
 *
 * <p>Custom constraint validator for {@link ValidStationQuery} applied to {@link
 * StationsQueryParams}.
 *
 * <p>Ensures that either a non-blank query string or both geographic coordinates (latitude and
 * longitude) are provided.
 *
 * <p>Performs validation on coordinate ranges:
 *
 * <ul>
 *   <li>Latitude must be between -90 and 90.
 *   <li>Longitude must be between -180 and 180.
 * </ul>
 *
 * <p>Builds custom validation messages when constraints are violated, disabling default messages
 * for clarity.
 */
public class ValidStationQueryValidator
    implements ConstraintValidator<ValidStationQuery, StationsQueryParams> {

  @Override
  public boolean isValid(StationsQueryParams value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    boolean hasQuery = value.getQuery() != null && !value.getQuery().trim().isEmpty();
    boolean hasLatitude = value.getLatitude() != null;
    boolean hasLongitude = value.getLongitude() != null;

    if (hasQuery && hasLatitude && hasLongitude) {
      buildCustomMessage(
          context,
          "Cannot provide both text search ('query') and coordinates ('latitude'/'longitude') "
              + "at the same time. Choose one method.");
      return false;
    }

    if (!hasQuery && !hasLatitude && !hasLongitude) {
      buildCustomMessage(
          context,
          "Either 'query' OR both coordinates ('Latitude' and 'Longitude') must be provided.");
      return false;
    }

    if (hasLatitude || hasLongitude) {
      return validateCoordinates(value, hasLatitude, hasLongitude, context);
    }

    return true;
  }

  private boolean validateCoordinates(
      StationsQueryParams value,
      boolean hasLatitude,
      boolean hasLongitude,
      ConstraintValidatorContext context) {

    if (!hasLatitude || !hasLongitude) {
      buildCustomMessage(
          context, "Both coordinates latitude and longitude must be provided together.");
      return false;
    }

    boolean isLatitudeValid = value.getLatitude() >= -90 && value.getLatitude() <= 90;
    boolean isLongitudeValid = value.getLongitude() >= -180 && value.getLongitude() <= 180;

    if (!isLatitudeValid || !isLongitudeValid) {
      buildCustomMessage(
          context,
          "Coordinates out of range. latitude must be between -90 and 90. "
              + "longitude must be between -180 and 180.");
      return false;
    }

    return true;
  }

  public void buildCustomMessage(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
