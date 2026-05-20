package com.group4.swissrouteapi.controllers;

import static com.jayway.jsonpath.JsonPath.read;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

class AuthControllerTest extends AbstractIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PasswordEncoder passwordEncoder;

  private UserEntity user;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    UserEntity rawUser =
        UserEntity.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();
    user = userRepository.save(rawUser);
  }

  @Test
  public void shouldReturn201WhenUserIsSuccessfullyCreated() throws Exception {
    userRepository.deleteAll();
    RegisterRequest user1 =
        RegisterRequest.builder()
            .name(user.getName())
            .email(user.getEmail())
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(user.getBaseCity())
            .build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value(user1.getName()))
        .andExpect(jsonPath("$.email").value(user1.getEmail()))
        .andExpect(jsonPath("$.baseCity").value(user1.getBaseCity()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(
            result -> {
              String json = result.getResponse().getContentAsString();
              String createdAt = read(json, "$.createdAt");
              java.time.Instant parsed = java.time.Instant.parse(createdAt);
              assertNotNull(parsed);
            });
  }

  @Test
  public void shouldReturn409WhenEmailAlreadyExists() throws Exception {
    RegisterRequest user1 =
        RegisterRequest.builder()
            .name(user.getName())
            .email(user.getEmail())
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(user.getBaseCity())
            .build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.name()))
        .andExpect(jsonPath("$.description").value(containsStringIgnoringCase("email")))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenAllFieldsAreNull() throws Exception {
    RegisterRequest user1 = RegisterRequest.builder().build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenAllFieldsAreBlank() throws Exception {
    RegisterRequest user1 =
        RegisterRequest.builder().name("").email("").password("").baseCity("").build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenNameIsBlank() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name("")
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenNameIsNull() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(null)
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoAtSymbol() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email("invalidemail.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoDomain() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email("user@")
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoLocalPart() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email("@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasDoubleAt() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email("user@@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailContainsSpaces() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email("user name@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailIsPlainText() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email("plaintext")
            .password(UserDataProvider.VALID_PASSWORD)
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoUppercase() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("nouppercase1!")
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoLowercase() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("NOLOWERCASE1!")
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoDigit() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("NoDigits!!")
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoSpecialChar() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("NoSpecial1")
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordIsTooShort() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("Sh@rt1")
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordContainsWhitespace() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("Has White1!")
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
