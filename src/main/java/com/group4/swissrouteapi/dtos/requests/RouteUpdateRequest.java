package com.group4.swissrouteapi.dtos.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.group4.swissrouteapi.utils.deserializer.TransportationTypeDeserializer;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteUpdateRequest {
    private String name;
    private String origin;
    private String destination;

    @JsonDeserialize(using = TransportationTypeDeserializer.class)
    private TransportationType transportationType;
}
