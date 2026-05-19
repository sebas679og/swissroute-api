package com.group4.swissrouteapi.exceptions;

import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ErrorResponse> handleResourceConflictException(
      ResourceConflictException ex) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

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
