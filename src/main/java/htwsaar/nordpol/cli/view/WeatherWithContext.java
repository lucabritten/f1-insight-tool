package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;

public record WeatherWithContext(String meetingName,
                                 String countryName,
                                 SessionName sessionName,
                                 Weather weather) {
}
