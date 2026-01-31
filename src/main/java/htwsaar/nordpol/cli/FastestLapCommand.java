package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.util.formatting.CliFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Year;
import java.util.concurrent.Callable;

@Command(
        name = "fastest-lap",
        description = "Print the fastest lap for a given location, year, and session",
        mixinStandardHelpOptions = true
)
public class FastestLapCommand implements Callable<Integer> {

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

    @Option(
            names = {"--driver-number", "-dn"},
            description = "Driver number to filter fastest lap within the session"
    )
    private Integer driverNumber;

    @Option(
            names = {"--top-laps", "-tl"},
            description = "The number of fastest laps to show (default: 1)",
            defaultValue = "1"
    )
    private int topLaps;

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
                    ? lapService.getFastestLapByLocationYearAndSessionName(location, year, sessionName, topLaps)
                    : lapService.getFastestLapByLocationYearSessionNameAndDriverNumber(location, year, sessionName,driverNumber, topLaps);

            String output = CliFormatter.formatFastestLaps(fastestLaps);

            System.out.println(output);
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 2;
        }
    }
}
