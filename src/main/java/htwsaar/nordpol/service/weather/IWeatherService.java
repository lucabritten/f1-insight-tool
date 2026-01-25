package htwsaar.nordpol.service.weather;

import htwsaar.nordpol.api.dto.WeatherDto;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;

import java.util.List;

public interface IWeatherService {
    WeatherWithContext getWeatherByLocationYearAndSessionName(String location, int year, SessionName sessionName);
    Weather getWeatherByMeetingAndSessionKey(int meetingKey, int sessionKey);

    private WeatherDto calculateAverageData(List<WeatherDto> dtoList){
        return null;
    }
}
