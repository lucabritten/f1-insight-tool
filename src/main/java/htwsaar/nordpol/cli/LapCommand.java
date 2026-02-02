package htwsaar.nordpol.cli;
import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.cli.view.LapsWithContext;
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

@Command(name = "laps",
        description = {
            "Print detailed lap information for a specific driver in a session.",
            "",
            "Examples:",
            "  laps -l Monza -y 2024 -s RACE -d 44",
            "  laps --location Monza --session-name FP1 --driver-number 1"
        },
        mixinStandardHelpOptions = true
)
public class LapCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(LapCommand.class);

    @Option(names = {"--location", "-l"},
            description = "The meeting location (e.g., Monza)",
            required = true
    )
    private String location;

    @Option(names = {"--year", "-y"},
            description = "The year the data is related too (default: current-year)"
    )
    private int year = Year.now().getValue();

    @Option(
            names = {"--session-name", "-s"},
            description = "Session name (e.g. FP1, PRACTICE1, Qualifying, Race)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    @Option(names = {"--driver-number", "-d"},
            description = "Driver number to show lap data for (e.g. 44)",
            required = true)
    private int driverNumber;


    private final LapService lapService;

    public LapCommand(LapService lapService) {
        this.lapService = lapService;
    }

    public LapCommand(){
        this(ApplicationContext.lapService());
    }

    @Override
    public Integer call() {
        try {
            LapsWithContext lap = lapService.getLapsByLocationYearSessionNameAndDriverNumber(location, year, sessionName, driverNumber);
            String output = CliFormatter.formatLaps(lap);
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
