package com.group4.swissrouteapi.exceptions;

/**
 * BadGatewayException
 *
 * <p>Exception type representing a "Bad Gateway" error scenario.
 *
 * <p>Typically used to indicate that the application, acting as a gateway or proxy, received an
 * invalid or unexpected response from an upstream service.
 *
 * <p>Extends {@link RuntimeException} to allow unchecked propagation within the application.
 */
public class BadGatewayException extends RuntimeException {
  public BadGatewayException(String message) {
    super(message);
  }
}
