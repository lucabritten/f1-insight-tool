package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.util.formatting.CliFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Year;
import java.util.concurrent.Callable;

@Command(
        name = "fastest-laps",
        description = {
                "Print the fastest lap times for a given location, year and session.",
                "Optionally filter by driver and/or limit the number of results.",
                "",
                "Examples:",
                "  fastest-laps -l Austin -y 2025 -s Qualifying",
                "  fastest-laps -l Austin -s Race --limit 5",
                "  fastest-laps -l Austin -s Race -d 81 --limit 3"
        },
        mixinStandardHelpOptions = true
)
public class FastestLapCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(FastestLapCommand.class);

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
            names = {"--session", "-s"},
            description = "Session name (e.g. FP1, PRACTICE1, Qualifying, Race)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    @Option(
            names = {"--driver-number", "-d"},
            description = "Optional: driver number to filter fastest laps for a specific driver"
    )
    private Integer driverNumber;

    @Option(
            names = {"--limit", "-lim"},
            description = "Maximum number of fastest laps to display, sorted by lap time (default: 1)",
            defaultValue = "1"
    )
    private int limit;

    private final LapService lapService;

    public FastestLapCommand() {
        this(ApplicationContext.lapService());
    }

    public FastestLapCommand(LapService lapService) {
        this.lapService = lapService;
    }

    @Override
    public Integer call() {
        try {
            FastestLapsWithContext fastestLaps = driverNumber == null
                    ? lapService.getFastestLapByLocationYearAndSessionName(location, year, sessionName, limit)
                    : lapService.getFastestLapByLocationYearSessionNameAndDriverNumber(location, year, sessionName,driverNumber, limit);

            String output = CliFormatter.formatFastestLaps(fastestLaps);

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
