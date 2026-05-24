package com.group4.swissrouteapi.exceptions;

/**
 * UnauthorizedException
 *
 * <p>Exception type representing unauthorized access attempts.
 *
 * <p>Typically used to indicate that a client request lacks valid authentication credentials or
 * does not have sufficient privileges to access a protected resource.
 *
 * <p>Extends {@link RuntimeException} to allow unchecked propagation within the application.
 */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
