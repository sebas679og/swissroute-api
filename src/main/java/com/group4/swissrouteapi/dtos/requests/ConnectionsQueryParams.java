package com.group4.swissrouteapi.dtos.requests;

import com.group4.swissrouteapi.utils.enums.TransportType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ConnectionsQueryParams
 *
 * <p>Class representing query parameters for searching transport connections. Includes origin and
 * destination station names, optional date and time, and optional list of transportation types to
 * filter results.
 *
 * <p>Built with Lombok {@link lombok.Data}, {@link lombok.Builder}, {@link
 * lombok.NoArgsConstructor}, and {@link lombok.AllArgsConstructor} for easy construction and usage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description =
        """
        Query parameters for searching transport connections between two stations.

        Includes required origin and destination station names, optional date and
        time for departure or arrival,
        and optional list of transportation types to filter results.
        """)
public class ConnectionsQueryParams {

  @NotBlank(message = "Origin station must not be blank")
  @Schema(
      description = "Origin station name",
      example = "Bern",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String from;

  @NotBlank(message = "Destination station must not be blank")
  @Schema(
      description = "Destination station name",
      example = "Zurich",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String to;

  @Schema(description = "Date for the connection search", example = "2023-10-10")
  private LocalDate date;

  @Schema(description = "Time for the connection search", example = "08:00")
  private LocalTime time;

  @ArraySchema(
      schema =
          @Schema(
              type = "string",
              allowableValues = {"train", "tram", "ship", "bus", "cableway"}),
      arraySchema = @Schema(example = "[\"train\", \"bus\"]"))
  @Builder.Default
  private List<TransportType> transportations = new ArrayList<>();

  @ArraySchema(
      schema = @Schema(type = "string", example = "Olten"),
      maxItems = 5,
      arraySchema =
          @Schema(
              description =
                  "Specifies up to five via locations. When specifying several vias, "
                      + "array notation (via=via1&via=via2) is required."))
  @Size(max = 5, message = "A maximum of 5 via locations is allowed")
  @Builder.Default
  private List<String> via = new ArrayList<>();
}
