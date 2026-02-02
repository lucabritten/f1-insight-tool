package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionReport;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.util.rendering.SessionReportRenderer;
import htwsaar.nordpol.service.report.SessionReportService;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.concurrent.Callable;

@Command(
    name = "session-report",
    description = {
        "Generate a PDF report for a Formula 1 session including results, weather, and lap comparisons.",
        "",
        "Examples:",
        "  session-report -l Monza -y 2024 -s Race",
        "  session-report -l 'Las Vegas' -s Qualifying --limit 10",
        "  session-report -l Austin -s Race -o reports/austin-race.pdf"
    },
    mixinStandardHelpOptions = true
)
public class SessionReportCommand implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(SessionReportCommand.class);

    @Option(names = {"--location", "-l"},
            description = "The meeting location (e.g., \"Monza\")",
            required = true
    )

    private String location;

    @Option(names = {"--year", "-y"},
            description = "Season year (default: current year)"
    )

    private int year = Year.now().getValue();

    @Option(
        names = {"--session", "-s"},
        description = "Session name (e.g. FP1, PRACTICE1, Qualifying, Race)",
        required = true,
        converter = SessionNameConverter.class
    )
    private SessionName sessionName;

    @Option(
        names = {"--limit", "-lim"},
        description = "Optional: Limit the report to the top N drivers in classification order"
    )
    private Integer limit;

    @Option(
        names = {"--output", "-o"},
        description = "Output path for the generated PDF file"
    )
    private String output;

    private final SessionReportService sessionReportService;
    private final SessionReportRenderer renderer;

    public SessionReportCommand(SessionReportService sessionReportService, SessionReportRenderer renderer) {
        this.sessionReportService = sessionReportService;
        this.renderer = renderer;
    }

    public SessionReportCommand() {
        this(ApplicationContext.sessionReportService(), new SessionReportRenderer());
    }

    @Override
    public Integer call() {
        try(ProgressBar progressBar = ApplicationContext.progressBar()) {

            SessionReport report = sessionReportService.buildReport(location,
                    year,
                    sessionName,
                    limit,
                    message -> {
                        progressBar.setExtraMessage(" | " + message);
                        progressBar.step();
                    }
            );
            Path outputPath = Paths.get(resolveOutputPath());
            renderer.render(report, outputPath);
            logger.info("Report written to: {}", outputPath.toAbsolutePath());
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

    private String resolveOutputPath() {
        if (output != null && !output.isBlank()) {
            return output;
        }
        String fileName = String.format("session-report-%s-%d-%s.pdf",
                slugify(location),
                year,
                slugify(sessionName.displayName()));
        return Paths.get("reports", fileName).toString();
    }

    private String slugify(String value) {
        if (value == null) {
            return "unknown";
        }
        String slug = value.trim().toLowerCase();
        slug = slug.replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("((^-)|(-$))", "");
        if (slug.isBlank()) {
            return "unknown";
        }
        return slug;
    }
}
