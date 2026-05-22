package com.group4.swissrouteapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

public class ApiTransportsStub {

  private final WireMockExtension wireMock;

  public ApiTransportsStub(WireMockExtension wireMock) {
    this.wireMock = wireMock;
  }

  public void stubLocationsByQuery(String query) {
    wireMock.stubFor(
        get(urlPathEqualTo("/locations"))
            .withQueryParam("query", equalTo(query))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-transports/locations/locations-by-query.json")));
  }

  public void stubLocationsByQueryNotFound(String query) {
    wireMock.stubFor(
        get(urlPathEqualTo("/locations"))
            .withQueryParam("query", equalTo(query))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-transports/locations/locations-by-query-not-found.json")));
  }

  public void reset() {
    wireMock.resetAll();
  }
}
