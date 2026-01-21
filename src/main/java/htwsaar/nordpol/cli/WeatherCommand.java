package htwsaar.nordpol.cli;

import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.exception.WeatherNotFoundException;
import htwsaar.nordpol.service.WeatherService;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "weather-info",
        description = "Print averaged weather information for a specific meeting and session",
        mixinStandardHelpOptions = true
)
public class WeatherCommand implements Runnable {

    @Option(
            names = {"--meetingKey", "-mk"},
            description = "The meeting key",
            required = true
    )
    private int meetingKey;

    @Option(
            names = {"--sessionKey", "-sk"},
            description = "The session key",
            required = true
    )
    private int sessionKey;

    private final WeatherService weatherService;

    public WeatherCommand(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public WeatherCommand() {
        this(ApplicationContext.weatherService());
    }

    @Override
    public void run() {
        try {
            Weather weather = weatherService.getWeatherByMeetingAndSessionKey(meetingKey, sessionKey);
            String output = Formatter.formatWeather(weather);
            System.out.println(output);

        } catch (WeatherNotFoundException e) {
            System.out.println("No weather data found for meetingKey=" + meetingKey
                    + " and sessionKey=" + sessionKey + ".");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}