package htwsaar.nordpol.api.weather;

import htwsaar.nordpol.api.dto.WeatherDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WeatherClientTest {

    private MockWebServer mockWebServer;
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        weatherClient = new WeatherClient(mockWebServer.url("/v1").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    void getWeatherDataByMeetingKeyAndSessionKey_returnsWeatherList() {
        String json = """
                [
                  {
                    "session_key": 9001,
                    "meeting_key": 1247,
                    "air_temperature": 27.5,
                    "humidity": 52.0,
                    "rainfall": 0,
                    "track_temperature": 39.1,
                    "wind_direction": 180.0,
                    "wind_speed": 3.4
                  },
                  {
                    "session_key": 9001,
                    "meeting_key": 1247,
                    "air_temperature": 28.1,
                    "humidity": 50.0,
                    "rainfall": 0,
                    "track_temperature": 40.0,
                    "wind_direction": 175.0,
                    "wind_speed": 3.1
                  }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<List<WeatherDto>> result =
                weatherClient.getWeatherDataByMeetingKeyAndSessionKey(1247, 9001);

        assertThat(result).isPresent();

        List<WeatherDto> weather = result.get();
        assertThat(weather).hasSize(2);

        WeatherDto first = weather.getFirst();
        assertThat(first.meeting_key()).isEqualTo(1247);
        assertThat(first.session_key()).isEqualTo(9001);
        assertThat(first.air_temperature()).isEqualTo(27.5);
        assertThat(first.humidity()).isEqualTo(52.0);
        assertThat(first.rainfall()).isEqualTo(0);
        assertThat(first.track_temperature()).isEqualTo(39.1);
        assertThat(first.wind_direction()).isEqualTo(180.0);
        assertThat(first.wind_speed()).isEqualTo(3.4);
    }

    @Test
    void getWeatherDataByMeetingKeyAndSessionKey_returnsEmptyOptional_whenApiResponseIsEmpty() {
        String json = "[]";

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<List<WeatherDto>> result =
                weatherClient.getWeatherDataByMeetingKeyAndSessionKey(9999, 8888);

        assertThat(result).isEmpty();
    }

    @Test
    void getWeatherDataByMeetingKeyAndSessionKey_throwsException_whenJsonIsInvalid() {
        String invalidJson = "{invalid";

        mockWebServer.enqueue(new MockResponse()
                .setBody(invalidJson)
                .setResponseCode(200)
        );

        assertThatThrownBy(() ->
                weatherClient.getWeatherDataByMeetingKeyAndSessionKey(1247, 9001)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void getWeatherDataByMeetingKeyAndSessionKey_returnsEmptyOptional_whenHttpStatusIsNotSuccessful() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );

        Optional<List<WeatherDto>> result =
                weatherClient.getWeatherDataByMeetingKeyAndSessionKey(1247, 9001);

        assertThat(result).isEmpty();
    }

    @Test
    void getWeatherDataByMeetingKeyAndSessionKey_throwsRuntimeException_whenConnectionFails() throws IOException {
        mockWebServer.shutdown();

        assertThatThrownBy(() ->
                weatherClient.getWeatherDataByMeetingKeyAndSessionKey(1247, 9001)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch weather");
    }
}