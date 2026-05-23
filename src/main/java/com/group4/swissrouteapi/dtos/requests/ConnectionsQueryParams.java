package com.group4.swissrouteapi.dtos.requests;

import com.group4.swissrouteapi.utils.enums.TransportationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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

  @DateTimeFormat(iso = DateTimeFormat.ISO.NONE)
  @Schema(
      description = "List of transportation types to filter results",
      allowableValues = {"train", "tram", "ship", "bus", "cableway"})
  private List<TransportationType> transportations;
}
