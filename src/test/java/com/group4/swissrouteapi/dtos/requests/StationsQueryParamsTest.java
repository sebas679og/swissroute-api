package com.group4.swissrouteapi.dtos.requests;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StationsQueryParams} bean validation constraints.
 *
 * <p>Uses the Jakarta Validation API directly — no Spring context required.
 */
@DisplayName("StationsQueryParams")
class StationsQueryParamsTest {

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

  // ===========================================================================
  // Object construction — all Lombok-generated entry points
  // ===========================================================================

  @Nested
  @DisplayName("construction")
  class ConstructionTest {

    @Test
    @DisplayName("should build via builder with query only")
    void shouldBuildWithQueryOnly() {
      StationsQueryParams params = StationsQueryParams.builder().query("Basel").build();

      assertThat(params.getQuery()).isEqualTo("Basel");
      assertThat(params.getLatitude()).isNull();
      assertThat(params.getLongitude()).isNull();
    }

    @Test
    @DisplayName("should build via builder with coordinates only")
    void shouldBuildWithCoordinatesOnly() {
      StationsQueryParams params =
          StationsQueryParams.builder().latitude(47.5596).longitude(7.5886).build();

      assertThat(params.getQuery()).isNull();
      assertThat(params.getLatitude()).isEqualTo(47.5596);
      assertThat(params.getLongitude()).isEqualTo(7.5886);
    }

    @Test
    @DisplayName("should build via builder with all fields")
    void shouldBuildWithAllFields() {
      StationsQueryParams params =
          StationsQueryParams.builder().query("Basel").latitude(47.5596).longitude(7.5886).build();

      assertThat(params.getQuery()).isEqualTo("Basel");
      assertThat(params.getLatitude()).isEqualTo(47.5596);
      assertThat(params.getLongitude()).isEqualTo(7.5886);
    }

    @Test
    @DisplayName("should build via no-args constructor with all fields null")
    void shouldBuildViaNoArgsConstructorWithNullFields() {
      StationsQueryParams params = new StationsQueryParams();

      assertThat(params.getQuery()).isNull();
      assertThat(params.getLatitude()).isNull();
      assertThat(params.getLongitude()).isNull();
    }

    @Test
    @DisplayName("should build via no-args constructor and setters")
    void shouldBuildViaNoArgsConstructorAndSetters() {
      StationsQueryParams params = new StationsQueryParams();
      params.setQuery("Geneva");
      params.setLatitude(46.2044);
      params.setLongitude(6.1432);

      assertThat(params.getQuery()).isEqualTo("Geneva");
      assertThat(params.getLatitude()).isEqualTo(46.2044);
      assertThat(params.getLongitude()).isEqualTo(6.1432);
    }

    @Test
    @DisplayName("should build via all-args constructor")
    void shouldBuildViaAllArgsConstructor() {
      StationsQueryParams params = new StationsQueryParams("Bern", 46.9481, 7.4474);

      assertThat(params.getQuery()).isEqualTo("Bern");
      assertThat(params.getLatitude()).isEqualTo(46.9481);
      assertThat(params.getLongitude()).isEqualTo(7.4474);
    }
  }

  // ===========================================================================
  // Field-level constraints
  // ===========================================================================

  @Nested
  @DisplayName("field-level constraints")
  class FieldLevelConstraintsTest {

    @Test
    @DisplayName("should have no field-level violations for null query")
    void shouldHaveNoFieldViolationsForNullQuery() {
      StationsQueryParams params =
          StationsQueryParams.builder().query(null).latitude(47.5596).longitude(7.5886).build();

      long fieldViolations =
          validate(params).stream()
              .filter(v -> !v.getPropertyPath().toString().isEmpty())
              .filter(v -> v.getLeafBean() != null)
              .count();

      assertThat(
              validate(params).stream()
                  .map(v -> v.getPropertyPath().toString())
                  .filter(
                      path ->
                          path.equals("query")
                              || path.equals("latitude")
                              || path.equals("longitude")))
          .isEmpty();
    }

    @Test
    @DisplayName("should have no field-level violations for null latitude and longitude")
    void shouldHaveNoFieldViolationsForNullCoordinates() {
      StationsQueryParams params = StationsQueryParams.builder().query("Basel").build();

      assertThat(
              validate(params).stream()
                  .map(v -> v.getPropertyPath().toString())
                  .filter(path -> path.equals("latitude") || path.equals("longitude")))
          .isEmpty();
    }
  }

  // ===========================================================================
  // Equality and toString — @Data contract
  // ===========================================================================

  @Nested
  @DisplayName("@Data generated methods")
  class DataContractTest {

    @Test
    @DisplayName("should be equal when all fields match")
    void shouldBeEqualWhenAllFieldsMatch() {
      StationsQueryParams a = new StationsQueryParams("Basel", 47.5596, 7.5886);
      StationsQueryParams b = new StationsQueryParams("Basel", 47.5596, 7.5886);

      assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("should not be equal when query differs")
    void shouldNotBeEqualWhenQueryDiffers() {
      StationsQueryParams a = new StationsQueryParams("Basel", null, null);
      StationsQueryParams b = new StationsQueryParams("Bern", null, null);

      assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("should have equal hashCode for equal objects")
    void shouldHaveEqualHashCodeForEqualObjects() {
      StationsQueryParams a = new StationsQueryParams("Basel", 47.5596, 7.5886);
      StationsQueryParams b = new StationsQueryParams("Basel", 47.5596, 7.5886);

      assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("should include field values in toString")
    void shouldIncludeFieldValuesInToString() {
      StationsQueryParams params = new StationsQueryParams("Basel", 47.5596, 7.5886);

      assertThat(params.toString()).contains("Basel").contains("47.5596").contains("7.5886");
    }
  }
}
