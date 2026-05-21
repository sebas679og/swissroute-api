package com.group4.swissrouteapi.exceptions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class JsonWriterTest {

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private JsonWriter jsonWriter;

  @Mock private HttpServletResponse response;

  @Mock private ServletOutputStream outputStream;

  @BeforeEach
  void setUp() throws IOException {
    when(response.getOutputStream()).thenReturn(outputStream);
  }

  // ─── sendError ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("sendError() should set the HTTP status on the response")
  void sendError_shouldSetHttpStatus() throws IOException {
    jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "Authentication required");

    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("sendError() should set Content-Type to application/json")
  void sendError_shouldSetContentTypeToApplicationJson() throws IOException {
    jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "Authentication required");

    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("sendError() should write the ErrorResponse body to the response output stream")
  void sendError_shouldWriteErrorResponseToOutputStream() throws IOException {
    jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "Authentication required");

    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValue(eq(outputStream), captor.capture());

    ErrorResponse written = captor.getValue();
    assertThat(written.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(written.getName()).isEqualTo(HttpStatus.UNAUTHORIZED.name());
    assertThat(written.getDescription()).isEqualTo("Authentication required");
  }

  @Test
  @DisplayName("sendError() should build ErrorResponse with code matching the HTTP status value")
  void sendError_shouldSetCorrectCodeInBody() throws IOException {
    jsonWriter.sendError(response, HttpStatus.NOT_FOUND, "Resource not found");

    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValue(eq(outputStream), captor.capture());

    assertThat(captor.getValue().getCode()).isEqualTo(404);
  }

  @Test
  @DisplayName("sendError() should build ErrorResponse with name matching the HTTP status name")
  void sendError_shouldSetCorrectNameInBody() throws IOException {
    jsonWriter.sendError(response, HttpStatus.NOT_FOUND, "Resource not found");

    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValue(eq(outputStream), captor.capture());

    assertThat(captor.getValue().getName()).isEqualTo("NOT_FOUND");
  }

  @Test
  @DisplayName("sendError() should build ErrorResponse with the provided description")
  void sendError_shouldSetCorrectDescriptionInBody() throws IOException {
    jsonWriter.sendError(response, HttpStatus.FORBIDDEN, "Access denied");

    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValue(eq(outputStream), captor.capture());

    assertThat(captor.getValue().getDescription()).isEqualTo("Access denied");
  }

  @Test
  @DisplayName("sendError() should populate timestamp in the ErrorResponse body")
  void sendError_shouldPopulateTimestampInBody() throws IOException {
    jsonWriter.sendError(response, HttpStatus.BAD_REQUEST, "Invalid input");

    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValue(eq(outputStream), captor.capture());

    assertThat(captor.getValue().getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("sendError() should propagate IOException thrown by ObjectMapper")
  void sendError_whenObjectMapperThrows() throws IOException {
    doThrow(new IOException("Stream closed"))
        .when(objectMapper)
        .writeValue(any(ServletOutputStream.class), any(ErrorResponse.class));

    assertThatThrownBy(
            () ->
                jsonWriter.sendError(
                    response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"))
        .isInstanceOf(IOException.class)
        .hasMessage("Stream closed");
  }

  @Test
  @DisplayName("sendError() should propagate IOException thrown by getOutputStream()")
  void sendError_whenGetOutputStreamThrows() throws IOException {
    reset(response);
    when(response.getOutputStream()).thenThrow(new IOException("Output stream unavailable"));

    assertThatThrownBy(() -> jsonWriter.sendError(response, HttpStatus.SERVICE_UNAVAILABLE, "Down"))
        .isInstanceOf(IOException.class)
        .hasMessage("Output stream unavailable");
  }

  @Test
  @DisplayName("sendError() should invoke setStatus, setContentType and writeValue in any order")
  void sendError_shouldInvokeAllThreeResponseInteractions() throws IOException {
    jsonWriter.sendError(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");

    verify(response).setStatus(429);
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(objectMapper).writeValue(eq(outputStream), any(ErrorResponse.class));
    verifyNoMoreInteractions(objectMapper);
  }
}
