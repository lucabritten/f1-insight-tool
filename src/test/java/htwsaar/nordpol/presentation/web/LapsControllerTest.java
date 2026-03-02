package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.presentation.view.LapsWithContext;
import htwsaar.nordpol.service.lap.ILapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LapsController.class)
class LapsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ILapService lapService;

    @Test
    void getLapsByLocationYearSessionNameAndDriverNumber_returnsContext() throws Exception {
        Lap lap = new Lap(1, 1, 1001, 30.1, 29.8, 31.2, 91.1, false);
        LapsWithContext context = new LapsWithContext(
                "Austrian GP",
                "Max Verstappen",
                SessionName.RACE,
                List.of(lap)
        );

        when(lapService.getLapsByLocationYearSessionNameAndDriverNumber("Spielberg", 2024, SessionName.RACE, 1))
                .thenReturn(context);

        mockMvc.perform(
                        get("/laps")
                                .param("location", "Spielberg")
                                .param("year", "2024")
                                .param("session", "Race")
                                .param("driver_number", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingName").value("Austrian GP"))
                .andExpect(jsonPath("$.driverName").value("Max Verstappen"))
                .andExpect(jsonPath("$.sessionName").value("RACE"))
                .andExpect(jsonPath("$.laps[0].lapNumber").value(1));
    }
}

