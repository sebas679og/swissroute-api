package com.group4.swissrouteapi.dtos.responses;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Station {
    String id;
    String name;
    Double latitude;
    Double longitude;
}
