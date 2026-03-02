package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.service.driver.IDriverService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IDriverService driverService;

    @Test
    void getDriverByNameAndYear_returnsDriver() throws Exception {
        Driver driver = new Driver("Max", "Verstappen", 1, "Red Bull Racing");
        when(driverService.getDriverByNameAndYear("Max", "Verstappen", 2024))
                .thenReturn(driver);

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

