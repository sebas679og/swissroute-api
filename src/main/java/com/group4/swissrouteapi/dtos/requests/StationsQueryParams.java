package com.group4.swissrouteapi.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationsQueryParams {

    @NotBlank(message = "Query cannot be blank")
    private String query;
}
