package htwsaar.nordpol.api.weather;

import htwsaar.nordpol.dto.WeatherDto;

import java.util.List;

public interface IWeatherClient {
    List<WeatherDto> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey);
}
