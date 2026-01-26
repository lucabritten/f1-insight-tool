package htwsaar.nordpol.cli;
import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "lap-info",
        description = "Print lap infos",
        mixinStandardHelpOptions = true
)
public class LapCommand implements Callable<Integer> {

    @Option(names = {"--location", "-l"},
            description = "The meeting location (e.g., \"Monza\")",
            required = true
    )
    private String location;

    @Option(names = {"--year", "-y"},
            description = "The season year (e.g., 2024)",
            defaultValue = "2024"
    )
    private int year;

    @Option(
            names = {"--session-name", "-sn"},
            description = "Session name (e.g. FP1, PRACTICE1, Quali, Race,...)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    @Option(names = {"--driver-number", "-d"},
            description = "The driver number (e.g., 44)",
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
            String output = Formatter.formatLaps(lap);
            System.out.println(output);
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 2;
        }
    }
}
