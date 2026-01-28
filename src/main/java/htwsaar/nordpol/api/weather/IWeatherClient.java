package htwsaar.nordpol.api.weather;

import htwsaar.nordpol.api.dto.WeatherDto;

import java.util.List;
import java.util.Optional;

public interface IWeatherClient {
    List<WeatherDto> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey);
}
