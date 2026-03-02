package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.service.weather.IWeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IWeatherService weatherService;

    @Test
    void getWeatherByLocationYearAndSessionName_returnsContext() throws Exception {
        Weather weather = new Weather(2000, 1000, 25.0, 50.0, false, 35.0, 90.0, 10.0);
        WeatherWithContext context = new WeatherWithContext(
                "Spanish GP",
                "Spain",
                SessionName.QUALIFYING,
                weather
        );

        when(weatherService.getWeatherByLocationYearAndSessionName("Barcelona", 2024, SessionName.QUALIFYING))
                .thenReturn(context);

        mockMvc.perform(
                        get("/weather")
                                .param("location", "Barcelona")
                                .param("year", "2024")
                                .param("session", "Qualifying")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingName").value("Spanish GP"))
                .andExpect(jsonPath("$.countryName").value("Spain"))
                .andExpect(jsonPath("$.sessionName").value("QUALIFYING"))
                .andExpect(jsonPath("$.weather.avgAirTemperature").value(25.0));
    }
}

