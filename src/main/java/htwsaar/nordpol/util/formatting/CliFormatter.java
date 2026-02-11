package htwsaar.nordpol.util.formatting;

import htwsaar.nordpol.presentation.view.LapsWithContext;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.presentation.view.FastestLapsWithContext;

public class CliFormatter {

    private CliFormatter(){

    }

    private static final String BOLD = "\u001B[1m";
    private static final String RESET = "\u001B[0m";

    // Delegates to the small, focused formatters to avoid duplicate logic
    private static final TimeFormatter TIME = new TimeFormatter();
    private static final GapFormatter GAP = new GapFormatter();

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

        int driversSize = context.drivers() == null ? 0 : context.drivers().size();
        int lapsSize = context.fastestLaps() == null ? 0 : context.fastestLaps().size();
        int count = Math.min(driversSize, lapsSize);

        for (int i = 0; i < count; i++) {
            Driver driver = context.drivers().get(i);
            Lap lap = context.fastestLaps().get(i);

            String driverName = (driver.firstName() + " " + driver.lastName()).trim();

            rows.append(String.format(
                    "%-4d %-22s %-7d %-7d %-7.3f%n",
                    i + 1,
                    driverName,
                    driver.driverNumber(),
                    lap.lapNumber(),
                    lap.lapDuration()
            ));
        }

        String mismatchNote = (driversSize != lapsSize)
                ? String.format("(warning: drivers=%d, laps=%d)", driversSize, lapsSize)
                : "";

        return """
       %s========== FASTEST LAPS ==========%s
       Meeting  : %s
       Session  : %s
       Entries  : %d %s

       #    Driver                 No.     Lap#    Lap(s)
       %s
       """.formatted(
                BOLD, RESET,
                context.meetingName(),
                context.sessionName().displayName(),
                count,
                mismatchNote.isEmpty() ? "" : (" " + mismatchNote),
                rows
        );
    }

    public static String formatSessionResults(SessionResultWithContext context) {
        boolean qualifying = isQualifying(context);

        StringBuilder rows = new StringBuilder();
        int rowIndex = 1;

        for (SessionResult r : context.results()) {
            int position = r.position() > 0 ? r.position() : rowIndex++;

            if (qualifying) {
                rows.append(String.format(
                        "%-4d %-6d %-8s %-8s %-8s %-8s%n",
                        position,
                        r.driverNumber(),
                        TIME.segment(r.duration(), 0),
                        TIME.segment(r.duration(), 1),
                        TIME.segment(r.duration(), 2),
                        GAP.gap(r.gapToLeader(), r.dsq(), r.dns(), r.dnf())
                ));
            } else {
                rows.append(String.format(
                        "%-4d %-6d %-8s%n",
                        position,
                        r.driverNumber(),
                        GAP.gap(r.gapToLeader(), r.dsq(), r.dns(), r.dnf())
                ));
            }
            rowIndex++;
        }

        return """
                %s========== %s RESULTS ==========%s
                Meeting: %s
                Session: %s
                Results: %d
                %s
                %s
                """.formatted(
                BOLD,
                context.sessionName().displayName().toUpperCase(),
                RESET,
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
        return context.sessionName().displayName()
                .toLowerCase()
                .contains("qualifying");
    }
}
