package com.group4.swissrouteapi.exceptions;

/**
 * ResourceConflictException
 *
 * <p>Exception type representing a conflict with an existing resource.
 *
 * <p>Typically used to indicate that a requested operation cannot be completed because the resource
 * already exists or violates a uniqueness constraint (e.g., duplicate email during registration).
 *
 * <p>Extends {@link RuntimeException} to allow unchecked propagation within the application.
 */
public class ResourceConflictException extends RuntimeException {
  public ResourceConflictException(String message) {
    super(message);
  }
}
