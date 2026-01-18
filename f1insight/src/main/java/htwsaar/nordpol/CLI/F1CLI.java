package htwsaar.nordpol.CLI;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "F1Insight",
        mixinStandardHelpOptions = true,
        subcommands = {DriverCommand.class}
)
public class F1CLI {

}
