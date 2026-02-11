package htwsaar.nordpol.presentation.cli;

import htwsaar.nordpol.presentation.cli.converter.SessionNameConverter;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionReport;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.report.ISessionReportService;
import htwsaar.nordpol.util.rendering.SessionReportRenderer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
@Component
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

    private final ISessionReportService sessionReportService;
    private final SessionReportRenderer renderer;

    public SessionReportCommand(ISessionReportService sessionReportService, SessionReportRenderer renderer) {
        this.sessionReportService = sessionReportService;
        this.renderer = renderer;
    }

    @Override
    public Integer call() {
        try(ProgressBar progressBar = new ProgressBarBuilder()
                .setTaskName("Generating session report")
                .setInitialMax(9)
                .setStyle(ProgressBarStyle.ASCII)
                .setMaxRenderedLength(100)
                .setUpdateIntervalMillis(100)
                .build()) {

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
            System.out.println("Report written to: " + outputPath.toAbsolutePath());
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
