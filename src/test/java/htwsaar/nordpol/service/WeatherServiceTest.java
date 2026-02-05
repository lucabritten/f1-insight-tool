package htwsaar.nordpol.service;

import htwsaar.nordpol.dto.WeatherDto;
import htwsaar.nordpol.api.weather.WeatherClient;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.exception.WeatherNotFoundException;
import htwsaar.nordpol.repository.weather.JooqWeatherRepo;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.weather.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    WeatherClient weatherClient;

    @Mock
    JooqWeatherRepo weatherRepo;

    @Mock
    MeetingService meetingService;

    @Mock
    SessionService sessionService;

    WeatherService weatherService;

    @BeforeEach
    void setup() {
        weatherService = new WeatherService(weatherClient, weatherRepo, sessionService, meetingService);
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        void nullRepository_throwsException() {
            assertThatThrownBy(() ->
                    new WeatherService(weatherClient, null, sessionService, meetingService)
            ).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("weatherRepo");
        }

        @Test
        void nullClient_throwsException() {
            assertThatThrownBy(() ->
                    new WeatherService(null, weatherRepo, sessionService, meetingService)
            ).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("weatherClient");
        }
    }

    @Nested
    @DisplayName("getWeatherByMeetingAndSessionKey")
    class GetWeatherByMeetingAndSessionKey {

        @Test
        void returnsWeatherFromDatabase() {
            WeatherDto dbDto =
                    new WeatherDto(
                            1111,
                            2222,
                            19.4,
                            65.4,
                            0,
                            26.3,
                            130.0,
                            5.4
                    );

            when(weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(1111, 2222))
                    .thenReturn(Optional.of(dbDto));

            Weather result =
                    weatherService.getWeatherByMeetingAndSessionKey(1111, 2222);

            assertThat(result.avgAirTemperature()).isEqualTo(19.4);

            verify(weatherClient, never()).getWeatherDataByMeetingKeyAndSessionKey(anyInt(), anyInt());
            verify(weatherRepo).getWeatherDataByMeetingKeyAndSessionKey(anyInt(), anyInt());
        }

        @Test
        void fetchesFromApiAndSavesWeather() {
            when(weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(1111, 2222))
                    .thenReturn(Optional.empty());

            List<WeatherDto> weatherDtos = List.of(
                    new WeatherDto(
                            1111,
                            2222,
                            29.3,
                            87.0,
                            1,
                            38.3,
                            23.3,
                            19.3
                    )
            );

            when(weatherClient.getWeatherDataByMeetingKeyAndSessionKey(1111, 2222))
                    .thenReturn(weatherDtos);

            Weather result =
                    weatherService.getWeatherByMeetingAndSessionKey(1111, 2222);

            assertThat(result.meetingKey()).isEqualTo(2222);
            assertThat(result.isRainfall()).isTrue();
        }

        @Test
        void throwsException_whenWeatherNotFoundAnywhere() {
            when(weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(1111, 2222))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    weatherService.getWeatherByMeetingAndSessionKey(1111, 2222)
            ).isInstanceOf(WeatherNotFoundException.class)
                    .hasMessageContaining("No weather-data");
        }
    }
}
