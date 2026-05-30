package com.group4.swissrouteapi.dtos.requests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.group4.swissrouteapi.utils.deserializer.TransportationTypeDeserializer;
import com.group4.swissrouteapi.utils.enums.TransportType;
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

@DisplayName("FavoriteRouteRequest")
class FavoriteRouteRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Bean Validation
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Validation")
  class ValidationTests {

    private FavoriteRouteRequest validRequest() {
      return FavoriteRouteRequest.builder()
          .name("Zurich–Bern Express")
          .origin("Zurich HB")
          .destination("Bern")
          .transportType(TransportType.TRAIN)
          .build();
    }

    @Test
    @DisplayName("no violations when all required fields are present")
    void noViolations_whenAllRequiredFieldsArePresent() {
      Set<ConstraintViolation<FavoriteRouteRequest>> violations =
          validator.validate(validRequest());

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("no violations when transportationType is null")
    void noViolations_whenTransportationTypeIsNull() {
      FavoriteRouteRequest request =
          FavoriteRouteRequest.builder()
              .name("Route A")
              .origin("Geneva")
              .destination("Lausanne")
              .transportType(null)
              .build();

      assertThat(validator.validate(request)).isEmpty();
    }

    // ── name ──────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "name = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("violation on name when blank or null")
    void violation_whenNameIsBlankOrNull(String name) {
      FavoriteRouteRequest request =
          FavoriteRouteRequest.builder()
              .name(name)
              .origin("Geneva")
              .destination("Lausanne")
              .build();

      Set<ConstraintViolation<FavoriteRouteRequest>> violations = validator.validate(request);

      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("name"))
          .allMatch(v -> v.getMessage().equals("Name is required"));
    }

    // ── origin ────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "origin = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("violation on origin when blank or null")
    void violation_whenOriginIsBlankOrNull(String origin) {
      FavoriteRouteRequest request =
          FavoriteRouteRequest.builder()
              .name("Route A")
              .origin(origin)
              .destination("Lausanne")
              .build();

      Set<ConstraintViolation<FavoriteRouteRequest>> violations = validator.validate(request);

      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("origin"))
          .allMatch(v -> v.getMessage().equals("Origin is required"));
    }

    // ── destination ───────────────────────────────────────────────────────

    @ParameterizedTest(name = "destination = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("violation on destination when blank or null")
    void violation_whenDestinationIsBlankOrNull(String destination) {
      FavoriteRouteRequest request =
          FavoriteRouteRequest.builder()
              .name("Route A")
              .origin("Geneva")
              .destination(destination)
              .build();

      Set<ConstraintViolation<FavoriteRouteRequest>> violations = validator.validate(request);

      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("destination"))
          .allMatch(v -> v.getMessage().equals("Destination is required"));
    }

    // ── multiple violations ───────────────────────────────────────────────

    @Test
    @DisplayName("three violations when name, origin, and destination are all blank")
    void threeViolations_whenAllRequiredFieldsAreBlank() {
      FavoriteRouteRequest request =
          FavoriteRouteRequest.builder().name("").origin("").destination("").build();

      assertThat(validator.validate(request)).hasSize(3);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // TransportationTypeDeserializer
  //
  // The deserializer is tested directly against TransportationType — not
  // through FavoriteRouteRequest — to avoid any dependency on Jackson being
  // able to instantiate the DTO (which requires @NoArgsConstructor or a
  // @JsonCreator that is not present on the production class).
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("TransportationTypeDeserializer")
  class TransportTypeDeserializerTests {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
      SimpleModule module = new SimpleModule();
      module.addDeserializer(TransportType.class, new TransportationTypeDeserializer());
      objectMapper = new ObjectMapper().registerModule(module);
    }

    // Wraps the raw string in JSON quotes so Jackson treats it as a JSON string token.
    private TransportType deserialize(String value) throws JsonProcessingException {
      return objectMapper.readValue("\"" + value + "\"", TransportType.class);
    }

    // ── valid values ──────────────────────────────────────────────────────

    @ParameterizedTest(name = "exact uppercase [{0}]")
    @ValueSource(strings = {"TRAIN", "TRAM", "SHIP", "BUS", "CABLEWAY"})
    @DisplayName("deserializes all exact uppercase enum names correctly")
    void deserializesExactUppercaseValues_correctly(String value) throws JsonProcessingException {
      assertThat(deserialize(value)).isEqualTo(TransportType.valueOf(value));
    }

    @ParameterizedTest(name = "lowercase [{0}]")
    @ValueSource(strings = {"train", "tram", "ship", "bus", "cableway"})
    @DisplayName("deserializes lowercase values by normalizing to uppercase")
    void deserializesLowercaseValues_byNormalizingToUppercase(String value)
        throws JsonProcessingException {
      assertThat(deserialize(value)).isEqualTo(TransportType.valueOf(value.toUpperCase()));
    }

    @ParameterizedTest(name = "mixed-case [{0}]")
    @ValueSource(strings = {"Train", "tRaIn", "sHip", "Bus", "CableWay"})
    @DisplayName("deserializes mixed-case values by normalizing to uppercase")
    void deserializesMixedCaseValues_byNormalizingToUppercase(String value)
        throws JsonProcessingException {
      assertThat(deserialize(value)).isEqualTo(TransportType.valueOf(value.trim().toUpperCase()));
    }

    @ParameterizedTest(name = "padded [{0}]")
    @ValueSource(strings = {" TRAIN", "TRAM ", " BUS "})
    @DisplayName("deserializes values with surrounding whitespace by trimming first")
    void deserializesValues_withSurroundingWhitespace_byTrimming(String value)
        throws JsonProcessingException {
      assertThat(deserialize(value)).isEqualTo(TransportType.valueOf(value.trim()));
    }

    // ── null / blank → null ───────────────────────────────────────────────

    @Test
    @DisplayName("returns null when value is an empty string")
    void returnsNull_whenValueIsEmptyString() throws JsonProcessingException {
      assertThat(deserialize("")).isNull();
    }

    @Test
    @DisplayName("returns null when value is a blank string with spaces")
    void returnsNull_whenValueIsBlankWithSpaces() throws JsonProcessingException {
      // Uses objectMapper.writeValueAsString() to produce valid JSON-escaped strings,
      // since raw \t and \n are illegal unquoted control characters in JSON.
      String json = objectMapper.writeValueAsString("   ");
      assertThat(objectMapper.readValue(json, TransportType.class)).isNull();
    }

    // ── invalid values ────────────────────────────────────────────────────

    @ParameterizedTest(name = "invalid [{0}]")
    @ValueSource(strings = {"PLANE", "SUBWAY", "CAR", "BICYCLE", "UNKNOWN"})
    @DisplayName("throws InvalidFormatException for unrecognized transportation type values")
    void throwsInvalidFormatException_forUnrecognizedValues(String invalid) {
      assertThatThrownBy(() -> deserialize(invalid)).isInstanceOf(InvalidFormatException.class);
    }

    @Test
    @DisplayName("InvalidFormatException message includes the valid enum values")
    void invalidFormatException_messageIncludesValidEnumValues() {
      // The deserializer passes Arrays.toString(TransportationType.values()) as the
      // message template, so the message contains the valid values, not the bad input.
      assertThatThrownBy(() -> deserialize("HELICOPTER"))
          .isInstanceOf(InvalidFormatException.class)
          .hasMessageContaining("TRAIN");
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
      FavoriteRouteRequest request =
          FavoriteRouteRequest.builder()
              .name("Night Train")
              .origin("Zurich")
              .destination("Vienna")
              .transportType(TransportType.TRAIN)
              .build();

      assertThat(request.getName()).isEqualTo("Night Train");
      assertThat(request.getOrigin()).isEqualTo("Zurich");
      assertThat(request.getDestination()).isEqualTo("Vienna");
      assertThat(request.getTransportType()).isEqualTo(TransportType.TRAIN);
    }

    @Test
    @DisplayName("builder produces independent instances")
    void builder_producesIndependentInstances() {
      FavoriteRouteRequest a =
          FavoriteRouteRequest.builder().name("A").origin("X").destination("Y").build();
      FavoriteRouteRequest b =
          FavoriteRouteRequest.builder().name("B").origin("X").destination("Y").build();

      assertThat(a.getName()).isNotEqualTo(b.getName());
    }
  }
}
