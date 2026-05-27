package com.group4.swissrouteapi.utils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * TransportationTypeDeserializer
 *
 * <p>Custom Jackson deserializer for converting JSON string values into {@link TransportationType}
 * enum instances.
 */
public class TransportationTypeDeserializer extends StdDeserializer<TransportationType> {

  public TransportationTypeDeserializer() {
    super(TransportationType.class);
  }

  @Override
  public TransportationType deserialize(JsonParser p, DeserializationContext ctx)
      throws IOException {
    String value = p.getText();

    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return TransportationType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new InvalidFormatException(
          p, Arrays.toString(TransportationType.values()), value, TransportationType.class);
    }
  }
}
