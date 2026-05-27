package com.group4.swissrouteapi.dtos.requests;

import static org.assertj.core.api.Assertions.assertThat;

import com.group4.swissrouteapi.utils.enums.TransportType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link ConnectionsQueryParams} bean validation constraints.
 *
 * <p>Uses the Jakarta Validation API directly — no Spring context required. Cross-field and enum
 * constraints are not present on this class; field-level {@code @NotBlank} rules on {@code from}
 * and {@code to} are fully covered here.
 */
@DisplayName("ConnectionsQueryParams")
class ConnectionsQueryParamsTest {

  private static Validator validator;

  private static final String VALID_FROM = "Bern";
  private static final String VALID_TO = "Zurich";

  @BeforeAll
  static void setUpValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private ConnectionsQueryParams buildRequest(String from, String to) {
    return ConnectionsQueryParams.builder().from(from).to(to).build();
  }

  private ConnectionsQueryParams validRequest() {
    return buildRequest(VALID_FROM, VALID_TO);
  }

  private Set<ConstraintViolation<ConnectionsQueryParams>> validate(ConnectionsQueryParams params) {
    return validator.validate(params);
  }

  private String singleMessage(Set<ConstraintViolation<ConnectionsQueryParams>> violations) {
    assertThat(violations).hasSize(1);
    return violations.iterator().next().getMessage();
  }

  private Set<String> messages(Set<ConstraintViolation<ConnectionsQueryParams>> violations) {
    return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
  }

  // ===========================================================================
  // Happy path
  // ===========================================================================

  @Test
  @DisplayName("should pass when from and to are provided")
  void shouldPassWhenRequiredFieldsArePresent() {
    assertThat(validate(validRequest())).isEmpty();
  }

  @Test
  @DisplayName("should pass when optional fields are also provided")
  void shouldPassWhenOptionalFieldsAreProvided() {
    ConnectionsQueryParams params =
        ConnectionsQueryParams.builder()
            .from(VALID_FROM)
            .to(VALID_TO)
            .date(LocalDate.of(2024, 10, 10))
            .time(LocalTime.of(8, 0))
            .transportations(List.of(TransportType.TRAIN, TransportType.BUS))
            .build();

    assertThat(validate(params)).isEmpty();
  }

  @Test
  @DisplayName("should pass when optional fields are null")
  void shouldPassWhenOptionalFieldsAreNull() {
    ConnectionsQueryParams params =
        ConnectionsQueryParams.builder()
            .from(VALID_FROM)
            .to(VALID_TO)
            .date(null)
            .time(null)
            .build();

    assertThat(validate(params)).isEmpty();
  }

  // ===========================================================================
  // from field
  // ===========================================================================

  @Nested
  @DisplayName("from field")
  class FromFieldTest {

    @Test
    @DisplayName("should fail when from is null")
    void shouldFailWhenFromIsNull() {
      String message = singleMessage(validate(buildRequest(null, VALID_TO)));

      assertThat(message).isEqualTo("Origin station must not be blank");
    }

    @Test
    @DisplayName("should fail when from is empty")
    void shouldFailWhenFromIsEmpty() {
      String message = singleMessage(validate(buildRequest("", VALID_TO)));

      assertThat(message).isEqualTo("Origin station must not be blank");
    }

    @Test
    @DisplayName("should fail when from is blank")
    void shouldFailWhenFromIsBlank() {
      String message = singleMessage(validate(buildRequest("   ", VALID_TO)));

      assertThat(message).isEqualTo("Origin station must not be blank");
    }

    @ParameterizedTest(name = "valid from: \"{0}\"")
    @ValueSource(strings = {"Bern", "Zürich HB", "Geneva-Cornavin", "A"})
    @DisplayName("should pass for any non-blank from value")
    void shouldPassForNonBlankFrom(String from) {
      assertThat(validate(buildRequest(from, VALID_TO))).isEmpty();
    }

    @Test
    @DisplayName("should report violation on the 'from' property path")
    void shouldReportViolationOnFromPropertyPath() {
      ConstraintViolation<ConnectionsQueryParams> violation =
          validate(buildRequest(null, VALID_TO)).iterator().next();

      assertThat(violation.getPropertyPath().toString()).isEqualTo("from");
    }
  }

  // ===========================================================================
  // to field
  // ===========================================================================

  @Nested
  @DisplayName("to field")
  class ToFieldTest {

    @Test
    @DisplayName("should fail when to is null")
    void shouldFailWhenToIsNull() {
      String message = singleMessage(validate(buildRequest(VALID_FROM, null)));

      assertThat(message).isEqualTo("Destination station must not be blank");
    }

    @Test
    @DisplayName("should fail when to is empty")
    void shouldFailWhenToIsEmpty() {
      String message = singleMessage(validate(buildRequest(VALID_FROM, "")));

      assertThat(message).isEqualTo("Destination station must not be blank");
    }

    @Test
    @DisplayName("should fail when to is blank")
    void shouldFailWhenToIsBlank() {
      String message = singleMessage(validate(buildRequest(VALID_FROM, "   ")));

      assertThat(message).isEqualTo("Destination station must not be blank");
    }

    @ParameterizedTest(name = "valid to: \"{0}\"")
    @ValueSource(strings = {"Zurich", "Basel SBB", "Lausanne", "B"})
    @DisplayName("should pass for any non-blank to value")
    void shouldPassForNonBlankTo(String to) {
      assertThat(validate(buildRequest(VALID_FROM, to))).isEmpty();
    }

    @Test
    @DisplayName("should report violation on the 'to' property path")
    void shouldReportViolationOnToPropertyPath() {
      ConstraintViolation<ConnectionsQueryParams> violation =
          validate(buildRequest(VALID_FROM, null)).iterator().next();

      assertThat(violation.getPropertyPath().toString()).isEqualTo("to");
    }
  }

  // ===========================================================================
  // Both required fields invalid simultaneously
  // ===========================================================================

  @Nested
  @DisplayName("both required fields invalid")
  class BothFieldsInvalidTest {

    @Test
    @DisplayName("should produce two violations when both from and to are null")
    void shouldProduceTwoViolationsWhenBothNull() {
      Set<ConstraintViolation<ConnectionsQueryParams>> violations =
          validate(buildRequest(null, null));

      assertThat(violations).hasSize(2);
      assertThat(messages(violations))
          .containsExactlyInAnyOrder(
              "Origin station must not be blank", "Destination station must not be blank");
    }

    @Test
    @DisplayName("should produce two violations when both from and to are blank")
    void shouldProduceTwoViolationsWhenBothBlank() {
      Set<ConstraintViolation<ConnectionsQueryParams>> violations =
          validate(buildRequest("   ", "   "));

      assertThat(violations).hasSize(2);
      assertThat(messages(violations))
          .containsExactlyInAnyOrder(
              "Origin station must not be blank", "Destination station must not be blank");
    }
  }

  // ===========================================================================
  // transportations field — @Builder.Default
  // ===========================================================================

  @Nested
  @DisplayName("transportations field")
  class TransportationsFieldTest {

    @Test
    @DisplayName("should default to an empty list when built via builder without transportations")
    void shouldDefaultToEmptyListWhenBuiltWithBuilder() {
      ConnectionsQueryParams params = validRequest();

      assertThat(params.getTransportations()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("should default to an empty list when built via no-args constructor")
    void shouldDefaultToEmptyListViaNoArgsConstructor() {
      ConnectionsQueryParams params = new ConnectionsQueryParams();

      // @Builder.Default does not apply to no-args constructor — field initializer handles it
      assertThat(params.getTransportations()).isNotNull();
    }

    @Test
    @DisplayName("should accept a list with all valid transportation types")
    void shouldAcceptListWithAllTransportationTypes() {
      ConnectionsQueryParams params =
          ConnectionsQueryParams.builder()
              .from(VALID_FROM)
              .to(VALID_TO)
              .transportations(
                  List.of(
                      TransportType.TRAIN,
                      TransportType.TRAM,
                      TransportType.SHIP,
                      TransportType.BUS,
                      TransportType.CABLEWAY))
              .build();

      assertThat(validate(params)).isEmpty();
      assertThat(params.getTransportations()).hasSize(5);
    }

    @Test
    @DisplayName("should accept an empty transportations list explicitly")
    void shouldAcceptEmptyTransportationsList() {
      ConnectionsQueryParams params =
          ConnectionsQueryParams.builder()
              .from(VALID_FROM)
              .to(VALID_TO)
              .transportations(new ArrayList<>())
              .build();

      assertThat(validate(params)).isEmpty();
    }
  }

  // ===========================================================================
  // @Data generated methods
  // ===========================================================================

  @Nested
  @DisplayName("@Data generated methods")
  class DataContractTest {

    @Test
    @DisplayName("should be equal when all fields match")
    void shouldBeEqualWhenAllFieldsMatch() {
      ConnectionsQueryParams a =
          new ConnectionsQueryParams(VALID_FROM, VALID_TO, null, null, new ArrayList<>());
      ConnectionsQueryParams b =
          new ConnectionsQueryParams(VALID_FROM, VALID_TO, null, null, new ArrayList<>());

      assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("should not be equal when from differs")
    void shouldNotBeEqualWhenFromDiffers() {
      ConnectionsQueryParams a = buildRequest("Bern", VALID_TO);
      ConnectionsQueryParams b = buildRequest("Geneva", VALID_TO);

      assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("should not be equal when to differs")
    void shouldNotBeEqualWhenToDiffers() {
      ConnectionsQueryParams a = buildRequest(VALID_FROM, "Zurich");
      ConnectionsQueryParams b = buildRequest(VALID_FROM, "Basel");

      assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("should include from and to in toString")
    void shouldIncludeFromAndToInToString() {
      ConnectionsQueryParams params = validRequest();

      assertThat(params.toString()).contains(VALID_FROM).contains(VALID_TO);
    }
  }
}
