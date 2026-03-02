package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.presentation.web.dto.SessionReportDto;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.service.report.ISessionReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionReportController.class)
class SessionReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISessionReportService reportService;

    @Test
    void buildReport_returnsSessionReportDto() throws Exception {
        Weather weather = new Weather(2000, 1000, 25.0, 60.0, false, 35.0, 180.0, 12.0);
        WeatherWithContext weatherWithContext = new WeatherWithContext(
                "Monaco GP",
                "Monaco",
                SessionName.QUALIFYING,
                weather
        );

        SessionResult sessionResult = new SessionResult(
                16,
                "Charles Leclerc",
                1,
                List.of("+0.000"),
                List.of(72.0),
                false,
                false,
                false
        );
        SessionResultWithContext resultWithContext = new SessionResultWithContext(
                "Monaco GP",
                SessionName.QUALIFYING,
                List.of(sessionResult)
        );

        Driver driver = new Driver("Charles", "Leclerc", 16, "Ferrari");
        Lap lap = new Lap(16, 1, 2000, 24.0, 24.0, 24.0, 72.0, false);

        SessionReport report = new SessionReport(
                "Monaco GP",
                SessionName.QUALIFYING,
                2024,
                "Monte Carlo",
                weatherWithContext,
                resultWithContext,
                Map.of(driver, List.of(lap)),
                "https://example.com/flag.png"
        );

        when(reportService.buildReport("Monte Carlo", 2024, SessionName.QUALIFYING, 10, org.mockito.ArgumentMatchers.any()))
                .thenReturn(report);

        mockMvc.perform(
                        get("/report")
                                .param("location", "Monte Carlo")
                                .param("year", "2024")
                                .param("session", "Qualifying")
                                .param("top_drivers", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingName").value("Monaco GP"))
                .andExpect(jsonPath("$.sessionName").value("QUALIFYING"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.lapSeries[0].driver.firstName").value("Charles"))
                .andExpect(jsonPath("$.lapSeries[0].laps[0].lapNumber").value(1));
    }
}

