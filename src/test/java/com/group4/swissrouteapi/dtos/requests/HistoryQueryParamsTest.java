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

@DisplayName("HistoryQueryParams")
class HistoryQueryParamsTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  // -------------------------------------------------------------------------
  // Constants
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Constants")
  class Constants {

    @Test
    @DisplayName("DEFAULT_PAGE should be 1")
    void defaultPage_shouldBeOne() {
      assertThat(HistoryQueryParams.DEFAULT_PAGE).isEqualTo(1);
    }

    @Test
    @DisplayName("DEFAULT_SIZE should be 20")
    void defaultSize_shouldBeTwenty() {
      assertThat(HistoryQueryParams.DEFAULT_SIZE).isEqualTo(20);
    }
  }

  // -------------------------------------------------------------------------
  // Default construction (no-args constructor)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("No-args constructor")
  class NoArgsConstructor {

    @Test
    @DisplayName("page defaults to DEFAULT_PAGE when built with no-args constructor")
    void page_defaultsToDefaultPage() {
      HistoryQueryParams params = new HistoryQueryParams();
      assertThat(params.getPage()).isEqualTo(HistoryQueryParams.DEFAULT_PAGE);
    }

    @Test
    @DisplayName("size defaults to DEFAULT_SIZE when built with no-args constructor")
    void size_defaultsToDefaultSize() {
      HistoryQueryParams params = new HistoryQueryParams();
      assertThat(params.getSize()).isEqualTo(HistoryQueryParams.DEFAULT_SIZE);
    }
  }

  // -------------------------------------------------------------------------
  // Builder — default values
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Builder — default values")
  class BuilderDefaults {

    @Test
    @DisplayName("page defaults to DEFAULT_PAGE when not set in builder")
    void page_defaultsToDefaultPage_whenNotExplicitlySet() {
      HistoryQueryParams params = HistoryQueryParams.builder().build();
      assertThat(params.getPage()).isEqualTo(HistoryQueryParams.DEFAULT_PAGE);
    }

    @Test
    @DisplayName("size defaults to DEFAULT_SIZE when not set in builder")
    void size_defaultsToDefaultSize_whenNotExplicitlySet() {
      HistoryQueryParams params = HistoryQueryParams.builder().build();
      assertThat(params.getSize()).isEqualTo(HistoryQueryParams.DEFAULT_SIZE);
    }
  }

  // -------------------------------------------------------------------------
  // Builder — explicit values
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Builder — explicit values")
  class BuilderExplicitValues {

    @Test
    @DisplayName("page is set to provided value when explicitly specified")
    void page_isSetToProvidedValue() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(3).build();
      assertThat(params.getPage()).isEqualTo(3);
    }

    @Test
    @DisplayName("size is set to provided value when explicitly specified")
    void size_isSetToProvidedValue() {
      HistoryQueryParams params = HistoryQueryParams.builder().size(10).build();
      assertThat(params.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("both page and size are set to provided values")
    void pageAndSize_areSetToProvidedValues() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(5).size(50).build();
      assertThat(params.getPage()).isEqualTo(5);
      assertThat(params.getSize()).isEqualTo(50);
    }
  }

  // -------------------------------------------------------------------------
  // Getter null-safety fallback
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Getter null-safety fallback")
  class GetterNullSafetyFallback {

    @Test
    @DisplayName("getPage() returns DEFAULT_PAGE when internal page field is null")
    void getPage_returnsDefaultPage_whenFieldIsNull() {
      HistoryQueryParams params = new HistoryQueryParams();
      params.setPage(null);
      assertThat(params.getPage()).isEqualTo(HistoryQueryParams.DEFAULT_PAGE);
    }

    @Test
    @DisplayName("getSize() returns DEFAULT_SIZE when internal size field is null")
    void getSize_returnsDefaultSize_whenFieldIsNull() {
      HistoryQueryParams params = new HistoryQueryParams();
      params.setSize(null);
      assertThat(params.getSize()).isEqualTo(HistoryQueryParams.DEFAULT_SIZE);
    }

    @Test
    @DisplayName("getPage() returns explicitly set value when field is non-null")
    void getPage_returnsSetValue_whenFieldIsNonNull() {
      HistoryQueryParams params = new HistoryQueryParams();
      params.setPage(7);
      assertThat(params.getPage()).isEqualTo(7);
    }

    @Test
    @DisplayName("getSize() returns explicitly set value when field is non-null")
    void getSize_returnsSetValue_whenFieldIsNonNull() {
      HistoryQueryParams params = new HistoryQueryParams();
      params.setSize(25);
      assertThat(params.getSize()).isEqualTo(25);
    }
  }

  // -------------------------------------------------------------------------
  // Bean Validation — page constraint
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Validation — page")
  class PageValidation {

    @Test
    @DisplayName("no violations when page is 1 (minimum boundary)")
    void page_noViolations_atMinimumBoundary() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(1).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("no violations when page is a large valid number")
    void page_noViolations_forLargeValidNumber() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(1000).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("violation when page is 0")
    void page_violation_whenZero() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(0).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).hasSize(1);
      ConstraintViolation<HistoryQueryParams> violation = violations.iterator().next();
      assertThat(violation.getPropertyPath().toString()).isEqualTo("page");
      assertThat(violation.getMessage()).isEqualTo("Page must be greater than or equal to 1");
    }

    @Test
    @DisplayName("violation when page is negative")
    void page_violation_whenNegative() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(-5).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("page"));
    }
  }

  // -------------------------------------------------------------------------
  // Bean Validation — size constraint
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Validation — size")
  class SizeValidation {

    @Test
    @DisplayName("no violations when size is 1 (minimum boundary)")
    void size_noViolations_atMinimumBoundary() {
      HistoryQueryParams params = HistoryQueryParams.builder().size(1).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("no violations when size is 50 (maximum boundary)")
    void size_noViolations_atMaximumBoundary() {
      HistoryQueryParams params = HistoryQueryParams.builder().size(50).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("no violations when size is 20 (default value)")
    void size_noViolations_atDefaultValue() {
      HistoryQueryParams params = HistoryQueryParams.builder().size(20).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("violation when size is 0")
    void size_violation_whenZero() {
      HistoryQueryParams params = HistoryQueryParams.builder().size(0).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).hasSize(1);
      ConstraintViolation<HistoryQueryParams> violation = violations.iterator().next();
      assertThat(violation.getPropertyPath().toString()).isEqualTo("size");
      assertThat(violation.getMessage()).isEqualTo("Size must be greater than or equal to 1");
    }

    @Test
    @DisplayName("violation when size is negative")
    void size_violation_whenNegative() {
      HistoryQueryParams params = HistoryQueryParams.builder().size(-1).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations)
          .hasSize(1)
          .allMatch(v -> v.getPropertyPath().toString().equals("size"));
    }

    @Test
    @DisplayName("violation when size exceeds 50")
    void size_violation_whenAboveMaximum() {
      HistoryQueryParams params = HistoryQueryParams.builder().size(51).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).hasSize(1);
      ConstraintViolation<HistoryQueryParams> violation = violations.iterator().next();
      assertThat(violation.getPropertyPath().toString()).isEqualTo("size");
      assertThat(violation.getMessage()).isEqualTo("Size must be less than or equal to 50");
    }
  }

  // -------------------------------------------------------------------------
  // Bean Validation — combined constraints
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Validation — combined constraints")
  class CombinedValidation {

    @Test
    @DisplayName("two violations when both page and size are invalid")
    void twoViolations_whenBothPageAndSizeAreInvalid() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(0).size(0).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).hasSize(2);
    }

    @Test
    @DisplayName("no violations with valid page and size combination")
    void noViolations_withValidPageAndSizeCombination() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(2).size(30).build();
      Set<ConstraintViolation<HistoryQueryParams>> violations = validator.validate(params);
      assertThat(violations).isEmpty();
    }
  }

  // -------------------------------------------------------------------------
  // All-args constructor
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("All-args constructor")
  class AllArgsConstructor {

    @Test
    @DisplayName("assigns page and size correctly when using all-args constructor")
    void allArgsConstructor_assignsFieldsCorrectly() {
      HistoryQueryParams params = new HistoryQueryParams(4, 15);
      assertThat(params.getPage()).isEqualTo(4);
      assertThat(params.getSize()).isEqualTo(15);
    }
  }

  // -------------------------------------------------------------------------
  // Lombok-generated equals and hashCode
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Equality and hash code")
  class EqualityAndHashCode {

    @Test
    @DisplayName("two instances with same page and size are equal")
    void instancesWithSameValues_areEqual() {
      HistoryQueryParams a = HistoryQueryParams.builder().page(1).size(20).build();
      HistoryQueryParams b = HistoryQueryParams.builder().page(1).size(20).build();
      assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("two instances with same page and size have the same hash code")
    void instancesWithSameValues_haveSameHashCode() {
      HistoryQueryParams a = HistoryQueryParams.builder().page(1).size(20).build();
      HistoryQueryParams b = HistoryQueryParams.builder().page(1).size(20).build();
      assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("two instances with different values are not equal")
    void instancesWithDifferentValues_areNotEqual() {
      HistoryQueryParams a = HistoryQueryParams.builder().page(1).size(20).build();
      HistoryQueryParams b = HistoryQueryParams.builder().page(2).size(10).build();
      assertThat(a).isNotEqualTo(b);
    }
  }
}
