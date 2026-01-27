package htwsaar.nordpol.util;

import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionResult;
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
            rows.append(String.format("%-4d. %-22s %-7d %-7d %-7.3f%n",
                    rank++,
                    entry.driverName(),
                    entry.driverNumber(),
                    entry.lapNumber(),
                    entry.lapDuration()
            ));
        }
        return """
       %s========== FASTEST LAPS ==========%s
       Meeting  : %s
       Session  : %s
       Entries  : %d

       # Driver                 No.     Lap#    Lap(s)
       %s
       """.formatted(
               BOLD, RESET,
               context.location(),
               context.sessionName().displayName(),
               context.entries().size(),
               rows
       );
    }

    public static String formatSessionResults(SessionResultWithContext context) {
        boolean qualifying = isQualifying(context);

        StringBuilder rows = new StringBuilder();
        int rowIndex = 1;

        for (SessionResult r : context.results()) {
            String status = statusOf(r);
            int position = r.position() > 0 ? r.position() : rowIndex++;

            if (qualifying) {
                rows.append(String.format(
                        "%-4d %-6d %-8s %-8s %-8s %-8s%n",
                        position,
                        r.driverNumber(),
                        formatTime(r.duration(), 0),
                        formatTime(r.duration(), 1),
                        formatTime(r.duration(), 2),
                        qualifyingGap(r, status)
                ));
            } else {
                rows.append(String.format(
                        "%-4d %-6d %-8s%n",
                        position,
                        r.driverNumber(),
                        raceGap(r, status)
                ));
            }
        }

        return """
                %s%s
                Meeting: %s
                Session: %s
                Results: %d
                %s
                %s
                """.formatted(
                BOLD, RESET,
                context.meetingName(),
                context.sessionName().displayName(),
                context.results().size(),
                qualifying
                        ? "Pos  No     Q1(s)    Q2(s)    Q3(s)    Gap"
                        : "Pos  No     Gap",
                rows
        );
    }

    private static boolean isQualifying(SessionResultWithContext context) {
        return context.sessionName().displayName().toLowerCase().contains("qualifying");
    }

    private static String formatTime(java.util.List<Double> durations, int index) {
        return durations.size() > index && durations.get(index) != null
                ? durations.get(index).toString()
                : "-";
    }

    private static String statusOf(SessionResult r) {
        if (r.dsq()) return "DSQ";
        if (r.dns()) return "DNS";
        if (r.dnf()) return "DNF";
        return null;
    }

    private static String qualifyingGap(SessionResult r, String status) {
        if (status != null) return status;

        if (r.gapToLeader().size() > 2 && r.gapToLeader().get(2) != null)
            return "+" + r.gapToLeader().get(2);

        if (r.gapToLeader().size() > 1 && r.gapToLeader().get(1) != null)
            return "+" + r.gapToLeader().get(1);

        if (r.gapToLeader().size() > 0 && r.gapToLeader().get(0) != null)
            return "+" + r.gapToLeader().get(0);

        return "-";
    }

    private static String raceGap(SessionResult r, String status) {
        if (status != null) return status;

        if (!r.gapToLeader().isEmpty() && r.gapToLeader().get(0) != null)
            return "+" + r.gapToLeader().get(0);

        return "-";
    }
}
