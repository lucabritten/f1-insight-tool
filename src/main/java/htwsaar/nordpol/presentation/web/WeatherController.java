package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.presentation.view.WeatherWithContext;
import htwsaar.nordpol.service.weather.IWeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final IWeatherService weatherService;

    public WeatherController(IWeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherWithContext getWeatherByLocationYearAndSessionName(
            @RequestParam(name = "location") String location,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "session") String session
    ) {
        SessionName sessionName = SessionName.fromString(session);
        return weatherService.getWeatherByLocationYearAndSessionName(location, year, sessionName);
    }
}
