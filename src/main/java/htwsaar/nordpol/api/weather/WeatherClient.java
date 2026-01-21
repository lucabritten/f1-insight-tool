package htwsaar.nordpol.api.weather;

import htwsaar.nordpol.api.dto.WeatherDto;

import java.util.List;
import java.util.Optional;

public class WeatherClient implements IWeatherClient{
    @Override
    public Optional<List<WeatherDto>> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey) {
        return Optional.empty();
    }
}
