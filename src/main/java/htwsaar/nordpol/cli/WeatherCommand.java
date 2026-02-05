package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.weather.IWeatherService;
import htwsaar.nordpol.util.formatting.CliFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Year;
import java.util.concurrent.Callable;

@Command(
        name = "weather",
        description = {
                "Print averaged weather information for a specific location, year and session type",
                "",
                "Examples:",
                "weather -l Monza -y 2023 -s Race",
                "weather --location Austin --year 2025 --session-name Qualifying"
        },
        mixinStandardHelpOptions = true
)
public class WeatherCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(WeatherCommand.class);

    @Option(
            names = {"--location", "-l"},
            description = "The location of the race (e.g. Austin)",
            required = true
    )
    private String location;

    @Option(
            names = {"--year", "-y"},
            description = "The year the data is related too (default: current-year)"
    )
    private int year = Year.now().getValue();

    @Option(
            names = {"--session-name", "-sn"},
            description = "Session name (e.g. FP1, PRACTICE1, Quali, Race,...)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    private final IWeatherService weatherService;

    public WeatherCommand(IWeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public WeatherCommand() {
        this(ApplicationContext.getInstance().weatherService());
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
            logger.info(output);
            return 0;
        } catch (DataNotFoundException e) {
            logger.error("Requested data not found: {}", e.getMessage());
            logger.error("Use --help for usage information.");
            return 2;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return 1;
        }
    }
}