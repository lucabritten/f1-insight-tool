package htwsaar.nordpol.service.weather;

import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;

public interface IWeatherService {
    WeatherWithContext getWeatherByLocationYearAndSessionName(String location, int year, SessionName sessionName);
    Weather getWeatherByMeetingAndSessionKey(int meetingKey, int sessionKey);
}
