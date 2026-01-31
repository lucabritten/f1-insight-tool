package htwsaar.nordpol.cli;
import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.concurrent.Callable;

@Command(
        name = "session-result",
        description = "Print session results for a given location, year and session",
        mixinStandardHelpOptions = true
)

public class SessionResultCommand implements Callable<Integer> {

    @Option(
            names = {"--location", "-l"},
            description = "Race location",
            required = true
    )
    private String location;


    @Option(
            names = {"session", "-s"},
            description = "Session name",
            required = true
    )
    private SessionName sessionName;

    @Option(
            names = {"--year", "-y"},
            description = "Season year"
    )
    private int year;

    private final ISessionResultService sessionResultService;

    public SessionResultCommand(ISessionResultService sessionResultService) {
        this.sessionResultService = sessionResultService;
    }

    public SessionResultCommand () {
        this(ApplicationContext.sessionResultService());
    }

    @Override
    public Integer call() {
        try {
            SessionResultWithContext result = sessionResultService.getResultByLocationYearAndSessionType(location, year, sessionName);
            System.out.println(Formatter.formatSessionResults(result));
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 2;
        }
    }

}
