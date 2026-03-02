package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.config.DatabaseInitializer;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SessionResultController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = htwsaar.nordpol.App.class
        )
)
class SessionResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISessionResultService sessionResultService;

    @MockBean
    private DatabaseInitializer databaseInitializer;

    @Test
    void getSessionResultsBySession_returnsContext() throws Exception {
        SessionResult result = new SessionResult(1, "Max Verstappen", 1,
                List.of("+0.000"), List.of(5400.0), false, false, false);
        SessionResultWithContext context = new SessionResultWithContext(
                "Dutch GP",
                SessionName.RACE,
                List.of(result)
        );

        when(sessionResultService.getResultByLocationYearAndSessionType("Zandvoort", 2024, SessionName.RACE))
                .thenReturn(context);

        mockMvc.perform(
                        get("/session-result")
                                .param("location", "Zandvoort")
                                .param("year", "2024")
                                .param("session", "Race")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingName").value("Dutch GP"))
                .andExpect(jsonPath("$.sessionName").value("RACE"))
                .andExpect(jsonPath("$.results[0].driverName").value("Max Verstappen"));
    }
}

