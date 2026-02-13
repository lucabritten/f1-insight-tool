package htwsaar.nordpol.presentation.cli;
import htwsaar.nordpol.presentation.cli.converter.SessionNameConverter;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import htwsaar.nordpol.util.formatting.CliFormatter;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Year;
import java.util.concurrent.Callable;

@Command(
        name = "session-result",
        description = {
                "Print session results for a given location, year and session",
                "",
                "Examples:",
                "session-result -l Monza -y 2023 -s Race",
                "session-result --location Austin --year 2025 --session Qualifying"
        },
        mixinStandardHelpOptions = true
)
@Component
public class SessionResultCommand implements Callable<Integer> {

    @Option(
            names = {"--location", "-l"},
            description = "The location, where the session took place (e.g. Austin, Monza)",
            required = true
    )
    private String location;


    @Option(
            names = {"--session", "-s"},
            description = "Session name (e.g. FP1, PRACTICE1, Quali, Race,...)",
            required = true,
            converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    @Option(
            names = {"--year", "-y"},
            description = "The year the data is related too (default: current-year)"
    )
    private int year = Year.now().getValue();

    private final ISessionResultService sessionResultService;

    public SessionResultCommand(ISessionResultService sessionResultService) {
        this.sessionResultService = sessionResultService;
    }

    @Override
    public Integer call() {
        try {
            SessionResultWithContext result = sessionResultService.getResultByLocationYearAndSessionType(location, year, sessionName);
            String output = CliFormatter.formatSessionResults(result);
            System.out.println(output);
            return 0;
        } catch (DataNotFoundException e) {
            System.err.println("Requested data not found: " + e.getMessage());
            System.err.println("Use --help for usage information.");
            return 2;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }

}
