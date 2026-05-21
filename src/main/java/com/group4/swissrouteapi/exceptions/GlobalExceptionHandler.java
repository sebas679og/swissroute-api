package com.group4.swissrouteapi.exceptions;

import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler
 *
 * <p>Centralized exception handling component for REST controllers.
 *
 * <p>Annotated with {@link RestControllerAdvice} to intercept exceptions across the application and
 * provide consistent error responses.
 *
 * <p>Defines handlers for custom exceptions such as {@link ResourceConflictException} and
 * validation-related exceptions like {@link MethodArgumentNotValidException}. Builds standardized
 * {@link ErrorResponse} objects with appropriate HTTP status codes and descriptive messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ErrorResponse> handleResourceConflictException(
      ResourceConflictException ex) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  /**
   * Handles validation errors thrown when request parameters fail to meet defined constraints.
   *
   * <p>Extracts error messages from the {@link MethodArgumentNotValidException}, normalizes
   * whitespace, and formats field-specific errors as {@code field: message}. Concatenates all
   * messages into a single descriptive string separated by semicolons.
   *
   * <p>Returns a {@link ResponseEntity} with an {@link ErrorResponse} body and {@code BAD_REQUEST}
   * (HTTP 400) status.
   *
   * @param ex the exception containing validation errors
   * @return a response entity with error details and HTTP 400 status
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> customMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String description =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  String message = error.getDefaultMessage();
                  if (message != null) {
                    message = message.replaceAll("\\s+", " ").trim();
                  }
                  if (error instanceof FieldError fieldError) {
                    return fieldError.getField() + ": " + message;
                  }
                  return message;
                })
            .filter(msg -> msg != null && !msg.isBlank())
            .collect(Collectors.joining("; "));

    return buildErrorResponse(HttpStatus.BAD_REQUEST, description);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(BadGatewayException.class)
  public ResponseEntity<ErrorResponse> handleBadGatewayException(BadGatewayException ex) {
    return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleServiceUnavailableException(
      ServiceUnavailableException ex) {
    return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String description) {
    return ResponseEntity.status(status)
        .body(
            ErrorResponse.builder()
                .code(status.value())
                .name(status.name())
                .description(description)
                .build());
  }
}
