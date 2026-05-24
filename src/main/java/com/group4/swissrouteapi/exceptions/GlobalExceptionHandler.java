package com.group4.swissrouteapi.exceptions;

import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import java.util.stream.Collectors;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    return handleBindingErrors(ex.getBindingResult());
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
    return handleBindingErrors(ex.getBindingResult());
  }

  private ResponseEntity<ErrorResponse> handleBindingErrors(BindingResult bindingResult) {
    String description =
        bindingResult.getAllErrors().stream()
            .map(
                error -> {
                  if (error instanceof FieldError fieldError) {
                    String field = fieldError.getField();
                    Object rejectedValue = fieldError.getRejectedValue();

                    if (fieldError.contains(TypeMismatchException.class)) {
                      TypeMismatchException typeMismatch =
                          fieldError.unwrap(TypeMismatchException.class);
                      Throwable cause = typeMismatch.getCause();
                      if (cause instanceof IllegalArgumentException) {
                        return cause.getMessage();
                      }
                      return "Field '%s': invalid value '%s'".formatted(field, rejectedValue);
                    }

                    String message = fieldError.getDefaultMessage();
                    if (message != null) {
                      message = message.replaceAll("\\s+", " ").trim();
                    }
                    return field + ": " + (message != null ? message : "invalid value");
                  }
                  return error.getDefaultMessage();
                })
            .filter(msg -> msg != null && !msg.isBlank())
            .collect(Collectors.joining("; "));

    return buildErrorResponse(HttpStatus.BAD_REQUEST, description);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
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
