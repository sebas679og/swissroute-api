package com.group4.swissrouteapi;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

public class ApiTransportsStationsBoardStub {

    private final WireMockExtension wireMock;

    public ApiTransportsStationsBoardStub(WireMockExtension wireMock) {
        this.wireMock = wireMock;
    }

    public void reset() {
        wireMock.resetAll();
    }
}
