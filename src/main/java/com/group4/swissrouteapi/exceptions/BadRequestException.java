package com.group4.swissrouteapi.exceptions;

/**
 * BadRequestException
 *
 * <p>Exception type representing an invalid or malformed client request that cannot be processed by
 * the server.
 *
 * <p>Typically used to signal input validation errors, missing parameters, or incorrect request
 * formats in API operations.
 *
 * <p>Extends {@link RuntimeException} to allow unchecked propagation within the application.
 */
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
