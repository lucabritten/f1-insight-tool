package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.concurrent.Callable;


import java.time.Year;


@CommandLine.Command(name = "fastest-laps",
                    description = "print the fastest laps of a session",
                    mixinStandardHelpOptions = true)

public class FastestLapsCommand implements Callable<Integer> {
    @CommandLine.Option(
            names = {"--top-laps", "-tl"},
            description = "The number of fastest laps to show (default: 3)",
            defaultValue = "3"
    )
    private int topLaps;

    @CommandLine.Option(
            names = {"--location", "-l"},
            description = "The location of the race (e.g. Austin)",
            required = true
    )
    private String location;

    @CommandLine.Option(
            names = {"--year", "-y"},
            description = "The season year"
    )
    private int year = Year.now().getValue();

    @CommandLine.Option(
            names = {"--session-name", "-sn"},
            description = "Session name (e.g. FP1, PRACTICE1, Quali, Race,...)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    private final LapService lapService;


    public FastestLapsCommand(){
        this(ApplicationContext.lapService());
    }
    public FastestLapsCommand(LapService lapService){
        this.lapService = lapService;
    }

    @Override
    public Integer call(){
         try{
             FastestLapsWithContext fastestLaps = lapService.getFastestLapsByLocationYearAndSessionName(location, year, sessionName, topLaps);
             String output = Formatter.formatFastestLaps(fastestLaps);
             System.out.println(output);
             return 0;
         }
         catch (Exception e){
             System.err.println(e.getMessage());
             return 2;
         }
    }
}
