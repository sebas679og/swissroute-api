package com.group4.swissrouteapi.utils.validators.passwordds;

import static org.assertj.core.api.Assertions.assertThat;

import com.group4.swissrouteapi.utils.validators.passwords.PasswordConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for password validation class. */
@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordConstraintValidator")
class PasswordConstraintValidatorTest {

  private PasswordConstraintValidator validator;

  @Mock private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new PasswordConstraintValidator();
  }

  // -------------------------------------------------------------------------
  // Cases where validation should be skipped (null / blank)
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("when password is null or blank")
  class WhenPasswordIsNullOrBlank {

    @ParameterizedTest(name = "password = [{0}]")
    @NullAndEmptySource
    @DisplayName("should return true (delegated to external @NotNull/@NotBlank)")
    void shouldReturnTrueForNullOrEmpty(String password) {
      assertThat(validator.isValid(password, context)).isTrue();
    }

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {" ", "   ", "\t", "\n", "  \t  "})
    @DisplayName("should return true when password contains only whitespace")
    void shouldReturnTrueForBlankPasswords(String password) {
      assertThat(validator.isValid(password, context)).isTrue();
    }
  }

  // -------------------------------------------------------------------------
  // Valid passwords
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("when password satisfies all rules")
  class WhenPasswordIsValid {

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(
        strings = {"Abcdef1!", "Secure@123", "MyP@ssw0rd", "C0mpl3x!Password", "Aa1!Aa1!Aa1!Aa1!"})
    @DisplayName("should return true")
    void shouldReturnTrueForValidPasswords(String password) {
      assertThat(validator.isValid(password, context)).isTrue();
    }
  }

  // -------------------------------------------------------------------------
  // Invalid passwords — one rule broken at a time
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("when password violates minimum length rule")
  class WhenLengthRuleViolated {

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"Ab1!", "Abc1!", "Abcd1!", "Abcde1!"})
    @DisplayName("should return false when shorter than 8 characters")
    void shouldReturnFalseWhenTooShort(String password) {
      assertThat(validator.isValid(password, context)).isFalse();
    }
  }

  @Nested
  @DisplayName("when password violates uppercase rule")
  class WhenUpperCaseRuleViolated {

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"abcdef1!", "password1!", "secure@123"})
    @DisplayName("should return false when it has no uppercase letters")
    void shouldReturnFalseWithoutUpperCase(String password) {
      assertThat(validator.isValid(password, context)).isFalse();
    }
  }

  @Nested
  @DisplayName("when password violates lowercase rule")
  class WhenLowerCaseRuleViolated {

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"ABCDEF1!", "PASSWORD1!", "SECURE@123"})
    @DisplayName("should return false when it has no lowercase letters")
    void shouldReturnFalseWithoutLowerCase(String password) {
      assertThat(validator.isValid(password, context)).isFalse();
    }
  }

  @Nested
  @DisplayName("when password violates digit rule")
  class WhenDigitRuleViolated {

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"Abcdefg!", "Password!", "Secure@abc"})
    @DisplayName("should return false when it has no digits")
    void shouldReturnFalseWithoutDigit(String password) {
      assertThat(validator.isValid(password, context)).isFalse();
    }
  }

  @Nested
  @DisplayName("when password violates special character rule")
  class WhenSpecialCharRuleViolated {

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"Abcdef12", "Password1", "Secure1234"})
    @DisplayName("should return false when it has no special characters")
    void shouldReturnFalseWithoutSpecialChar(String password) {
      assertThat(validator.isValid(password, context)).isFalse();
    }
  }

  @Nested
  @DisplayName("when password violates whitespace rule")
  class WhenWhitespaceRuleViolated {

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"Abcdef1! ", " Abcdef1!", "Abc def1!", "Abcdef1!\t"})
    @DisplayName("should return false when it contains whitespace")
    void shouldReturnFalseWithWhitespace(String password) {
      assertThat(validator.isValid(password, context)).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // Edge cases
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("edge cases")
  class EdgeCases {

    @Test
    @DisplayName("should return true for exactly 8 valid characters")
    void shouldReturnTrueForMinimumValidPassword() {
      assertThat(validator.isValid("Abcdef1!", context)).isTrue();
    }

    @Test
    @DisplayName("should return true for very long valid password")
    void shouldReturnTrueForVeryLongValidPassword() {
      String longPassword = "Secure@1" + "a".repeat(200);
      assertThat(validator.isValid(longPassword, context)).isTrue();
    }

    @Test
    @DisplayName("should return false when multiple rules are violated")
    void shouldReturnFalseWhenMultipleRulesViolated() {
      assertThat(validator.isValid("abc", context)).isFalse();
    }

    @Test
    @DisplayName("should return false for all-digit password")
    void shouldReturnFalseForAllDigits() {
      assertThat(validator.isValid("12345678", context)).isFalse();
    }

    @Test
    @DisplayName("should return false for all special characters password")
    void shouldReturnFalseForAllSpecialChars() {
      assertThat(validator.isValid("!@#$%^&*", context)).isFalse();
    }

    @Test
    @DisplayName("should never interact with validation context")
    void shouldNeverInteractWithContext() {
      validator.isValid("Abcdef1!", context);
      validator.isValid("invalid", context);
      validator.isValid(null, context);

      org.mockito.Mockito.verifyNoInteractions(context);
    }
  }
}
