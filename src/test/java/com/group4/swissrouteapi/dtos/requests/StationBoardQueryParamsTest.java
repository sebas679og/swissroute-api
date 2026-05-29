package com.group4.swissrouteapi.dtos.requests;

import static org.assertj.core.api.Assertions.assertThat;

import com.group4.swissrouteapi.utils.enums.TransportType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("StationBoardQueryParams")
class StationBoardQueryParamsTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Default values
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Default values")
  class DefaultValues {

    @Test
    @DisplayName("transportType defaults to an empty list when built without setting it")
    void transportType_defaultsToEmptyList_whenBuiltWithoutSettingIt() {
      StationBoardQueryParams params =
          StationBoardQueryParams.builder().station("Zurich HB").build();

      assertThat(params.getTransportType()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("no-args constructor initializes transportType to an empty list")
    void noArgsConstructor_initializesTransportType_toEmptyList() {
      StationBoardQueryParams params = new StationBoardQueryParams();

      assertThat(params.getTransportType()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("no-args constructor initializes optional fields to null")
    void noArgsConstructor_initializesOptionalFields_toNull() {
      StationBoardQueryParams params = new StationBoardQueryParams();

      assertThat(params.getStation()).isNull();
      assertThat(params.getId()).isNull();
      assertThat(params.getLimit()).isNull();
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
      StationBoardQueryParams params =
          StationBoardQueryParams.builder()
              .station("Bern")
              .id("8507000")
              .limit(10)
              .transportType(List.of(TransportType.TRAIN, TransportType.BUS))
              .build();

      assertThat(params.getStation()).isEqualTo("Bern");
      assertThat(params.getId()).isEqualTo("8507000");
      assertThat(params.getLimit()).isEqualTo(10);
      assertThat(params.getTransportType()).containsExactly(TransportType.TRAIN, TransportType.BUS);
    }

    @Test
    @DisplayName("builder allows overriding the default transportType with an explicit list")
    void builder_allowsOverridingDefaultTransportType() {
      List<TransportType> types = List.of(TransportType.TRAM, TransportType.SHIP);
      StationBoardQueryParams params =
          StationBoardQueryParams.builder().station("Zurich HB").transportType(types).build();

      assertThat(params.getTransportType()).containsExactly(TransportType.TRAM, TransportType.SHIP);
    }

    @Test
    @DisplayName("builder sets only station when other optional fields are omitted")
    void builder_setsOnlyStation_whenOtherFieldsAreOmitted() {
      StationBoardQueryParams params = StationBoardQueryParams.builder().station("Geneva").build();

      assertThat(params.getStation()).isEqualTo("Geneva");
      assertThat(params.getId()).isNull();
      assertThat(params.getLimit()).isNull();
    }

    @Test
    @DisplayName("setter updates fields after construction")
    void setter_updatesFields_afterConstruction() {
      StationBoardQueryParams params = new StationBoardQueryParams();
      params.setStation("Lausanne");
      params.setId("8501120");
      params.setLimit(5);
      params.setTransportType(List.of(TransportType.CABLEWAY));

      assertThat(params.getStation()).isEqualTo("Lausanne");
      assertThat(params.getId()).isEqualTo("8501120");
      assertThat(params.getLimit()).isEqualTo(5);
      assertThat(params.getTransportType()).containsExactly(TransportType.CABLEWAY);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Validation — no violations
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Validation — no violations")
  class NoViolations {

    @Test
    @DisplayName("no violations when station is set and optional fields are absent")
    void noViolations_whenStationIsSet_andOptionalFieldsAreAbsent() {
      StationBoardQueryParams params =
          StationBoardQueryParams.builder().station("Zurich HB").build();

      assertThat(validator.validate(params)).isEmpty();
    }

    @Test
    @DisplayName("no violations when all fields are set with valid values")
    void noViolations_whenAllFieldsAreSetWithValidValues() {
      StationBoardQueryParams params =
          StationBoardQueryParams.builder()
              .station("Bern")
              .id("8507000")
              .limit(15)
              .transportType(List.of(TransportType.TRAIN))
              .build();

      assertThat(validator.validate(params)).isEmpty();
    }

    @Test
    @DisplayName("no violations when id is null")
    void noViolations_whenIdIsNull() {
      StationBoardQueryParams params =
          StationBoardQueryParams.builder().station("Basel").id(null).build();

      assertThat(validator.validate(params)).isEmpty();
    }

    @Test
    @DisplayName("no violations when limit is null")
    void noViolations_whenLimitIsNull() {
      StationBoardQueryParams params =
          StationBoardQueryParams.builder().station("Basel").limit(null).build();

      assertThat(validator.validate(params)).isEmpty();
    }

    @Test
    @DisplayName("no violations when transportType list is empty")
    void noViolations_whenTransportTypeListIsEmpty() {
      StationBoardQueryParams params =
          StationBoardQueryParams.builder().station("Aarau").transportType(List.of()).build();

      assertThat(validator.validate(params)).isEmpty();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Validation — station constraint
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Validation — station")
  class StationValidation {

    @ParameterizedTest(name = "station = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("violation when station is null, empty, or blank")
    void violation_whenStationIsNullEmptyOrBlank(String value) {
      StationBoardQueryParams params = StationBoardQueryParams.builder().station(value).build();

      Set<ConstraintViolation<StationBoardQueryParams>> violations = validator.validate(params);

      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("station"))
          .allMatch(v -> v.getMessage().equals("Station is required"));
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Equality and hash code (@Data)
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Equality and hash code")
  class EqualityAndHashCode {

    @Test
    @DisplayName("two instances with the same field values are equal")
    void instancesWithSameValues_areEqual() {
      StationBoardQueryParams a =
          StationBoardQueryParams.builder()
              .station("Zurich HB")
              .id("8503000")
              .limit(10)
              .transportType(List.of(TransportType.TRAIN))
              .build();
      StationBoardQueryParams b =
          StationBoardQueryParams.builder()
              .station("Zurich HB")
              .id("8503000")
              .limit(10)
              .transportType(List.of(TransportType.TRAIN))
              .build();

      assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("two instances with the same field values have the same hash code")
    void instancesWithSameValues_haveSameHashCode() {
      StationBoardQueryParams a = StationBoardQueryParams.builder().station("Bern").build();
      StationBoardQueryParams b = StationBoardQueryParams.builder().station("Bern").build();

      assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("two instances with different stations are not equal")
    void instancesWithDifferentStations_areNotEqual() {
      StationBoardQueryParams a = StationBoardQueryParams.builder().station("Bern").build();
      StationBoardQueryParams b = StationBoardQueryParams.builder().station("Zurich HB").build();

      assertThat(a).isNotEqualTo(b);
    }
  }
}
