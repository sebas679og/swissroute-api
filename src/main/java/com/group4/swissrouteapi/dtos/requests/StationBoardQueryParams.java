package com.group4.swissrouteapi.dtos.requests;

import com.group4.swissrouteapi.utils.enums.TransportType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StationBoardQueryParams
 *
 * <p>DTO representing query parameters used to filter and retrieve station departure and arrival
 * board information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
    description = "Parameters used to filter and retrieve the station departure and arrival board.")
public class StationBoardQueryParams {

  @NotBlank(message = "Station is required")
  @Schema(description = "Name of the station to search for.", example = "Aarau")
  private String station;

  @Schema(description = "Unique identifier of the station.", example = "8503059")
  private String id;

  @Schema(description = "Maximum number of connections to return.", example = "15")
  private Integer limit;

  @ArraySchema(
      schema =
          @Schema(
              type = "string",
              allowableValues = {"train", "tram", "ship", "bus", "cableway"}),
      arraySchema =
          @Schema(
              description = "Filter results by transport types.",
              example = "[\"train\", \"bus\"]"))
  @Builder.Default
  private List<TransportType> transportType = new ArrayList<>();
}
