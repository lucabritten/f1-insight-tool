package htwsaar.nordpol.api.session;

import htwsaar.nordpol.api.dto.SessionDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SessionClientTest {

    private MockWebServer mockWebServer;
    private SessionClient sessionClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        sessionClient = new SessionClient(mockWebServer.url("/v1").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getSessionByMeetingKeyAndsessionName_returnsSession(){
        String json = """
                [
                    {
                        "meeting_key": 1256,
                        "session_key": 9999,
                        "session_name": "Practice 1",
                        "session_type": "Practice"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<SessionDto> result = sessionClient.getSessionByMeetingKeyAndsessionName(1256, "Practice");

        assertThat(result).isPresent();

        SessionDto sessionDto = result.get();

        assertThat(sessionDto.meeting_key()).isEqualTo(1256);
        assertThat(sessionDto.session_type()).isEqualTo("Practice");

    }

    @Test
    void getSessionByMeetingKeyAndsessionName_returnsEmptyOptional_whenApiResponseIsEmpty(){
        String json = "[]";

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<SessionDto> result = sessionClient.getSessionByMeetingKeyAndsessionName(2021, "DestroyHeadset");

        assertThat(result).isEmpty();
    }

    @Test
    void getSessionByMeetingKeyAndsessionName_returnsEmptyOptional_whenHttpStatusIsNotSuccessful(){
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );

        Optional<SessionDto> result =
                sessionClient.getSessionByMeetingKeyAndsessionName(1256, "Practice");

        assertThat(result).isEmpty();
    }

    @Test
    void getSessionByMeetingKeyAndsessionName_throwsRuntimeException_whenConnectionFails() throws IOException {
        mockWebServer.shutdown();

        assertThatThrownBy(() ->
                sessionClient.getSessionByMeetingKeyAndsessionName(1256, "Practice"))
        .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch session from OpenF1 API");
    }
}
