package htwsaar.nordpol.presentation.cli;

import htwsaar.nordpol.presentation.cli.converter.SessionNameConverter;
import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.weather.IWeatherService;
import htwsaar.nordpol.util.formatting.CliFormatter;
import org.springframework.stereotype.Component;
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
@Component
public class WeatherCommand implements Callable<Integer> {

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
            names = {"--session-name", "-s"},
            description = "Session name (e.g. FP1, PRACTICE1, Quali, Race,...)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    private final IWeatherService weatherService;

    public WeatherCommand(IWeatherService weatherService) {
        this.weatherService = weatherService;
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
        } catch (DataNotFoundException e) {
            System.err.println("Requested data not found: " + e.getMessage());
            System.err.println("Use --help for usage information.");
            return 2;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}