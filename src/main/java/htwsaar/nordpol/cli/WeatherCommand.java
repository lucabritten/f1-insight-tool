package htwsaar.nordpol.cli;

import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.service.WeatherService;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "weather-info",
        description = "Print averaged weather information for a specific location, year and session type",
        mixinStandardHelpOptions = true
)
public class WeatherCommand implements Runnable {

    @Option(
            names = {"--location", "-l"},
            description = "The location of the race (e.g. Austin)",
            required = true
    )
    private String location;

    @Option(
            names = {"--year", "-y"},
            description = "The season year",
            defaultValue = "2024"
    )
    private int year;

    @Option(
            names = {"--sessionType", "-st"},
            description = "The session type (e.g. Race, Qualifying, Practice)",
            required = true
    )
    private String sessionType;

    private final WeatherService weatherService;

    public WeatherCommand(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public WeatherCommand() {
        this(ApplicationContext.weatherService());
    }

    @Override
    public void run() {
        try {
            Weather weather =
                    weatherService.getWeatherByLocationSeasonAndSessionType(
                            location,
                            year,
                            sessionType
                    );

            String output = Formatter.formatWeather(weather);
            System.out.println(output);

        } catch (Exception e) {
           throw new CommandLine.ExecutionException(
                   new CommandLine(this),
                   e.getMessage()
           );
        }
    }
}