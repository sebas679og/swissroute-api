package com.group4.swissrouteapi.dtos.responses.favorites;

import com.group4.swissrouteapi.utils.enums.TransportationType;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class Route {
    UUID id;
    String name;
    String origin;
    String destination;
    TransportationType transportType;
}
