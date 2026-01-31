package htwsaar.nordpol.util.rendering;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.util.formatting.GapFormatter;
import htwsaar.nordpol.util.formatting.TimeFormatter;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the session results table into the PDF. Keeps PDFBox usage local to avoid
 * coupling the main renderer to table details.
 */
public final class ResultsTableRenderer {

    private static final int TEXT_SIZE = 10;
    private static final String QUALI_HEADER = "%-4s %-22s %-4s %-8s %-8s %-8s %-8s";
    private static final String RACE_HEADER  = "%-4s %-22s %-4s %-8s";

    private final TimeFormatter timeFormatter;
    private final GapFormatter gapFormatter;

    public ResultsTableRenderer(TimeFormatter timeFormatter, GapFormatter gapFormatter) {
        this.timeFormatter = timeFormatter;
        this.gapFormatter = gapFormatter;
    }

    public float render(PDPageContentStream contentStream,
                        float x,
                        float y,
                        SessionResultWithContext resultsContext,
                        Map<Driver, List<Lap>> lapSeriesByDriver) throws IOException {
        if (resultsContext == null || resultsContext.results().isEmpty()) {
            return writeLine(contentStream, PDType1Font.HELVETICA, TEXT_SIZE, x, y, "No session results found.");
        }

        Map<Integer, Driver> driversByNumber = new HashMap<>();
        for (Driver driver : lapSeriesByDriver.keySet()) {
            driversByNumber.put(driver.driverNumber(), driver);
        }

        boolean qualifying = isQualifying(resultsContext.sessionName());
        String headerFormat = qualifying ? QUALI_HEADER : RACE_HEADER;

        y = writeLine(contentStream, PDType1Font.COURIER_BOLD, TEXT_SIZE, x, y,
                qualifying
                        ? String.format(headerFormat, "Pos", "Driver", "No.", "Q1(s)", "Q2(s)", "Q3(s)", "Gap")
                        : String.format(headerFormat, "Pos", "Driver", "No.", "Gap"));

        int rowIndex = 1;
        for (SessionResult result : resultsContext.results()) {
            Driver driver = driversByNumber.get(result.driverNumber());
            String driverName = driver == null
                    ? "Driver " + result.driverNumber()
                    : driver.firstName() + " " + driver.lastName();
            int position = result.position() > 0 ? result.position() : rowIndex;
            rowIndex++;

            if (qualifying) {
                y = writeLine(contentStream, PDType1Font.COURIER, TEXT_SIZE, x, y,
                        String.format(headerFormat,
                                position,
                                trim(driverName, 22),
                                result.driverNumber(),
                                timeFormatter.segment(result.duration(), 0),
                                timeFormatter.segment(result.duration(), 1),
                                timeFormatter.segment(result.duration(), 2),
                                gapFormatter.gap(result.gapToLeader(), result.dsq(), result.dns(), result.dnf())));
            } else {
                y = writeLine(contentStream, PDType1Font.COURIER, 10, x, y,
                        String.format(headerFormat,
                                position,
                                trim(driverName, 22),
                                result.driverNumber(),
                                gapFormatter.gap(result.gapToLeader(), result.dsq(), result.dns(), result.dnf())));
            }
        }
        return y;
    }

    private boolean isQualifying(SessionName sessionName) {
        return sessionName != null
                && sessionName.displayName().toLowerCase().contains("qualifying");
    }

    private float writeLine(PDPageContentStream contentStream,
                            PDType1Font font,
                            int size,
                            float x,
                            float y,
                            String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, size);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - (size + 4);
    }

    private String trim(String value, int maxLen) {
        if (value == null) return "";
        if (value.length() <= maxLen) return value;
        return value.substring(0, Math.max(0, maxLen - 3)) + "...";
    }
}
