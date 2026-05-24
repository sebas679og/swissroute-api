package com.group4.swissrouteapi.integrations.dto.responses.connections;

import java.util.List;

/**
 * ApiJourney
 *
 * <p>Record representing a transport API journey. Holds basic journey details such as name,
 * category, operator, destination, list of endpoints, and seating capacity.
 */
public record ApiJourney(
    String name,
    String category,
    String subcategory,
    String categoryCode,
    String number,
    String operator,
    String to,
    List<ApiEndpoint> passList,
    Integer capacity1st,
    Integer capacity2nd) {}
