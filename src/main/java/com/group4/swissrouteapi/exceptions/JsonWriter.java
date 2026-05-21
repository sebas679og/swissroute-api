package com.group4.swissrouteapi.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * JsonWriter Utility component responsible for writing JSON-formatted responses. Provides methods
 * to send standardized error messages to clients using the {@link HttpServletResponse}.
 */
@Component
@RequiredArgsConstructor
public class JsonWriter {

  private final ObjectMapper objectMapper;

  /**
   * Sends an error response in JSON format to the client.
   *
   * <p>Builds an {@link ErrorResponse} object containing the HTTP status code, status name, and a
   * descriptive error message. The response is written to the output stream with content type
   * {@code application/json}.
   *
   * @param response the {@link HttpServletResponse} to write the error to
   * @param status the {@link HttpStatus} representing the error type
   * @param message the descriptive error message
   * @throws IOException if an I/O error occurs while writing the response
   */
  public void sendError(HttpServletResponse response, HttpStatus status, String message)
      throws IOException {
    ErrorResponse body =
        ErrorResponse.builder()
            .code(status.value())
            .name(status.name())
            .description(message)
            .build();

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
