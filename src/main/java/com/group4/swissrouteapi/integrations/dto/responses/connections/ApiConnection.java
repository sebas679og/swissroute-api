package com.group4.swissrouteapi.integrations.dto.responses.connections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.group4.swissrouteapi.integrations.dto.responses.ApiEndpoint;
import java.util.List;

/**
 * ApiConnection
 *
 * <p>Record representing a transport API connection. Holds origin and destination endpoints,
 * duration, number of transfers, service details, products, seating capacity, and journey sections.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiConnection(
    ApiEndpoint from,
    ApiEndpoint to,
    String duration,
    Integer transfers,
    Object service,
    List<String> products,
    Integer capacity1st,
    Integer capacity2nd,
    List<ApiSection> sections) {}
