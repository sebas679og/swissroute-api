package com.group4.swissrouteapi.utils.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TransportationTypeDeserializer")
class TransportationTypeDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TransportationType.class, new TransportationTypeDeserializer());
        objectMapper = new ObjectMapper().registerModule(module);
    }

    /**
     * Serializes the given raw Java string into a valid JSON string token using
     * ObjectMapper, then deserializes it back as TransportationType.
     * This avoids embedding literal control characters (tab, newline) directly
     * into JSON, which would produce a JsonParseException.
     */
    private TransportationType deserialize(String value) throws JsonProcessingException {
        String jsonToken = objectMapper.writeValueAsString(value);
        return objectMapper.readValue(jsonToken, TransportationType.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Valid values
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Valid values")
    class ValidValues {

        @ParameterizedTest(name = "uppercase [{0}]")
        @ValueSource(strings = {"TRAIN", "TRAM", "SHIP", "BUS", "CABLEWAY"})
        @DisplayName("deserializes all exact uppercase enum names correctly")
        void deserializesExactUppercase_correctly(String value) throws JsonProcessingException {
            assertThat(deserialize(value)).isEqualTo(TransportationType.valueOf(value));
        }

        @ParameterizedTest(name = "lowercase [{0}]")
        @ValueSource(strings = {"train", "tram", "ship", "bus", "cableway"})
        @DisplayName("normalizes lowercase input to uppercase before resolving the enum")
        void normalizesLowercase_toUppercase(String value) throws JsonProcessingException {
            assertThat(deserialize(value)).isEqualTo(TransportationType.valueOf(value.toUpperCase()));
        }

        @ParameterizedTest(name = "mixed-case [{0}]")
        @ValueSource(strings = {"Train", "tRaIn", "sHip", "Bus", "CableWay"})
        @DisplayName("normalizes mixed-case input to uppercase before resolving the enum")
        void normalizesMixedCase_toUppercase(String value) throws JsonProcessingException {
            assertThat(deserialize(value))
                    .isEqualTo(TransportationType.valueOf(value.trim().toUpperCase()));
        }

        @ParameterizedTest(name = "padded [{0}]")
        @ValueSource(strings = {" TRAIN", "TRAM ", " BUS ", " cableway "})
        @DisplayName("trims surrounding whitespace before resolving the enum")
        void trimsSurroundingWhitespace_beforeResolvingEnum(String value)
                throws JsonProcessingException {
            assertThat(deserialize(value))
                    .isEqualTo(TransportationType.valueOf(value.trim().toUpperCase()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Null / blank → null
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Blank and empty values return null")
    class BlankAndEmptyValues {

        @Test
        @DisplayName("returns null when value is an empty string")
        void returnsNull_whenValueIsEmptyString() throws JsonProcessingException {
            assertThat(deserialize("")).isNull();
        }

        @Test
        @DisplayName("returns null when value contains only spaces")
        void returnsNull_whenValueIsBlankWithSpaces() throws JsonProcessingException {
            // writeValueAsString safely escapes the string into valid JSON
            assertThat(deserialize("   ")).isNull();
        }

        @Test
        @DisplayName("returns null when value contains only a tab character")
        void returnsNull_whenValueIsBlankWithTab() throws JsonProcessingException {
            // Raw \t would be illegal inside a JSON string literal;
            // writeValueAsString produces the valid escaped form \"\\t\".
            assertThat(deserialize("\t")).isNull();
        }

        @Test
        @DisplayName("returns null when value contains only a newline character")
        void returnsNull_whenValueIsBlankWithNewline() throws JsonProcessingException {
            assertThat(deserialize("\n")).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Invalid values → InvalidFormatException
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Invalid values throw InvalidFormatException")
    class InvalidValues {

        @ParameterizedTest(name = "invalid [{0}]")
        @ValueSource(strings = {"PLANE", "SUBWAY", "CAR", "BICYCLE", "UNKNOWN", "123", "!!"})
        @DisplayName("throws InvalidFormatException for every unrecognized value")
        void throwsInvalidFormatException_forUnrecognizedValues(String invalid) {
            assertThatThrownBy(() -> deserialize(invalid))
                    .isInstanceOf(InvalidFormatException.class);
        }

        @Test
        @DisplayName("exception target type is TransportationType")
        void exceptionTargetType_isTransportationType() {
            assertThatThrownBy(() -> deserialize("HELICOPTER"))
                    .isInstanceOf(InvalidFormatException.class)
                    .satisfies(ex -> {
                        InvalidFormatException ife = (InvalidFormatException) ex;
                        assertThat(ife.getTargetType()).isEqualTo(TransportationType.class);
                    });
        }

        @Test
        @DisplayName("exception getValue() holds the original unrecognized string")
        void exceptionGetValue_holdsTheOriginalInvalidString() {
            assertThatThrownBy(() -> deserialize("HELICOPTER"))
                    .isInstanceOf(InvalidFormatException.class)
                    .satisfies(ex -> {
                        InvalidFormatException ife = (InvalidFormatException) ex;
                        assertThat(ife.getValue()).isEqualTo("HELICOPTER");
                    });
        }

        @Test
        @DisplayName("exception message contains the valid enum values")
        void exceptionMessage_containsValidEnumValues() {
            // The deserializer passes Arrays.toString(TransportationType.values()) as the
            // msgTemplate — the invalid input is stored in getValue(), not in the message.
            assertThatThrownBy(() -> deserialize("HELICOPTER"))
                    .isInstanceOf(InvalidFormatException.class)
                    .hasMessageContainingAll("TRAIN", "TRAM", "SHIP", "BUS", "CABLEWAY");
        }
    }
}
