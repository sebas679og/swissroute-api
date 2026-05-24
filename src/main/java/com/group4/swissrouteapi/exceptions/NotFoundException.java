package com.group4.swissrouteapi.exceptions;

/**
 * NotFoundException
 *
 * <p>Exception type representing a "Not Found" error scenario.
 *
 * <p>Typically used to indicate that a requested resource, entity, or record could not be located
 * in the system.
 *
 * <p>Extends {@link RuntimeException} to allow unchecked propagation within the application.
 */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
