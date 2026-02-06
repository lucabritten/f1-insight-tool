package htwsaar.nordpol.api.lap;

import htwsaar.nordpol.config.api.ApiClientConfig;
import htwsaar.nordpol.dto.LapDto;
import htwsaar.nordpol.config.ApplicationContext;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LapClientTest {

    private MockWebServer mockWebServer;
    private LapClient lapClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        lapClient = new LapClient(mockWebServer.url("/").toString(), ApiClientConfig.openF1HttpClient(), ApplicationContext.getInstance().objectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("getLapsBySessionKeyAndDriverNumber")
    class GetLapsBySessionKeyAndDriverNumber {

        @Test
        void returnsLaps() throws InterruptedException {
            String json = """
                    [
                      {
                        "driver_number": 44,
                        "session_key": 123,
                        "lap_number": 1,
                        "duration_sector_1": 30.1,
                        "duration_sector_2": 28.5,
                        "duration_sector_3": 32.4,
                        "lap_duration": 91.0,
                        "is_pit_out_lap": false
                      },
                      {
                        "driver_number": 44,
                        "session_key": 123,
                        "lap_number": 2,
                        "duration_sector_1": 29.9,
                        "duration_sector_2": 28.3,
                        "duration_sector_3": 32.0,
                        "lap_duration": 90.2,
                        "is_pit_out_lap": false
                      }
                    ]
                    """;

            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(json)
                    .setResponseCode(200)
            );

            List<LapDto> result =
                    lapClient.getLapsBySessionKeyAndDriverNumber(123, 44);

            assertThat(result).hasSize(2);

            LapDto lap = result.getFirst();
            assertThat(lap.driver_number()).isEqualTo(44);
            assertThat(lap.session_key()).isEqualTo(123);
            assertThat(lap.lap_number()).isEqualTo(1);
            assertThat(lap.lap_duration()).isEqualTo(91.0);
            assertThat(lap.is_pit_out_lap()).isFalse();

            RecordedRequest request = mockWebServer.takeRequest();

            assertThat(request.getPath()).startsWith("//laps?");
            assertThat(request.getPath()).contains("session_key=123");
            assertThat(request.getPath()).contains("driver_number=44");
        }

        @Test
        void returnsEmptyList_whenApiReturnsEmptyArray() {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody("[]")
                    .setResponseCode(200)
            );

            List<LapDto> result =
                    lapClient.getLapsBySessionKeyAndDriverNumber(123, 44);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLapsBySessionKey")
    class GetLapsBySessionKey {

        @Test
        void returnsLaps() throws InterruptedException {
            String json = """
                    [
                      {
                        "driver_number": 16,
                        "session_key": 999,
                        "lap_number": 10,
                        "duration_sector_1": 31.0,
                        "duration_sector_2": 29.1,
                        "duration_sector_3": 33.2,
                        "lap_duration": 93.3,
                        "is_pit_out_lap": true
                      }
                    ]
                    """;

            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(json)
                    .setResponseCode(200)
            );

            List<LapDto> result = lapClient.getLapsBySessionKey(999);

            assertThat(result).hasSize(1);

            LapDto lap = result.getFirst();
            assertThat(lap.driver_number()).isEqualTo(16);
            assertThat(lap.session_key()).isEqualTo(999);
            assertThat(lap.is_pit_out_lap()).isTrue();

            RecordedRequest request = mockWebServer.takeRequest();

            assertThat(request.getPath()).startsWith("//laps?");
            assertThat(request.getPath()).contains("session_key=999");
        }

        @Test
        void returnsEmptyList_whenApiReturns404() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(404)
            );

            List<LapDto> result = lapClient.getLapsBySessionKey(123);

            assertThat(result).isEmpty();
        }

        @Test
        void returnsEmptyList_whenHttpStatusIsNotSuccessful() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
            );

            List<LapDto> result = lapClient.getLapsBySessionKey(123);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        void throwsException_whenJsonIsInvalid() {
            String invalidJson = "{invalid";

            mockWebServer.enqueue(new MockResponse()
                    .setBody(invalidJson)
                    .setResponseCode(200)
            );

            assertThatThrownBy(() -> lapClient.getLapsBySessionKey(123))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void throwsRuntimeException_whenConnectionFails() throws IOException {
            mockWebServer.shutdown();

            assertThatThrownBy(() -> lapClient.getLapsBySessionKey(123))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to fetch data from OpenF1 API");
        }
    }
}
