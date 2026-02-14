package htwsaar.nordpol.util.rendering;

import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.domain.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that PDF generation completes successfully and produces a non-empty file.
 * Detailed layout testing is intentionally omitted.
 */
public class SessionReportRendererTest {

    @Test
    void render_createsPDfFile() throws IOException {
        SessionReportRenderer renderer = new SessionReportRenderer();
        SessionReport report = validSessionReport();

        Path output = Files.createTempFile("session-report", ".pdf");

        renderer.render(report, output);

        assertThat(Files.exists(output)).isTrue();
        assertThat(Files.size(output)).isGreaterThan(0);

        Files.deleteIfExists(output);
    }

    private SessionReport validSessionReport() {
        String meetingName = "Austrian GP";
        SessionName sessionName = SessionName.RACE;

        Weather weather = new Weather(1234, 4321, 10, 50, false, 30, 2, 1);
        WeatherWithContext weatherWithContext = new WeatherWithContext(meetingName, "Austria", sessionName, weather);

        SessionResult sessionResult = new SessionResult(1,"Verstappen",1,List.of("0"), List.of(10.0), false, false, false);
        SessionResultWithContext sessionResultWithContext = new SessionResultWithContext(meetingName, sessionName, List.of(sessionResult));

        Driver driver = new Driver("Max", "Verstappen", 1, "Racing Cats");
        Lap lap = new Lap(1, 1, 1234, 1.0,1.0,1.0,3.0,false);

        var lapSeries = Map.of(driver, List.of(lap));


        return new SessionReport(
                meetingName,
                sessionName,
                2026,
                "Alps",
                weatherWithContext,
                sessionResultWithContext,
                lapSeries,
                "https://media.formula1.com/content/dam/fom-website/2018-redesign-assets/Flags%2016x9/singapore-flag.png"
        );
    }
}
