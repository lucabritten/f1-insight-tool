package htwsaar.nordpol.repository.weather;

import htwsaar.nordpol.api.dto.WeatherDto;

import java.util.Optional;

public interface IWeatherRepo {
    void save(WeatherDto dto);
    Optional<WeatherDto> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey);
}
