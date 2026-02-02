package htwsaar.nordpol.cli;

import picocli.CommandLine.Command;

/**
 * Exit codes:
 * <p> 0 - successful execution</p>
 * <p> 1 - invalid command usage/parameters</p>
 * <p> 2 - no data found for the given criteria</p>
 */
@Command(name = "f1-insight",
        mixinStandardHelpOptions = true,
        subcommands = {
            DriverCommand.class,
            WeatherCommand.class,
            FastestLapCommand.class,
            LapCommand.class,
            SessionReportCommand.class,
            SessionResultCommand.class
        }
)
public class F1CLI {

}
