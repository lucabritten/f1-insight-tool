package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "fastest-lap",
        description = "Print the fastest lap for a given location, year, and session",
        mixinStandardHelpOptions = true
)
public class FastestLapCommand implements Runnable {

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
            names = {"--sessionName", "-sn"},
            description = "The session name (e.g. Race, Qualifying, Practice)",
            required = true
    )
    private SessionName sessionName;

    @Option(
            names = {"--driverNumber", "-dn"},
            description = "Driver number to filter fastest lap within the session"
    )
    private Integer driverNumber;

    private final LapService lapService;

    public FastestLapCommand() {
        this(ApplicationContext.lapService());
    }

    public FastestLapCommand(LapService lapService) {
        this.lapService = lapService;
    }

    @Override
    public void run() {
        try {
            LapsWithContext fastestLap = driverNumber == null
                    ? lapService.getFastestLapByLocationYearAndSessionName(location, year, sessionName)
                    : lapService.getFastestLapByLocationYearSessionNameAndDriverNumber(location, year, sessionName,driverNumber);

            String output = Formatter.formatFastestLap(fastestLap);

            System.out.println(output);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
