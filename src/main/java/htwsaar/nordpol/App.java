package htwsaar.nordpol;

import htwsaar.nordpol.api.sessionresult.SessionResultClient;
import htwsaar.nordpol.cli.F1CLI;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
//        CommandLine commandLine = new CommandLine(new F1CLI());
//        int exitCode = commandLine.execute(args);
//        System.exit(exitCode);
        SessionResultService sessionResultService = ApplicationContext.sessionResultService();
        System.out.println(Formatter.formatSessionResults(sessionResultService.getResultByLocationYearAndSessionType("Austin", 2025, SessionName.RACE)));
    }
}
