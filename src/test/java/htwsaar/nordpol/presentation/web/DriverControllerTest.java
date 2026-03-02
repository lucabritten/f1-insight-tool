package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.config.DatabaseInitializer;
import htwsaar.nordpol.service.driver.IDriverService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(
        controllers = DriverController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = htwsaar.nordpol.App.class
        )
)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IDriverService driverService;

    @MockBean
    private DatabaseInitializer databaseInitializer;

    @Test
    void getDriverByNameAndYear_returnsDriver() throws Exception {
        when(driverService.getDriverByNameAndYear("Max", "Verstappen", 2024))
                .thenReturn(new htwsaar.nordpol.domain.Driver("Max", "Verstappen", 1, "Red Bull Racing"));

        mockMvc.perform(
                        get("/driver")
                                .param("first_name", "Max")
                                .param("last_name", "Verstappen")
                                .param("year", "2024")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Max"))
                .andExpect(jsonPath("$.lastName").value("Verstappen"))
                .andExpect(jsonPath("$.driverNumber").value(1))
                .andExpect(jsonPath("$.teamName").value("Red Bull Racing"));
    }
}

