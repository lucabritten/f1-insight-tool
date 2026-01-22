package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.service.WeatherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeatherCommandTest {

    private WeatherService mockWeatherService;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private WeatherWithContext sampleWeatherContext;

    @BeforeEach
    void setup() {
        mockWeatherService = mock(WeatherService.class);

        sampleWeatherContext = new WeatherWithContext(
                "Austin",
                "United States",
                "Race",
                new Weather(
                1234,
                4321,
                20,
                50,
                true,
                30,
                10,
                10
                 )
        );

        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    void weatherInfo_printsFormattedWeather() {
        when(mockWeatherService.getWeatherByLocationSeasonAndSessionName("Austin", 2024, SessionName.RACE))
                .thenReturn(sampleWeatherContext);

        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Austin", "-y", "2024", "-st", "Race");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("WEATHER");
    }

    @Test
    void missingsessionName_causesError() {
        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Austin", "-y", "2024");

        assertThat(exitCode).isNotZero();
        assertThat(errorStream.toString())
                .contains("Missing required option")
                .contains("--sessionName");
    }

    @Test
    void unknownLocation_printsMessage() {
        when(mockWeatherService.getWeatherByLocationSeasonAndSessionName("Saarbrücken", 2024, SessionName.RACE))
                .thenThrow(new MeetingNotFoundException(2024, "Saarbrücken"));

        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Saarbrücken", "-y", "2024", "-st", "Race");

        assertThat(exitCode).isNotZero();
        assertThat(errorStream.toString())
                .contains("not found");
    }

    @Test
    void helpOption_printsUsage() {
        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("--help");

        assertThat(exitCode).isEqualTo(0);
        assertThat(outputStream.toString()).contains("weather-info");
    }

    @Test
    void shortAndLongOptions_work() {
        when(mockWeatherService.getWeatherByLocationSeasonAndSessionName("Austin", 2024, SessionName.RACE))
                .thenReturn(sampleWeatherContext);

        int exitCode = new CommandLine(new WeatherCommand(mockWeatherService))
                .execute("--location", "Austin", "--year", "2024", "--sessionName", "Race");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void invalidsessionName_printsErrorMessage() {
        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Austin", "-y", "2024", "-st", "Cruising");

        assertThat(exitCode).isNotZero();
        assertThat(errorStream.toString())
                .contains("Unknown session name")
                .contains("Cruising");
    }
}
