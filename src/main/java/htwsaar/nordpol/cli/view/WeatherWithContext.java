package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.Weather;

public record WeatherWithContext(String meetingName,
                                 String countryName,
                                 String sessionName,
                                 Weather weather) {
}
