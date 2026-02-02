package htwsaar.nordpol.cli;
import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.util.formatting.CliFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class SessionResultCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SessionResultCommand.class);

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

    public SessionResultCommand () {
        this(ApplicationContext.sessionResultService());
    }

    @Override
    public Integer call() {
        try {
            SessionResultWithContext result = sessionResultService.getResultByLocationYearAndSessionType(location, year, sessionName);
            String output = CliFormatter.formatSessionResults(result);
            logger.info(output);
            return 0;
        } catch (DataNotFoundException e) {
            logger.error("Requested data not found: {}", e.getMessage());
            logger.error("Use --help for usage information.");
            return 2;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return 1;
        }
    }

}
