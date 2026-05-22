package com.group4.swissrouteapi.utils.validators.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("ValidStationQueryValidator")
class ValidStationQueryValidatorTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private Set<ConstraintViolation<StationsQueryParams>> validate(StationsQueryParams params) {
    return validator.validate(params);
  }

  private String singleMessage(Set<ConstraintViolation<StationsQueryParams>> violations) {
    assertThat(violations).hasSize(1);
    return violations.iterator().next().getMessage();
  }

  private StationsQueryParams queryOnly(String query) {
    return StationsQueryParams.builder().query(query).build();
  }

  private StationsQueryParams coordsOnly(Double latitude, Double longitude) {
    return StationsQueryParams.builder().latitude(latitude).longitude(longitude).build();
  }

  private StationsQueryParams allThree(String query, Double latitude, Double longitude) {
    return StationsQueryParams.builder()
        .query(query)
        .latitude(latitude)
        .longitude(longitude)
        .build();
  }

  private StationsQueryParams empty() {
    return StationsQueryParams.builder().build();
  }

  // ===========================================================================
  // Null-safe guard
  // ===========================================================================

  @Nested
  @DisplayName("when the object itself is null")
  class NullObjectTest {

    @Test
    @DisplayName("should pass validation — null is treated as valid by the validator")
    void shouldPassWhenObjectIsNull() {
      // The validator is called with a non-null wrapper; test the guard via direct invocation
      ValidStationQueryValidator v = new ValidStationQueryValidator();
      boolean result = v.isValid(null, null);

      assertThat(result).isTrue();
    }
  }

  // ===========================================================================
  // Query only
  // ===========================================================================

  @Nested
  @DisplayName("when only query is provided")
  class QueryOnlyTest {

    @Test
    @DisplayName("should pass when query has a value and no coordinates are set")
    void shouldPassWithQueryOnly() {
      assertThat(validate(queryOnly("Basel"))).isEmpty();
    }

    @Test
    @DisplayName("should pass when query is a single character")
    void shouldPassWithSingleCharacterQuery() {
      assertThat(validate(queryOnly("Z"))).isEmpty();
    }

    @Test
    @DisplayName("should pass when query contains special characters")
    void shouldPassWithSpecialCharactersInQuery() {
      assertThat(validate(queryOnly("Zürich HB"))).isEmpty();
    }
  }

  // ===========================================================================
  // Coordinates only
  // ===========================================================================

  @Nested
  @DisplayName("when only coordinates are provided")
  class CoordinatesOnlyTest {

    @Test
    @DisplayName("should pass when both latitude and longitude are valid")
    void shouldPassWithValidCoordinates() {
      assertThat(validate(coordsOnly(47.5596, 7.5886))).isEmpty();
    }

    @Test
    @DisplayName("should pass at the minimum boundary values (-90, -180)")
    void shouldPassAtMinimumBoundary() {
      assertThat(validate(coordsOnly(-90.0, -180.0))).isEmpty();
    }

    @Test
    @DisplayName("should pass at the maximum boundary values (90, 180)")
    void shouldPassAtMaximumBoundary() {
      assertThat(validate(coordsOnly(90.0, 180.0))).isEmpty();
    }

    @Test
    @DisplayName("should pass at zero coordinates (0, 0)")
    void shouldPassAtZeroCoordinates() {
      assertThat(validate(coordsOnly(0.0, 0.0))).isEmpty();
    }
  }

  // ===========================================================================
  // Conflict: query + coordinates together
  // ===========================================================================

  @Nested
  @DisplayName("when query and coordinates are both provided")
  class ConflictTest {

    @Test
    @DisplayName("should fail with conflict message when all three fields are set")
    void shouldFailWhenQueryAndCoordinatesAreBothProvided() {
      StationsQueryParams params = allThree("Basel", 47.5596, 7.5886);

      String message = singleMessage(validate(params));

      assertThat(message)
          .isEqualTo(
              "Cannot provide both text search ('query') and coordinates ('latitude'/'longitude') "
                  + "at the same time. Choose one method.");
    }

    @Test
    @DisplayName("should produce exactly one violation for the conflict case")
    void shouldProduceExactlyOneViolationForConflict() {
      StationsQueryParams params = allThree("Basel", 47.5596, 7.5886);

      assertThat(validate(params)).hasSize(1);
    }
  }

  // ===========================================================================
  // Nothing provided
  // ===========================================================================

  @Nested
  @DisplayName("when no fields are provided")
  class NothingProvidedTest {

    @Test
    @DisplayName("should fail when all fields are null")
    void shouldFailWhenAllFieldsAreNull() {
      String message = singleMessage(validate(empty()));

      assertThat(message)
          .isEqualTo(
              "Either 'query' OR both coordinates ('Latitude' and 'Longitude') must be provided.");
    }

    @Test
    @DisplayName("should fail when query is empty string and no coordinates are set")
    void shouldFailWhenQueryIsEmptyStringAndNoCoordinates() {
      StationsQueryParams params = queryOnly("");

      String message = singleMessage(validate(params));

      assertThat(message)
          .isEqualTo(
              "Either 'query' OR both coordinates ('Latitude' and 'Longitude') must be provided.");
    }

    @Test
    @DisplayName("should fail when query is blank and no coordinates are set")
    void shouldFailWhenQueryIsBlankAndNoCoordinates() {
      StationsQueryParams params = queryOnly("   ");

      String message = singleMessage(validate(params));

      assertThat(message)
          .isEqualTo(
              "Either 'query' OR both coordinates ('Latitude' and 'Longitude') must be provided.");
    }
  }

  // ===========================================================================
  // Partial coordinates
  // ===========================================================================

  @Nested
  @DisplayName("when only one coordinate is provided")
  class PartialCoordinatesTest {

    @Test
    @DisplayName("should fail when only latitude is provided")
    void shouldFailWhenOnlyLatitudeProvided() {
      StationsQueryParams params = StationsQueryParams.builder().latitude(47.5596).build();

      String message = singleMessage(validate(params));

      assertThat(message)
          .isEqualTo("Both coordinates latitude and longitude must be provided together.");
    }

    @Test
    @DisplayName("should fail when only longitude is provided")
    void shouldFailWhenOnlyLongitudeProvided() {
      StationsQueryParams params = StationsQueryParams.builder().longitude(7.5886).build();

      String message = singleMessage(validate(params));

      assertThat(message)
          .isEqualTo("Both coordinates latitude and longitude must be provided together.");
    }
  }

  // ===========================================================================
  // Coordinates out of range
  // ===========================================================================

  @Nested
  @DisplayName("when coordinates are out of range")
  class CoordinatesOutOfRangeTest {

    private static final String OUT_OF_RANGE_MESSAGE =
        "Coordinates out of range. latitude must be between -90 and 90. "
            + "longitude must be between -180 and 180.";

    @Test
    @DisplayName("should fail when latitude exceeds 90")
    void shouldFailWhenLatitudeExceeds90() {
      String message = singleMessage(validate(coordsOnly(90.1, 7.5886)));

      assertThat(message).isEqualTo(OUT_OF_RANGE_MESSAGE);
    }

    @Test
    @DisplayName("should fail when latitude is below -90")
    void shouldFailWhenLatitudeIsBelowMinus90() {
      String message = singleMessage(validate(coordsOnly(-90.1, 7.5886)));

      assertThat(message).isEqualTo(OUT_OF_RANGE_MESSAGE);
    }

    @Test
    @DisplayName("should fail when longitude exceeds 180")
    void shouldFailWhenLongitudeExceeds180() {
      String message = singleMessage(validate(coordsOnly(47.5596, 180.1)));

      assertThat(message).isEqualTo(OUT_OF_RANGE_MESSAGE);
    }

    @Test
    @DisplayName("should fail when longitude is below -180")
    void shouldFailWhenLongitudeIsBelowMinus180() {
      String message = singleMessage(validate(coordsOnly(47.5596, -180.1)));

      assertThat(message).isEqualTo(OUT_OF_RANGE_MESSAGE);
    }

    @ParameterizedTest(name = "lat={0}, lon={1}")
    @CsvSource({
      "91.0,   0.0", // latitude too high
      "-91.0,  0.0", // latitude too low
      "0.0,  181.0", // longitude too high
      "0.0, -181.0", // longitude too low
      "91.0, 181.0", // both out of range
    })
    @DisplayName("should fail for out-of-range coordinate combinations")
    void shouldFailForOutOfRangeCoordinates(double lat, double lon) {
      String message = singleMessage(validate(coordsOnly(lat, lon)));

      assertThat(message).isEqualTo(OUT_OF_RANGE_MESSAGE);
    }
  }
}
