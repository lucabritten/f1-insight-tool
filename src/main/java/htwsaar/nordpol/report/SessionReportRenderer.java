package htwsaar.nordpol.report;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.domain.Weather;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionReportRenderer {

    public void render(SessionReport report, Path outputPath) {
        if (report == null) {
            throw new IllegalArgumentException("report must not be null.");
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null.");
        }

        BufferedImage chartImage = buildLapChart(report);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;

            boolean chartOnNewPage = false;
            float chartWidth = 0f;
            float chartHeight = 0f;
            float chartY = 0f;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                y = writeLine(contentStream, PDType1Font.HELVETICA_BOLD, 18, margin, y, "Session Report");
                y = writeLine(contentStream, PDType1Font.HELVETICA, 11, margin, y,
                        String.format("Meeting: %s", report.meetingName()));
                y = writeLine(contentStream, PDType1Font.HELVETICA, 11, margin, y,
                        String.format("Session: %s", report.sessionName().displayName()));
                y = writeLine(contentStream, PDType1Font.HELVETICA, 11, margin, y,
                        String.format("Year: %d", report.year()));
                y = writeLine(contentStream, PDType1Font.HELVETICA, 11, margin, y,
                        String.format("Location: %s", report.location()));

                y -= 6;
                y = writeLine(contentStream, PDType1Font.HELVETICA_BOLD, 12, margin, y, "Weather Summary");
                y = writeWeatherBlock(contentStream, margin, y, report.weather());

                y -= 6;
                y = writeLine(contentStream, PDType1Font.HELVETICA_BOLD, 12, margin, y, "Session Results");
                y = writeResultsTable(contentStream, margin, y, report.sessionResults(), report.lapSeriesByDriver());

                if (chartImage != null) {
                    chartWidth = page.getMediaBox().getWidth() - (2 * margin);
                    chartHeight = chartWidth * ((float) chartImage.getHeight() / chartImage.getWidth());
                    chartY = y - chartHeight - 10f;
                    chartOnNewPage = chartY < margin;
                    if (!chartOnNewPage) {
                        y = writeLine(contentStream, PDType1Font.HELVETICA_BOLD, 12, margin, y, "Lap Time Comparison");
                        drawChartImage(document, contentStream, chartImage, margin, y - chartHeight - 10f, chartWidth, chartHeight);
                    }
                }
            }

            if (chartImage != null && chartOnNewPage) {
                page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                float newPageY = page.getMediaBox().getHeight() - margin;
                try (PDPageContentStream chartStream = new PDPageContentStream(document, page)) {
                    writeLine(chartStream, PDType1Font.HELVETICA_BOLD, 12, margin, newPageY, "Lap Time Comparison");
                    drawChartImage(document, chartStream, chartImage, margin, newPageY - chartHeight - 10f, chartWidth, chartHeight);
                }
            }

            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            document.save(outputPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to render session report.", e);
        }
    }

    private float writeWeatherBlock(PDPageContentStream contentStream, float x, float y, WeatherWithContext weatherWithContext) throws IOException {
        if (weatherWithContext == null || weatherWithContext.weather() == null) {
            return writeLine(contentStream, PDType1Font.HELVETICA, 10, x, y, "Weather data not available.");
        }
        Weather weather = weatherWithContext.weather();
        y = writeLine(contentStream, PDType1Font.HELVETICA, 10, x, y,
                String.format("Air: %.1f C | Track: %.1f C | Humidity: %.1f%%",
                        weather.avgAirTemperature(),
                        weather.avgTrackTemperature(),
                        weather.avgHumidity()));
        return writeLine(contentStream, PDType1Font.HELVETICA, 10, x, y,
                String.format("Wind: %.1f km/h @ %.0f deg | Rain: %s",
                        weather.avgWindSpeed(),
                        weather.avgWindDirection(),
                        weather.isRainfall() ? "Yes" : "No"));
    }

    private float writeResultsTable(PDPageContentStream contentStream,
                                    float x,
                                    float y,
                                    SessionResultWithContext resultsContext,
                                    Map<Driver, List<Lap>> lapSeriesByDriver) throws IOException {
        if (resultsContext == null || resultsContext.results().isEmpty()) {
            return writeLine(contentStream, PDType1Font.HELVETICA, 10, x, y, "No session results found.");
        }

        Map<Integer, Driver> driversByNumber = new HashMap<>();
        for (Driver driver : lapSeriesByDriver.keySet()) {
            driversByNumber.put(driver.driverNumber(), driver);
        }

        boolean qualifying = isQualifying(resultsContext.sessionName());
        String headerFormat = qualifying
                ? "%-4s %-22s %-4s %-8s %-8s %-8s %-8s"
                : "%-4s %-22s %-4s %-8s";
        y = writeLine(contentStream, PDType1Font.COURIER_BOLD, 10, x, y,
                qualifying
                        ? String.format(headerFormat, "Pos", "Driver", "No.", "Q1(s)", "Q2(s)", "Q3(s)", "Gap")
                        : String.format(headerFormat, "Pos", "Driver", "No.", "Gap"));

        int rowIndex = 1;
        for (SessionResult result : resultsContext.results()) {
            Driver driver = driversByNumber.get(result.driverNumber());
            String driverName = driver == null
                    ? "Driver " + result.driverNumber()
                    : driver.firstName() + " " + driver.lastName();
            int position = result.position() > 0 ? result.position() : rowIndex++;

            if (qualifying) {
                y = writeLine(contentStream, PDType1Font.COURIER, 10, x, y,
                        String.format(headerFormat,
                                position,
                                trim(driverName, 22),
                                result.driverNumber(),
                                formatDurationSegment(result, 0),
                                formatDurationSegment(result, 1),
                                formatDurationSegment(result, 2),
                                formatGapToLeader(result)));
            } else {
                y = writeLine(contentStream, PDType1Font.COURIER, 10, x, y,
                        String.format(headerFormat,
                                position,
                                trim(driverName, 22),
                                result.driverNumber(),
                                formatGapToLeader(result)));
            }
        }
        return y;
    }

    private String formatDurationSegment(SessionResult result, int index) {
        if (result.duration() != null
                && result.duration().size() > index
                && result.duration().get(index) != null) {
            return String.format("%.3f", result.duration().get(index));
        }
        return "-";
    }

    private String formatGapToLeader(SessionResult result) {
        String status = statusOf(result);
        if (status != null) {
            return status;
        }
        if (result.gapToLeader() == null || result.gapToLeader().isEmpty()) {
            return "-";
        }
        for (int i = result.gapToLeader().size() - 1; i >= 0; i--) {
            String gap = result.gapToLeader().get(i);
            if (gap != null && !gap.isBlank()) {
                String trimmed = gap.trim();
                if (trimmed.startsWith("+") || trimmed.startsWith("-")) {
                    return trimmed;
                }
                return "+" + trimmed;
            }
        }
        return "-";
    }

    private String statusOf(SessionResult result) {
        if (result.dsq()) return "DSQ";
        if (result.dns()) return "DNS";
        if (result.dnf()) return "DNF";
        return null;
    }

    private boolean isQualifying(SessionName sessionName) {
        return sessionName != null
                && sessionName.displayName().toLowerCase().contains("qualifying");
    }

    private BufferedImage buildLapChart(SessionReport report) {
        if (report.lapSeriesByDriver().isEmpty()) {
            return null;
        }

        XYChart chart = new XYChartBuilder()
                .width(900)
                .height(420)
                .title("Lap Time Comparison")
                .xAxisTitle("Lap")
                .yAxisTitle("Lap duration (s)")
                .build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setMarkerSize(3);
        chart.getStyler().setYAxisDecimalPattern("0.000");

        for (Map.Entry<Driver, List<Lap>> entry : report.lapSeriesByDriver().entrySet()) {
            List<Lap> laps = entry.getValue();
            if (laps == null || laps.isEmpty()) {
                continue;
            }
            List<Integer> xData = new ArrayList<>();
            List<Double> yData = new ArrayList<>();
            for (Lap lap : laps) {
                double duration = lap.lapDuration();
                if (duration <= 0 || Double.isNaN(duration) || Double.isInfinite(duration)) {
                    continue;
                }
                xData.add(lap.lapNumber());
                yData.add(duration);
            }
            Driver driver = entry.getKey();
            String seriesName = driver.lastName() + " #" + driver.driverNumber();
            chart.addSeries(seriesName, xData, yData);
        }

        if (chart.getSeriesMap().isEmpty()) {
            return null;
        }
        return BitmapEncoder.getBufferedImage(chart);
    }

    private void drawChartImage(PDDocument document,
                                PDPageContentStream contentStream,
                                BufferedImage chartImage,
                                float x,
                                float y,
                                float width,
                                float height) throws IOException {
        PDImageXObject pdImage = LosslessFactory.createFromImage(document, chartImage);
        contentStream.drawImage(pdImage, x, y, width, height);
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
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLen - 3)) + "...";
    }
}
