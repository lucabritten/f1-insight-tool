package htwsaar.nordpol.api.meeting;

import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.config.ApplicationContext;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
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
        meetingClient = new MeetingClient(mockWebServer.url("/").toString(), ApplicationContext.getInstance().objectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("getMeetingByYearAndLocation")
    class GetMeetingByYearAndLocation {

        @Test
        void returnsMeeting() {
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

            Optional<MeetingDto> result = meetingClient.getMeetingByYearAndLocation(2024, "Austin");

            assertThat(result).isPresent();

            MeetingDto meetingDto = result.get();

            assertThat(meetingDto.location()).isEqualTo("Austin");
            assertThat(meetingDto.year()).isEqualTo(2024);
        }

        @Test
        void returnsEmptyOptional_whenApiResponseIsEmpty() {
            String json = "[]";

            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(json)
                    .setResponseCode(200)
            );

            Optional<MeetingDto> result = meetingClient.getMeetingByYearAndLocation(2027, "Saarbrücken");

            assertThat(result).isEmpty();
        }

        @Test
        void returnsEmptyOptional_whenHttpStatusIsNotSuccessful() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
            );

            Optional<MeetingDto> result =
                    meetingClient.getMeetingByYearAndLocation(2024, "Austin");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("GetMeetingByYear")
    class getMeetingByYear {

        @Test
        void returnsMeetingList() {
            String json = """
                    [
                        {
                            "meeting_key": 1247,
                            "country_code": "USA",
                            "country_name": "United States",
                            "location": "Austin",
                            "year": 2024
                        },
                        {
                            "meeting_key": 1248,
                            "country_code": "AUT",
                            "country_name": "Austria",
                            "location": "Spielberg",
                            "year": 2024
                        }
                    ]
                    """;

            mockWebServer.enqueue(new MockResponse()
                            .setBody(json)
                            .setResponseCode(200));

            List<MeetingDto> result = meetingClient.getMeetingsByYear(2024);
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().year()).isEqualTo(2024);
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

            assertThatThrownBy(() ->
                    meetingClient.getMeetingByYearAndLocation(2027, "Saarbrücken")
            )
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void throwsRuntimeException_whenConnectionFails() throws IOException {
            mockWebServer.shutdown();

            assertThatThrownBy(() ->
                    meetingClient.getMeetingByYearAndLocation(2024, "Austin")
            ).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to fetch data from OpenF1 API");
        }
    }
}
