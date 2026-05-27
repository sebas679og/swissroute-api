package com.group4.swissrouteapi.dtos.requests;

import static org.assertj.core.api.Assertions.assertThat;

import com.group4.swissrouteapi.utils.enums.TransportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("RouteUpdateRequest")
class RouteUpdateRequestTest {

  // -------------------------------------------------------------------------
  // Builder / constructors
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Builder and constructors")
  class BuilderAndConstructors {

    @Test
    @DisplayName("no-args constructor initializes all fields to null")
    void noArgsConstructor_initializesAllFieldsToNull() {
      RouteUpdateRequest request = new RouteUpdateRequest();

      assertThat(request.getName()).isNull();
      assertThat(request.getOrigin()).isNull();
      assertThat(request.getDestination()).isNull();
      assertThat(request.getTransportType()).isNull();
    }

    @Test
    @DisplayName("builder sets all fields correctly")
    void builder_setsAllFieldsCorrectly() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name("Night Train")
              .origin("Zurich")
              .destination("Bern")
              .transportType(TransportType.TRAIN)
              .build();

      assertThat(request.getName()).isEqualTo("Night Train");
      assertThat(request.getOrigin()).isEqualTo("Zurich");
      assertThat(request.getDestination()).isEqualTo("Bern");
      assertThat(request.getTransportType()).isEqualTo(TransportType.TRAIN);
    }

    @Test
    @DisplayName("all-args constructor assigns fields in declaration order")
    void allArgsConstructor_assignsFieldsCorrectly() {
      RouteUpdateRequest request =
          new RouteUpdateRequest("Express", "Geneva", "Lausanne", TransportType.TRAM);

      assertThat(request.getName()).isEqualTo("Express");
      assertThat(request.getOrigin()).isEqualTo("Geneva");
      assertThat(request.getDestination()).isEqualTo("Lausanne");
      assertThat(request.getTransportType()).isEqualTo(TransportType.TRAM);
    }

    @Test
    @DisplayName("setters update field values after construction")
    void setters_updateFieldValues() {
      RouteUpdateRequest request = new RouteUpdateRequest();
      request.setName("Updated Name");
      request.setOrigin("Basel");
      request.setDestination("Zurich");
      request.setTransportType(TransportType.BUS);

      assertThat(request.getName()).isEqualTo("Updated Name");
      assertThat(request.getOrigin()).isEqualTo("Basel");
      assertThat(request.getDestination()).isEqualTo("Zurich");
      assertThat(request.getTransportType()).isEqualTo(TransportType.BUS);
    }
  }

  // -------------------------------------------------------------------------
  // isEmpty() — returns true
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("isEmpty() — returns true")
  class IsEmptyTrue {

    @Test
    @DisplayName("returns true when all fields are null")
    void returnsTrue_whenAllFieldsAreNull() {
      RouteUpdateRequest request = new RouteUpdateRequest();

      assertThat(request.isEmpty()).isTrue();
    }

    @Test
    @DisplayName(
        "returns true when all string fields are empty strings and transportationType is null")
    void returnsTrue_whenAllStringFieldsAreEmptyStrings() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name("")
              .origin("")
              .destination("")
              .transportType(null)
              .build();

      assertThat(request.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("returns true when all string fields are blank and transportationType is null")
    void returnsTrue_whenAllStringFieldsAreBlank() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name("   ")
              .origin("   ")
              .destination("   ")
              .transportType(null)
              .build();

      assertThat(request.isEmpty()).isTrue();
    }

    @ParameterizedTest(name = "blank name [{0}]")
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("returns true when only name is blank and other fields are null")
    void returnsTrue_whenOnlyNameIsBlankAndOtherFieldsAreNull(String blank) {
      RouteUpdateRequest request = RouteUpdateRequest.builder().name(blank).build();

      assertThat(request.isEmpty()).isTrue();
    }

    @ParameterizedTest(name = "blank origin [{0}]")
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("returns true when only origin is blank and other fields are null")
    void returnsTrue_whenOnlyOriginIsBlankAndOtherFieldsAreNull(String blank) {
      RouteUpdateRequest request = RouteUpdateRequest.builder().origin(blank).build();

      assertThat(request.isEmpty()).isTrue();
    }

    @ParameterizedTest(name = "blank destination [{0}]")
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("returns true when only destination is blank and other fields are null")
    void returnsTrue_whenOnlyDestinationIsBlankAndOtherFieldsAreNull(String blank) {
      RouteUpdateRequest request = RouteUpdateRequest.builder().destination(blank).build();

      assertThat(request.isEmpty()).isTrue();
    }
  }

  // -------------------------------------------------------------------------
  // isEmpty() — returns false
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("isEmpty() — returns false")
  class IsEmptyFalse {

    @Test
    @DisplayName("returns false when only name has a non-blank value")
    void returnsFalse_whenOnlyNameIsPresent() {
      RouteUpdateRequest request = RouteUpdateRequest.builder().name("Night Train").build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("returns false when only origin has a non-blank value")
    void returnsFalse_whenOnlyOriginIsPresent() {
      RouteUpdateRequest request = RouteUpdateRequest.builder().origin("Zurich").build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("returns false when only destination has a non-blank value")
    void returnsFalse_whenOnlyDestinationIsPresent() {
      RouteUpdateRequest request = RouteUpdateRequest.builder().destination("Bern").build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("returns false when only transportationType is set")
    void returnsFalse_whenOnlyTransportationTypeIsSet() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder().transportType(TransportType.SHIP).build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("returns false when all fields have meaningful values")
    void returnsFalse_whenAllFieldsHaveMeaningfulValues() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name("Express")
              .origin("Geneva")
              .destination("Lausanne")
              .transportType(TransportType.TRAM)
              .build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("returns false when name is non-blank and all other fields are null")
    void returnsFalse_whenNameIsNonBlankAndOtherFieldsAreNull() {
      RouteUpdateRequest request = RouteUpdateRequest.builder().name("A").build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("returns false when origin is non-blank even if other string fields are blank")
    void returnsFalse_whenOriginIsNonBlankAndOtherStringFieldsAreBlank() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name("   ")
              .origin("Basel")
              .destination("")
              .transportType(null)
              .build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName(
        "returns false when destination is non-blank even if other string fields are blank")
    void returnsFalse_whenDestinationIsNonBlankAndOtherStringFieldsAreBlank() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name(null)
              .origin("   ")
              .destination("Zurich")
              .transportType(null)
              .build();

      assertThat(request.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("returns false when transportationType is set even if all string fields are blank")
    void returnsFalse_whenTransportationTypeIsSetAndAllStringFieldsAreBlank() {
      RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name("   ")
              .origin("")
              .destination(null)
              .transportType(TransportType.CABLEWAY)
              .build();

      assertThat(request.isEmpty()).isFalse();
    }
  }
}
