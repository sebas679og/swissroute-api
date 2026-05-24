package com.group4.swissrouteapi.dtos.requests;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link LoginRequest} bean validation constraints.
 *
 * <p>Uses the Jakarta Validation API directly — no Spring context required. Each nested class
 * isolates one field so failures are easy to locate.
 */
@DisplayName("LoginRequest")
class LoginRequestTest {

  private static Validator validator;

  private static final String VALID_EMAIL = "john.doe@example.com";
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

  private LoginRequest buildRequest(String email, String password) {
    return LoginRequest.builder().email(email).password(password).build();
  }

  private LoginRequest validRequest() {
    return buildRequest(VALID_EMAIL, VALID_PASSWORD);
  }

  private Set<ConstraintViolation<LoginRequest>> validate(LoginRequest request) {
    return validator.validate(request);
  }

  private String singleMessage(Set<ConstraintViolation<LoginRequest>> violations) {
    assertThat(violations).hasSize(1);
    return violations.iterator().next().getMessage();
  }

  private Set<String> messages(Set<ConstraintViolation<LoginRequest>> violations) {
    return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
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
  // email
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("email field")
  class EmailFieldTest {

    @Test
    @DisplayName("should fail when email is null")
    void shouldFailWhenEmailIsNull() {
      LoginRequest request = buildRequest(null, VALID_PASSWORD);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Email is required");
    }

    @Test
    @DisplayName("should fail when email is empty")
    void shouldFailWhenEmailIsEmpty() {
      LoginRequest request = buildRequest("", VALID_PASSWORD);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Email is required");
    }

    @Test
    @DisplayName("should fail when email is blank with both @NotBlank and @Email violations")
    void shouldFailWhenEmailIsBlank() {
      LoginRequest request = buildRequest("   ", VALID_PASSWORD);

      // Jakarta Validation fires both @NotBlank and @Email on blank strings
      Set<ConstraintViolation<LoginRequest>> violations = validate(request);

      assertThat(violations).hasSize(2);
      assertThat(messages(violations))
          .containsExactlyInAnyOrder("Email is required", "Invalid email format");
    }

    @ParameterizedTest(name = "invalid email: \"{0}\"")
    @ValueSource(
        strings = {"notanemail", "missing@", "@nodomain.com", "two@@at.com", "space @domain.com"})
    @DisplayName("should fail when email format is invalid")
    void shouldFailWhenEmailFormatIsInvalid(String invalidEmail) {
      LoginRequest request = buildRequest(invalidEmail, VALID_PASSWORD);

      Set<ConstraintViolation<LoginRequest>> violations = validate(request);

      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
    }

    @ParameterizedTest(name = "valid email: \"{0}\"")
    @ValueSource(strings = {"user@example.com", "user+tag@sub.domain.org", "user.name@domain.co"})
    @DisplayName("should pass when email format is valid")
    void shouldPassWhenEmailFormatIsValid(String validEmail) {
      LoginRequest request = buildRequest(validEmail, VALID_PASSWORD);

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
      LoginRequest request = buildRequest(VALID_EMAIL, null);

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Password is required");
    }

    @Test
    @DisplayName("should fail when password is empty")
    void shouldFailWhenPasswordIsEmpty() {
      LoginRequest request = buildRequest(VALID_EMAIL, "");

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Password is required");
    }

    @Test
    @DisplayName("should fail when password is blank")
    void shouldFailWhenPasswordIsBlank() {
      LoginRequest request = buildRequest(VALID_EMAIL, "   ");

      String message = singleMessage(validate(request));

      assertThat(message).isEqualTo("Password is required");
    }

    @Test
    @DisplayName("should pass when password is any non-blank string")
    void shouldPassWhenPasswordIsAnyNonBlankString() {
      LoginRequest request = buildRequest(VALID_EMAIL, "any-value");

      assertThat(validate(request)).isEmpty();
    }
  }
}
