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
 * Unit tests for {@link RegisterRequest} bean validation constraints.
 *
 * <p>Uses the Jakarta Validation API directly — no Spring context required. Each nested class
 * isolates one field so failures are easy to locate.
 */
@DisplayName("RegisterRequest")
class RegisterRequestTest {

  private static Validator validator;

  /** A valid password that satisfies every {@code @ValidPassword} rule. */
  private static final String VALID_PASSWORD = "Secure@123";

  @BeforeAll
  static void setUpValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static final String VALID_NAME = "John Doe";
  private static final String VALID_EMAIL = "john.doe@example.com";
  private static final String VALID_CITY = "Bogotá";

  /**
   * Builds a {@link RegisterRequest} with explicit values for every field. Pass {@code null} to any
   * parameter to leave that field unset.
   */
  private RegisterRequest buildRequest(
      String name, String email, String password, String baseCity) {
    return RegisterRequest.builder()
        .name(name)
        .email(email)
        .password(password)
        .baseCity(baseCity)
        .build();
  }

  /** Builds a fully valid {@link RegisterRequest}. */
  private RegisterRequest validRequest() {
    return buildRequest(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, VALID_CITY);
  }

  private Set<ConstraintViolation<RegisterRequest>> validate(RegisterRequest request) {
    return validator.validate(request);
  }

  /** Returns the single violation message when exactly one violation is expected. */
  private String singleMessage(Set<ConstraintViolation<RegisterRequest>> violations) {
    assertThat(violations).hasSize(1);
    return violations.iterator().next().getMessage();
  }

  // ---------------------------------------------------------------------------
  // Happy path
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("should pass validation when all fields are valid")
  void shouldPassWhenAllFieldsAreValid() {
    assertThat(validate(validRequest())).isEmpty();
  }

  // ---------------------------------------------------------------------------
  // name
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("name field")
  class NameFieldTest {

    @Test
    @DisplayName("should fail when name is null")
    void shouldFailWhenNameIsNull() {
      RegisterRequest request = buildRequest(null, VALID_EMAIL, VALID_PASSWORD, VALID_CITY);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Name is required");
    }

    @Test
    @DisplayName("should fail when name is empty")
    void shouldFailWhenNameIsEmpty() {
      RegisterRequest request = buildRequest("", VALID_EMAIL, VALID_PASSWORD, VALID_CITY);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Name is required");
    }

    @Test
    @DisplayName("should fail when name is blank")
    void shouldFailWhenNameIsBlank() {
      RegisterRequest request = buildRequest("   ", VALID_EMAIL, VALID_PASSWORD, VALID_CITY);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Name is required");
    }

    @Test
    @DisplayName("should pass when name has a single non-blank character")
    void shouldPassWhenNameHasSingleCharacter() {
      RegisterRequest request = buildRequest("A", VALID_EMAIL, VALID_PASSWORD, VALID_CITY);

      assertThat(validate(request)).isEmpty();
    }
  }

  // ---------------------------------------------------------------------------
  // email
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("email field")
  class EmailFieldTest {

    @Test
    @DisplayName("should fail when email is null")
    void shouldFailWhenEmailIsNull() {
      RegisterRequest request = buildRequest(VALID_NAME, null, VALID_PASSWORD, VALID_CITY);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Email is required");
    }

    @Test
    @DisplayName("should fail when email is empty")
    void shouldFailWhenEmailIsEmpty() {
      RegisterRequest request = buildRequest(VALID_NAME, "", VALID_PASSWORD, VALID_CITY);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Email is required");
    }

    @Test
    @DisplayName("should fail when email is blank with both @NotBlank and @Email violations")
    void shouldFailWhenEmailIsBlank() {
      RegisterRequest request = buildRequest(VALID_NAME, "   ", VALID_PASSWORD, VALID_CITY);

      // Jakarta Validation fires both @NotBlank and @Email on blank strings
      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);
      Set<String> messages =
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(java.util.stream.Collectors.toSet());

      assertThat(violations).hasSize(2);
      assertThat(messages).containsExactlyInAnyOrder("Email is required", "Invalid email format");
    }

    @ParameterizedTest(name = "invalid email: \"{0}\"")
    @ValueSource(
        strings = {"notanemail", "missing@", "@nodomain.com", "two@@at.com", "space @domain.com"})
    @DisplayName("should fail when email format is invalid")
    void shouldFailWhenEmailFormatIsInvalid(String invalidEmail) {
      RegisterRequest request = buildRequest(VALID_NAME, invalidEmail, VALID_PASSWORD, VALID_CITY);

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
    }

    @ParameterizedTest(name = "valid email: \"{0}\"")
    @ValueSource(strings = {"user@example.com", "user+tag@sub.domain.org", "user.name@domain.co"})
    @DisplayName("should pass when email format is valid")
    void shouldPassWhenEmailFormatIsValid(String validEmail) {
      RegisterRequest request = buildRequest(VALID_NAME, validEmail, VALID_PASSWORD, VALID_CITY);

      assertThat(validate(request)).isEmpty();
    }
  }

  // ---------------------------------------------------------------------------
  // password
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("password field")
  class PasswordFieldTest {

    @Test
    @DisplayName("should fail when password is null")
    void shouldFailWhenPasswordIsNull() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, null, VALID_CITY);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Password is required");
    }

    @Test
    @DisplayName("should fail when password is empty")
    void shouldFailWhenPasswordIsEmpty() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "", VALID_CITY);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Password is required");
    }

    @Test
    @DisplayName("should fail when password is blank")
    void shouldFailWhenPasswordIsBlank() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "   ", VALID_CITY);

      // @NotBlank fires first; @ValidPassword treats blank as valid (delegates to @NotBlank)
      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Password is required");
    }

    @Test
    @DisplayName("should fail when password is shorter than 8 characters")
    void shouldFailWhenPasswordIsTooShort() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "Ab1@567", VALID_CITY);

      assertThat(validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("should fail when password has no uppercase letter")
    void shouldFailWhenPasswordHasNoUppercase() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "secure@123", VALID_CITY);

      assertThat(validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("should fail when password has no lowercase letter")
    void shouldFailWhenPasswordHasNoLowercase() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "SECURE@123", VALID_CITY);

      assertThat(validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("should fail when password has no digit")
    void shouldFailWhenPasswordHasNoDigit() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "Secure@abc", VALID_CITY);

      assertThat(validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("should fail when password has no special character")
    void shouldFailWhenPasswordHasNoSpecialCharacter() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "Secure1234", VALID_CITY);

      assertThat(validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("should fail when password contains whitespace")
    void shouldFailWhenPasswordContainsWhitespace() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, "Secure @123", VALID_CITY);

      assertThat(validate(request)).isNotEmpty();
    }

    @ParameterizedTest(name = "valid password: \"{0}\"")
    @ValueSource(strings = {"Secure@123", "P@ssw0rd!", "MyStr0ng#Pass", "C0mpl3x$Password"})
    @DisplayName("should pass when password satisfies all rules")
    void shouldPassWhenPasswordSatisfiesAllRules(String strongPassword) {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, strongPassword, VALID_CITY);

      assertThat(validate(request)).isEmpty();
    }
  }

  // ---------------------------------------------------------------------------
  // baseCity
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("baseCity field")
  class BaseCityFieldTest {

    @Test
    @DisplayName("should fail when baseCity is null")
    void shouldFailWhenBaseCityIsNull() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, null);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Base city is required");
    }

    @Test
    @DisplayName("should fail when baseCity is empty")
    void shouldFailWhenBaseCityIsEmpty() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, "");

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Base city is required");
    }

    @Test
    @DisplayName("should fail when baseCity is blank")
    void shouldFailWhenBaseCityIsBlank() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, "   ");

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Base city is required");
    }

    @Test
    @DisplayName("should pass when baseCity has a single non-blank character")
    void shouldPassWhenBaseCityHasSingleCharacter() {
      RegisterRequest request = buildRequest(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, "X");

      assertThat(validate(request)).isEmpty();
    }
  }
}
