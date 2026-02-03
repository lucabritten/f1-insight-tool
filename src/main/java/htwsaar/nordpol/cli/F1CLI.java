package htwsaar.nordpol.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

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

    @Option(names = "--debug", description = "Enable debug logging", scope = ScopeType.INHERIT)
    public void setDebug(boolean debug) {
        if (debug) {
            ((Logger) LoggerFactory.getLogger("htwsaar.nordpol.api")).setLevel(Level.DEBUG);
            ((Logger) LoggerFactory.getLogger("htwsaar.nordpol.cli")).setLevel(Level.DEBUG);
        }
    }
}
