package com.group4.swissrouteapi.exceptions;

/**
 * ServiceUnavailableException
 *
 * <p>Exception type representing service unavailability.
 *
 * <p>Typically used to indicate that an external dependency or downstream service is temporarily
 * unreachable or unable to process requests.
 *
 * <p>Extends {@link RuntimeException} to allow unchecked propagation within the application.
 */
public class ServiceUnavailableException extends RuntimeException {
  public ServiceUnavailableException(String message) {
    super(message);
  }
}
