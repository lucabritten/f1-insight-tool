package htwsaar.nordpol.cli;

import picocli.CommandLine.Command;

@Command(name = "F1Insight",
        mixinStandardHelpOptions = true,
        subcommands = {
            DriverCommand.class,
            WeatherCommand.class,
            FastestLapCommand.class
        }
)
public class F1CLI {

}
