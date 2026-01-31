package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.converter.SessionNameConverter;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionReport;
import htwsaar.nordpol.util.rendering.SessionReportRenderer;
import htwsaar.nordpol.service.report.SessionReportService;
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
        description = "Generate a PDF report for a session with results, weather, and lap comparisons",
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
            description = "The season year (e.g., 2024)"
    )

    private int year = Year.now().getValue();

    @Option(
            names = {"--session-name", "-sn"},
            description = "Session name (e.g. FP1, PRACTICE1, Quali, Race,...)",
            required = true,
            converter = SessionNameConverter.class
    )

    private SessionName sessionName;

    @Option(
            names = {"--top-drivers", "-td"},
            description = "Limit the report to the first N drivers in result order"
    )

    private Integer topDrivers;

    @Option(
            names = {"--output", "-o"},
            description = "Output path for the generated PDF"
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
        try {
            SessionReport report = sessionReportService.buildReport(location, year, sessionName, topDrivers);
            Path outputPath = Paths.get(resolveOutputPath());
            renderer.render(report, outputPath);
            logger.info("Report written to: {}", outputPath.toAbsolutePath());
            return 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 2;
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
