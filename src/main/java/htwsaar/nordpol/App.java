package htwsaar.nordpol;

import htwsaar.nordpol.cli.F1CLI;
import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new F1CLI());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
