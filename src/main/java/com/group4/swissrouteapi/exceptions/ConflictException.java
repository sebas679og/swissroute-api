package com.group4.swissrouteapi.exceptions;

/**
 * ConflictException
 *
 * <p>Exception type representing a conflict with an existing resource or operation that cannot be
 * completed due to state inconsistency.
 */
public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
