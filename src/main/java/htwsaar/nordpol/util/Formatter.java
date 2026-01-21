package htwsaar.nordpol.util;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Weather;

public class Formatter {

    private Formatter(){

    }

    public static String formatDriver(Driver driver){
        return """
                ========== DRIVER ==========
                Name         : %s %s
                Number       : %d
                Country Code : %s
                """.formatted(
                    driver.firstName(),
                    driver.lastName(),
                    driver.driverNumber(),
                    driver.countryCode()
            );
    }

    public static String formatWeather(Weather weather) {
        return """
                ========== WEATHER ==========
                Meeting Key        : %d
                Session Key        : %d
                Air Temperature    : %.1f °C
                Humidity           : %.1f %%
                Track Temperature  : %.1f °C
                Wind Speed         : %.1f m/s
                Wind Direction     : %.1f °
                Rainfall           : %s
                """.formatted(
                weather.meetingKey(),
                weather.sessionKey(),
                weather.avgAirTemperature(),
                weather.avgHumidity(),
                weather.avgTrackTemperature(),
                weather.avgWindSpeed(),
                weather.avgWindDirection(),
                weather.isRainfall() ? "Yes" : "No"
        );
    }
}
