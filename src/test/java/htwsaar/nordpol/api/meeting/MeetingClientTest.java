package htwsaar.nordpol.api.meeting;

import htwsaar.nordpol.api.dto.MeetingDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeetingClientTest {

    private MockWebServer mockWebServer;
    private MeetingClient meetingClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        meetingClient = new MeetingClient(mockWebServer.url("/v1").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    void getMeetingBySeasonAndLocation_returnsMeeting() {
        String json = """
                [
                    {
                        "meeting_key": 1247,
                        "country_code": "USA",
                        "country_name": "United States",
                        "location": "Austin",
                        "year": 2024
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<MeetingDto> result = meetingClient.getMeetingBySeasonAndLocation(2024, "Austin");

        assertThat(result).isPresent();

        MeetingDto meetingDto = result.get();

        assertThat(meetingDto.location()).isEqualTo("Austin");
        assertThat(meetingDto.year()).isEqualTo(2024);
    }

    @Test
    void getMeetingBySeasonAndLocation_returnsEmptyOptional_whenApiResponseIsEmpty(){
        String json = "[]";

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<MeetingDto> result = meetingClient.getMeetingBySeasonAndLocation(2027, "Saarbrücken");

        assertThat(result).isEmpty();
    }

    @Test
    void getMeetingBySeasonAndLocation_throwsException_whenJsonIsInvalid() {
        String invalidJson = "{invalid";

        mockWebServer.enqueue(new MockResponse()
                .setBody(invalidJson)
                .setResponseCode(200)
        );

        assertThatThrownBy(() ->
                meetingClient.getMeetingBySeasonAndLocation(2027, "Saarbrücken")
        )
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getMeetingBySeasonAndLocation_returnsEmptyOptional_whenHttpStatusIsNotSuccessful() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );

        Optional<MeetingDto> result =
                meetingClient.getMeetingBySeasonAndLocation(2024, "Austin");

        assertThat(result).isEmpty();
    }

    @Test
    void getMeetingBySeasonAndLocation_throwsRuntimeException_whenConnectionFails() throws IOException{
        mockWebServer.shutdown();

        assertThatThrownBy(() ->
                meetingClient.getMeetingBySeasonAndLocation(2024, "Austin")
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch meeting");
    }
}
