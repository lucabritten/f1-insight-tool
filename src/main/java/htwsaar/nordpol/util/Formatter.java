package htwsaar.nordpol.util;

import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Weather;

public class Formatter {

    private Formatter(){

    }

    private static final String BOLD = "\u001B[1m";
    private static final String RESET = "\u001B[0m";

    public static String formatDriver(Driver driver){
        return """
                %s========== DRIVER ==========%s
                Name         : %s %s
                Number       : %d
                Country Code : %s
                """.formatted(
                        BOLD, RESET,
                    driver.firstName(),
                    driver.lastName(),
                    driver.driverNumber(),
                    driver.countryCode()
            );
    }

    public static String formatWeather(WeatherWithContext weatherWithContext) {
        Weather relatedWeatherData = weatherWithContext.weather();
        return """
                %s========== WEATHER ==========%s
                Location           : %s
                Country name       : %s
                Session Type       : %s
                Air Temperature    : %.1f °C
                Humidity           : %.1f %%
                Track Temperature  : %.1f °C
                Wind Speed         : %.1f m/s
                Wind Direction     : %.1f °
                Rainfall           : %s
                """.formatted(
                        BOLD, RESET,
                weatherWithContext.meetingName(),
                weatherWithContext.countryName(),
                weatherWithContext.sessionName(),
                relatedWeatherData.avgAirTemperature(),
                relatedWeatherData.avgHumidity(),
                relatedWeatherData.avgTrackTemperature(),
                relatedWeatherData.avgWindSpeed(),
                relatedWeatherData.avgWindDirection(),
                relatedWeatherData.isRainfall() ? "Yes" : "No"
        );
    }
}
