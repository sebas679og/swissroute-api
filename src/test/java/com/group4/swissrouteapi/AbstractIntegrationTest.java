package com.group4.swissrouteapi;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Base integration test configuration. */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class AbstractIntegrationTest {

  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.3");

  @RegisterExtension
  static WireMockExtension wireMockExtension =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("src/test/resources"))
          .build();

  protected ApiTransportsLocationsStub transportsStubLocations;
  protected ApiTransportsConnectionsStub connectionsStub;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("swissroute.app.transport.api.base-url", wireMockExtension::baseUrl);
  }

  @BeforeEach
  void setUpBase() {
    transportsStubLocations = new ApiTransportsLocationsStub(wireMockExtension);
    connectionsStub = new ApiTransportsConnectionsStub(wireMockExtension);
  }

  @AfterEach
  void tearDown() {
    transportsStubLocations.reset();
    connectionsStub.reset();
  }
}
