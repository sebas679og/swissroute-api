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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

  private StationsQueryParams buildRequest(String query) {
    return StationsQueryParams.builder().query(query).build();
  }

  private Set<ConstraintViolation<StationsQueryParams>> validate(StationsQueryParams params) {
    return validator.validate(params);
  }

  private String singleMessage(Set<ConstraintViolation<StationsQueryParams>> violations) {
    assertThat(violations).hasSize(1);
    return violations.iterator().next().getMessage();
  }

  // ---------------------------------------------------------------------------
  // Happy path
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("should pass validation when query is a valid non-blank string")
  void shouldPassWhenQueryIsValid() {
    assertThat(validate(buildRequest("Basel"))).isEmpty();
  }

  @Test
  @DisplayName("should pass validation when query is built via no-args constructor and setter")
  void shouldPassWhenQuerySetViaNoArgsConstructor() {
    StationsQueryParams params = new StationsQueryParams();
    params.setQuery("Zurich");

    assertThat(validate(params)).isEmpty();
  }

  @ParameterizedTest(name = "valid query: \"{0}\"")
  @ValueSource(
      strings = {
        "Basel",
        "Zürich HB",
        "Geneva",
        "A", // single character
        "Bern Hauptbahnhof" // multi-word
      })
  @DisplayName("should pass for any non-blank query string")
  void shouldPassForNonBlankQueries(String query) {
    assertThat(validate(buildRequest(query))).isEmpty();
  }

  // ---------------------------------------------------------------------------
  // query field
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("query field")
  class QueryFieldTest {

    @Test
    @DisplayName("should fail when query is null")
    void shouldFailWhenQueryIsNull() {
      StationsQueryParams params = buildRequest(null);

      String message = singleMessage(validate(params));

      assertThat(message).isEqualTo("Query cannot be blank");
    }

    @Test
    @DisplayName("should fail when query is empty")
    void shouldFailWhenQueryIsEmpty() {
      StationsQueryParams params = buildRequest("");

      String message = singleMessage(validate(params));

      assertThat(message).isEqualTo("Query cannot be blank");
    }

    @Test
    @DisplayName("should fail when query is blank")
    void shouldFailWhenQueryIsBlank() {
      StationsQueryParams params = buildRequest("   ");

      String message = singleMessage(validate(params));

      assertThat(message).isEqualTo("Query cannot be blank");
    }

    @Test
    @DisplayName("should fail when query is only tab characters")
    void shouldFailWhenQueryIsOnlyTabs() {
      StationsQueryParams params = buildRequest("\t\t");

      String message = singleMessage(validate(params));

      assertThat(message).isEqualTo("Query cannot be blank");
    }

    @Test
    @DisplayName("should fail when query is only newline characters")
    void shouldFailWhenQueryIsOnlyNewlines() {
      StationsQueryParams params = buildRequest("\n\n");

      String message = singleMessage(validate(params));

      assertThat(message).isEqualTo("Query cannot be blank");
    }

    @Test
    @DisplayName("should report the violation on the query property path")
    void shouldReportViolationOnQueryPropertyPath() {
      StationsQueryParams params = buildRequest(null);

      ConstraintViolation<StationsQueryParams> violation = validate(params).iterator().next();

      assertThat(violation.getPropertyPath().toString()).isEqualTo("query");
    }

    @Test
    @DisplayName("should produce exactly one violation for a blank query")
    void shouldProduceExactlyOneViolationForBlankQuery() {
      StationsQueryParams params = buildRequest("   ");

      assertThat(validate(params)).hasSize(1);
    }

    @Test
    @DisplayName("should pass when query is built via all-args constructor")
    void shouldPassWhenBuiltWithAllArgsConstructor() {
      StationsQueryParams params = new StationsQueryParams("Lausanne");

      assertThat(validate(params)).isEmpty();
    }

    @Test
    @DisplayName("should fail when query is null via all-args constructor")
    void shouldFailWhenNullViaAllArgsConstructor() {
      StationsQueryParams params = new StationsQueryParams(null);

      String message = singleMessage(validate(params));

      assertThat(message).isEqualTo("Query cannot be blank");
    }
  }
}
