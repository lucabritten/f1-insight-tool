package htwsaar.nordpol.cli;

import picocli.CommandLine.Command;

/**
 * Exit codes:
 * 0 - success
 * 2 - Entity not found / invalid input / BUSINESS LOGIC ERROR
 */
@Command(name = "f1-insight",
        mixinStandardHelpOptions = true,
        subcommands = {
            DriverCommand.class,
            WeatherCommand.class,
            FastestLapCommand.class,
            LapCommand.class,
            SessionReportCommand.class
        }
)
public class F1CLI {

}
