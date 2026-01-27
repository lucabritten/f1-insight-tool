package htwsaar.nordpol.util;

import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.cli.view.FastestLapEntry;

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
                Team         : %s
                """.formatted(
                        BOLD, RESET,
                    driver.firstName(),
                    driver.lastName(),
                    driver.driverNumber(),
                    driver.teamName()
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
                weatherWithContext.sessionName().displayName(),
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
                lapWithContext.sessionName().displayName(),
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
                fastestLap.sessionName().displayName(),
                fastestLap.driverName(),
                lap.lapNumber(),
                lap.lapDuration(),
                lap.durationSector1(),
                lap.durationSector2(),
                lap.durationSector3()
        );
    }
    public static String formatFastestLaps(FastestLapsWithContext context) {

        StringBuilder rows = new StringBuilder();
        int rank = 1;

        for(FastestLapEntry entry : context.entries()){
            rows.append(String.format("%-4d. %-22s %-7d %-7d %-7.3f\n",
                    rank++,
                    entry.driverName(),
                    entry.driverNumber(),
                    entry.lapNumber(),
                    entry.lapDuration()
            ));
        }
        return """ 
               %s========== FASTEST LAP ==========%s
               Meeting  : %s
               Session  : %s
               Entries   : %d
               
               # Driver                 No.     Lap#    Lap(s)
               %s
               """.formatted(
                       BOLD, context.entries().size(), RESET,
                       context.location(),
                       context.sessionName().displayName(),
                       context.entries().size(),
                       rows
        );
    }
}
