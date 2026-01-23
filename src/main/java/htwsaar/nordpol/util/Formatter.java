package htwsaar.nordpol.util;

import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
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

    public static String formatLaps(LapsWithContext lapWithContext) {
        StringBuilder rows = new StringBuilder();
        for (Lap lap : lapWithContext.laps()) {
            rows.append(String.format(
                    "%-4d %-7.3f %-7.3f %-7.3f %-7.3f %s%n",
                    lap.lapNumber(),
                    lap.durationSector1(),
                    lap.durationSector2(),
                    lap.durationSector3(),
                    lap.lapDuration(),
                    lap.isPitOutLap() ? "Yes" : "No"
            ));
        }

        return """
       %s========== LAPS ==========%s
       Meeting  : %s
       Driver   : %s
       Session  : %s
       Laps     : %d
                
       Lap  S1(s)   S2(s)   S3(s)   Lap(s)  Pit Out
       %s
        """.formatted(
                BOLD, RESET,
                lapWithContext.meetingName(),
                lapWithContext.driverName(),
                lapWithContext.sessionName(),
                lapWithContext.laps().size(),
                rows
        );
    }

    public static String formatFastestLap(LapsWithContext fastestLap) {
        Lap lap = fastestLap.laps().getFirst();
        return """
                %s========== FASTEST LAP ==========%s
                Meeting  : %s
                Session  : %s
                Driver   : %s
                Lap #    : %d
                Lap(s)   : %.3f
                S1(s)    : %.3f
                S2(s)    : %.3f
                S3(s)    : %.3f
                """.formatted(
                BOLD, RESET,
                fastestLap.meetingName(),
                fastestLap.sessionName(),
                fastestLap.driverName(),
                lap.lapNumber(),
                lap.lapDuration(),
                lap.durationSector1(),
                lap.durationSector2(),
                lap.durationSector3()
        );
    }
}
