package htwsaar.nordpol.api.sessionresult;

import htwsaar.nordpol.config.ApplicationContext;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionResultClientTest {

    private MockWebServer mockWebServer;
    private SessionResultClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new SessionResultClient(mockWebServer.url("/v1").toString(), ApplicationContext.getInstance().objectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("getSessionResultsBySessionKey")
    class getSessionResultBySessionKey{
        @Test
        void callsCorrectEndpointWithQueryParam() throws InterruptedException {
            String json = """
                [
                   {
                      "position":1,
                      "driver_number":1,
                      "number_of_laps":24,
                      "dnf":false,
                      "dns":false,
                      "dsq":false,
                      "duration":77.565,
                      "gap_to_leader":0,
                      "meeting_key":1143,
                      "session_key":7782
                   },
                   {
                      "position":2,
                      "driver_number":14,
                      "number_of_laps":26,
                      "dnf":false,
                      "dns":false,
                      "dsq":false,
                      "gap_to_leader":0.162,
                      "duration":77.727,
                      "meeting_key":1143,
                      "session_key":7782
                   },
                   {
                      "position":3,
                      "driver_number":31,
                      "number_of_laps":23,
                      "dnf":false,
                      "dns":false,
                      "dsq":false,
                      "gap_to_leader":0.373,
                      "duration":77.938,
                      "meeting_key":1143,
                      "session_key":7782
                   }
                ]
                """;

            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .setBody(json)
            );

            client.getSessionResultBySessionKey(7782);

            RecordedRequest request = mockWebServer.takeRequest();

            assertThat(request.getPath())
                    .isEqualTo("/v1/session_result?session_key=7782");
        }
    }
}
