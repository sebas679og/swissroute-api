package com.group4.swissrouteapi.dtos.requests;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("StationRequest")
class StationRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Builder
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Builder")
  class BuilderTests {

    @Test
    @DisplayName("builder sets all fields correctly")
    void builder_setsAllFieldsCorrectly() {
      StationRequest request =
          StationRequest.builder().externalStationId("8503000").stationName("Zurich HB").build();

      assertThat(request.getExternalStationId()).isEqualTo("8503000");
      assertThat(request.getStationName()).isEqualTo("Zurich HB");
    }

    @Test
    @DisplayName("setter updates field values after construction")
    void setter_updatesFieldValues() {
      StationRequest request =
          StationRequest.builder().externalStationId("8503000").stationName("Zurich HB").build();

      request.setExternalStationId("8507000");
      request.setStationName("Bern");

      assertThat(request.getExternalStationId()).isEqualTo("8507000");
      assertThat(request.getStationName()).isEqualTo("Bern");
    }

    @Test
    @DisplayName("builder produces independent instances")
    void builder_producesIndependentInstances() {
      StationRequest a =
          StationRequest.builder().externalStationId("A").stationName("Station A").build();
      StationRequest b =
          StationRequest.builder().externalStationId("B").stationName("Station B").build();

      assertThat(a.getExternalStationId()).isNotEqualTo(b.getExternalStationId());
      assertThat(a.getStationName()).isNotEqualTo(b.getStationName());
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Validation — no violations
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Validation — no violations")
  class NoViolations {

    @Test
    @DisplayName("no violations when both fields are valid")
    void noViolations_whenBothFieldsAreValid() {
      StationRequest request =
          StationRequest.builder().externalStationId("8503000").stationName("Zurich HB").build();

      assertThat(validator.validate(request)).isEmpty();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Validation — externalStationId
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Validation — externalStationId")
  class ExternalStationIdValidation {

    @ParameterizedTest(name = "externalStationId = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("violation when externalStationId is null, empty, or blank")
    void violation_whenExternalStationIdIsNullEmptyOrBlank(String value) {
      StationRequest request =
          StationRequest.builder().externalStationId(value).stationName("Zurich HB").build();

      Set<ConstraintViolation<StationRequest>> violations = validator.validate(request);

      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("externalStationId"))
          .allMatch(v -> v.getMessage().equals("The station ID is required"));
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Validation — stationName
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Validation — stationName")
  class StationNameValidation {

    @ParameterizedTest(name = "stationName = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("violation when stationName is null, empty, or blank")
    void violation_whenStationNameIsNullEmptyOrBlank(String value) {
      StationRequest request =
          StationRequest.builder().externalStationId("8503000").stationName(value).build();

      Set<ConstraintViolation<StationRequest>> violations = validator.validate(request);

      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("stationName"))
          .allMatch(v -> v.getMessage().equals("The name of the station is required"));
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Validation — combined violations
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Validation — combined violations")
  class CombinedViolations {

    @Test
    @DisplayName("two violations when both fields are null")
    void twoViolations_whenBothFieldsAreNull() {
      StationRequest request = StationRequest.builder().build();

      assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    @DisplayName("two violations when both fields are empty strings")
    void twoViolations_whenBothFieldsAreEmptyStrings() {
      StationRequest request =
          StationRequest.builder().externalStationId("").stationName("").build();

      assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    @DisplayName("two violations when both fields are blank")
    void twoViolations_whenBothFieldsAreBlank() {
      StationRequest request =
          StationRequest.builder().externalStationId("   ").stationName("   ").build();

      assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    @DisplayName("violation messages are correct when both fields are invalid")
    void violationMessages_areCorrect_whenBothFieldsAreInvalid() {
      StationRequest request = StationRequest.builder().build();

      Set<ConstraintViolation<StationRequest>> violations = validator.validate(request);
      Set<String> messages =
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(java.util.stream.Collectors.toSet());

      assertThat(messages)
          .containsExactlyInAnyOrder(
              "The station ID is required", "The name of the station is required");
    }
  }
}
