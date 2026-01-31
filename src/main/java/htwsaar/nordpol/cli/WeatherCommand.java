package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.service.weather.WeatherService;
import htwsaar.nordpol.util.formatting.CliFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Year;
import java.util.concurrent.Callable;

@Command(
        name = "weather-info",
        description = "Print averaged weather information for a specific location, year and session type",
        mixinStandardHelpOptions = true
)
public class WeatherCommand implements Callable<Integer> {

    @Option(
            names = {"--location", "-l"},
            description = "The location of the race (e.g. Austin)",
            required = true
    )
    private String location;

    @Option(
            names = {"--year", "-y"},
            description = "The season year"
    )
    private int year = Year.now().getValue();

    @Option(
            names = {"--session-name", "-sn"},
            description = "Session name (e.g. FP1, PRACTICE1, Quali, Race,...)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    private final WeatherService weatherService;

    public WeatherCommand(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public WeatherCommand() {
        this(ApplicationContext.weatherService());
    }

    @Override
    public Integer call() {
        try {
            WeatherWithContext weatherWithContext =
                    weatherService.getWeatherByLocationYearAndSessionName(
                            location,
                            year,
                            sessionName
                    );

            String output = CliFormatter.formatWeather(weatherWithContext);
            System.out.println(output);
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 2;
        }
    }
}