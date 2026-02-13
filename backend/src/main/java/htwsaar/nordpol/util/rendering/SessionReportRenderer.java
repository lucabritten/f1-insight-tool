package htwsaar.nordpol.util.rendering;

import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.util.formatting.GapFormatter;
import htwsaar.nordpol.util.formatting.TimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SessionReportRenderer {

    private final static float MARGIN = 50f;
    private final static float NEW_PARAGRAPH = 5f;
    private final static float COLUMN_GAP = 20f;
    private final static int LINE_SPACING = 4;

    private final static int HEADER_SIZE = 16;
    private final static int SUB_HEADER_SIZE = 14;
    private final static int TEXT_SIZE = 10;

    private final static float CHART_TITLE_OFFSET = 10f;
    private final static float CHART_SECTION_SPACING = 2.5f * NEW_PARAGRAPH;

    private final static int FLAG_WIDTH = 64;
    private final static int FLAG_HEIGHT = 40;
    private final static float IMAGE_BORDER_WIDTH = 0.5f;

    private final static PDType1Font HELVETICA = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final static PDType1Font HELVETICA_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final ResultsTableRenderer resultsTableRenderer = new ResultsTableRenderer(new TimeFormatter(), new GapFormatter());
    private final LapChartBuilder chartBuilder = new XChartLapChartBuilder();

    public void render(SessionReport report, Path outputPath) {
        if (report == null) {
            throw new IllegalArgumentException("report must not be null.");
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null.");
        }

        BufferedImage chartImage = chartBuilder.build(report);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            
            float y = page.getMediaBox().getHeight() - MARGIN;

            boolean chartOnNewPage = false;
            float chartWidth = 0f;
            float chartHeight = 0f;
            float chartY;

            float pageWidth = page.getMediaBox().getWidth();
            float contentWidth = pageWidth - (2 * MARGIN);

            float leftColWidth = contentWidth * 0.55f;

            float leftX = MARGIN;
            float rightX = MARGIN + leftColWidth + COLUMN_GAP;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                y = writeLine(contentStream, HELVETICA_BOLD, HEADER_SIZE, leftX, y, "Auto-generated " + report.sessionName().displayName() + " Report");

                float leftY = writeSessionDetails(contentStream,leftX, y, report);
                float rightY = y;

                BufferedImage flagImage = loadImageFromUrl(report.countryFlagUrl());
                float flagX = pageWidth - MARGIN - FLAG_WIDTH;
                rightY = drawImage(document, contentStream, flagImage, flagX, rightY, FLAG_WIDTH, FLAG_HEIGHT);

                writeWeatherBlock(contentStream, rightX, rightY, report.weather());
                y = Math.min(leftY, rightY) - NEW_PARAGRAPH;

                //Session-Table
                y = writeLine(contentStream, HELVETICA_BOLD, SUB_HEADER_SIZE, leftX, y, report.sessionName().displayName() + " Results");
                y = resultsTableRenderer.render(contentStream, MARGIN, y, report.sessionResults(), report.lapSeriesByDriver());

                if (chartImage != null) {
                    chartWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
                    chartHeight = chartWidth * ((float) chartImage.getHeight() / chartImage.getWidth());
                    chartY = y - chartHeight;
                    chartOnNewPage = chartY < MARGIN;
                    if (!chartOnNewPage) {
                        y -= CHART_SECTION_SPACING;
                        y = writeLine(contentStream, HELVETICA_BOLD, SUB_HEADER_SIZE, MARGIN, y, "Lap Time Comparison");
                        drawImage(document, contentStream, chartImage, MARGIN, y - chartHeight + CHART_TITLE_OFFSET, chartWidth, chartHeight);
                    }
                }
            }

            if (chartImage != null && chartOnNewPage) {
                page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                float newPageY = page.getMediaBox().getHeight() - MARGIN;
                try (PDPageContentStream chartStream = new PDPageContentStream(document, page)) {
                    writeLine(chartStream, HELVETICA_BOLD, SUB_HEADER_SIZE, MARGIN, newPageY, "Lap Time Comparison");
                    drawImage(document, chartStream, chartImage, MARGIN, newPageY - chartHeight - CHART_TITLE_OFFSET, chartWidth, chartHeight);
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
    private float writeSessionDetails(PDPageContentStream contentStream, float leftX, float leftY, SessionReport report) throws IOException{
        leftY = writeLine(contentStream, HELVETICA_BOLD, SUB_HEADER_SIZE, leftX, leftY, "Session Details:");
        leftY = writeLine(contentStream, HELVETICA, TEXT_SIZE, leftX, leftY,
                String.format("Meeting: %s", report.meetingName()));
        leftY = writeLine(contentStream, HELVETICA, TEXT_SIZE, leftX, leftY,
                String.format("Session: %s", report.sessionName().displayName()));
        leftY = writeLine(contentStream, HELVETICA, TEXT_SIZE, leftX, leftY,
                String.format("Year: %d", report.year()));
        leftY = writeLine(contentStream, HELVETICA, TEXT_SIZE, leftX, leftY,
                String.format("Location: %s", report.location()));

        return leftY;
    }

    private float writeWeatherBlock(PDPageContentStream contentStream, float x, float y, WeatherWithContext weatherWithContext) throws IOException {
        if (weatherWithContext == null || weatherWithContext.weather() == null) {
            return writeLine(contentStream, HELVETICA, TEXT_SIZE, x, y, "Weather data not available.");
        }
        Weather weather = weatherWithContext.weather();
        y = writeLine(contentStream, HELVETICA_BOLD, SUB_HEADER_SIZE, x, y, "Weather Summary");
        y = writeLine(contentStream, HELVETICA, TEXT_SIZE, x, y,
                String.format("Air: %.1f C | Track: %.1f C | Humidity: %.1f%%",
                        weather.avgAirTemperature(),
                        weather.avgTrackTemperature(),
                        weather.avgHumidity()));
        return writeLine(contentStream, HELVETICA, TEXT_SIZE, x, y,
                String.format("Wind: %.1f km/h @ %.0f deg | Rain: %s",
                        weather.avgWindSpeed(),
                        weather.avgWindDirection(),
                        weather.isRainfall() ? "Yes" : "No"));
    }

    private float drawImage(PDDocument document,
                                PDPageContentStream contentStream,
                                BufferedImage image,
                                float x,
                                float y,
                                float width,
                                float height) throws IOException {
        if(image != null) {
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
            contentStream.drawImage(pdImage, x, y, width, height);

            contentStream.setLineWidth(IMAGE_BORDER_WIDTH);
            contentStream.setStrokingColor(Color.GRAY);
            contentStream.addRect(x, y, width, height);
            contentStream.stroke();
            return y - height + LINE_SPACING;
        }
        return y;
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
        return y - (size + LINE_SPACING);
    }

    private BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
        try(InputStream inputStream = URI.create(imageUrl).toURL().openStream()) {
            return ImageIO.read(inputStream);
        }
    }
}
