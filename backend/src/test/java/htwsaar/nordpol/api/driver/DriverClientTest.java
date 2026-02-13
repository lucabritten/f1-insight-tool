package htwsaar.nordpol.api.driver;

import htwsaar.nordpol.config.api.ApiClientConfig;
import htwsaar.nordpol.dto.DriverDto;


import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class DriverClientTest {

    private MockWebServer mockWebServer;
    private DriverClient driverClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        driverClient = new DriverClient(mockWebServer.url("/").toString(), ApiClientConfig.openF1HttpClient(),new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("getDriverByName")
    class GetDriverByName {

        @Test
        void returnsDriver() {
            String json = """
                    [
                        {
                            "driver_number": 44,
                            "first_name": "Lewis",
                            "last_name": "Hamilton",
                            "country_code": "GBR"
                        }
                    ]
                    """;

            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(json)
                    .setResponseCode(200)
            );

            Optional<DriverDto> optionalDriverDto = driverClient.getDriverByName("Lewis", "Hamilton", 2024);

            assertThat(optionalDriverDto).isPresent();

            DriverDto driverDto = optionalDriverDto.get();

            assertThat(driverDto.driver_number()).isEqualTo(44);
            assertThat(driverDto.first_name()).isEqualTo("Lewis");
            assertThat(driverDto.last_name()).isEqualTo("Hamilton");
        }

        @Test
        void returnsEmptyOptional_whenApiReturnsEmptyArray() {
            String json = "[]";

            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(json)
                    .setResponseCode(200)
            );

            Optional<DriverDto> driverApiDto = driverClient.getDriverByName("Unknown", "Driver", 2025);

            assertThat(driverApiDto).isEmpty();
        }

        @Test
        void returnsEmptyOptional_whenApiReturns404() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(404)
            );

            Optional<DriverDto> optionalDriverDto = driverClient.getDriverByName("Lando", "NORRIS", 2025);

            assertThat(optionalDriverDto).isEmpty();
        }

        @Test
        void throwsException_whenJsonIsInvalid() {
            String invalidJson = "{invalid";

            mockWebServer.enqueue(new MockResponse()
                    .setBody(invalidJson)
                    .setResponseCode(200)
            );

            assertThatThrownBy(() ->
                    driverClient.getDriverByName("Lewis", "Hamilton", 2025)
            )
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void returnsEmptyOptional_whenHttpStatusIsNotSuccessful() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
            );

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
            );

            Optional<DriverDto> result =
                    driverClient.getDriverByName("Lewis", "Hamilton", 1234);

            assertThat(result).isEmpty();
        }

        @Test
        void throwsRuntimeException_whenConnectionFails() throws IOException {
            mockWebServer.shutdown();

            assertThatThrownBy(() ->
                    driverClient.getDriverByName("Lewis", "Hamilton", 1234)

            ).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to fetch data from OpenF1 API");
        }
    }

    @Nested
    @DisplayName("getDriverByNumberAndMeetingKey")
    class GetDriverByNumberAndMeetingKey {

        @Test
        void returnsDriver() {
            String json = """
                    [
                        {
                            "driver_number": 44,
                            "first_name": "Lewis",
                            "last_name": "Hamilton",
                            "team_name": "Mercedes"
                        }
                    ]
                    """;

            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(json)
                    .setResponseCode(200)
            );

            Optional<DriverDto> optionalDriverDto =
                    driverClient.getDriverByNumberAndMeetingKey(44, 2025);

            assertThat(optionalDriverDto).isPresent();

            DriverDto driverDto = optionalDriverDto.get();
            assertThat(driverDto.driver_number()).isEqualTo(44);
            assertThat(driverDto.first_name()).isEqualTo("Lewis");
            assertThat(driverDto.last_name()).isEqualTo("Hamilton");
            assertThat(driverDto.team_name()).isEqualTo("Mercedes");
        }

        @Test
        void returnsEmptyOptional_whenApiReturnsEmptyArray() {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody("[]")
                    .setResponseCode(200)
            );

            Optional<DriverDto> result =
                    driverClient.getDriverByNumberAndMeetingKey(999, 2025);

            assertThat(result).isEmpty();
        }

        @Test
        void returnsEmptyOptional_whenApiReturns404() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(404)
            );

            Optional<DriverDto> result =
                    driverClient.getDriverByNumberAndMeetingKey(44, 2025);

            assertThat(result).isEmpty();
        }

        @Test
        void returnsEmptyOptional_whenHttpStatusIsNotSuccessful() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
            );

            Optional<DriverDto> result =
                    driverClient.getDriverByNumberAndMeetingKey(44, 2025);

            assertThat(result).isEmpty();
        }

        @Test
        void throwsException_whenJsonIsInvalid() {
            String invalidJson = "{invalid";

            mockWebServer.enqueue(new MockResponse()
                    .setBody(invalidJson)
                    .setResponseCode(200)
            );

            assertThatThrownBy(() ->
                    driverClient.getDriverByNumberAndMeetingKey(44, 2025)
            ).isInstanceOf(RuntimeException.class);
        }

        @Test
        void throwsRuntimeException_whenConnectionFails() throws IOException {
            mockWebServer.shutdown();

            assertThatThrownBy(() ->
                    driverClient.getDriverByNumberAndMeetingKey(44, 2025)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to fetch data from OpenF1 API");
        }
    }
}
