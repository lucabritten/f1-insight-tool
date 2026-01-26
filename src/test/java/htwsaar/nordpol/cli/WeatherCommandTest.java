package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.service.weather.WeatherService;
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

    private static final int BUSINESS_LOGIC_ERROR = 2;

    @BeforeEach
    void setup() {
        mockWeatherService = mock(WeatherService.class);

        sampleWeatherContext = new WeatherWithContext(
                "Austin",
                "United States",
                SessionName.RACE,
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
        when(mockWeatherService.getWeatherByLocationYearAndSessionName("Austin", 2024, SessionName.RACE))
                .thenReturn(sampleWeatherContext);

        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Austin", "-y", "2024", "-sn", "Race");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("WEATHER");
    }

    @Test
    void missingSessionName_causesError() {
        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Austin", "-y", "2024");

        assertThat(exitCode).isEqualTo(BUSINESS_LOGIC_ERROR);
        assertThat(errorStream.toString())
                .contains("Missing required option")
                .contains("--session-name");
    }

    @Test
    void unknownLocation_printsErrorMessage() {
        when(mockWeatherService.getWeatherByLocationYearAndSessionName("Saarbrücken", 2024, SessionName.RACE))
                .thenThrow(new MeetingNotFoundException(2024, "Saarbrücken"));

        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Saarbrücken", "-y", "2024", "-sn", "Race");

        assertThat(exitCode).isEqualTo(BUSINESS_LOGIC_ERROR);
        assertThat(errorStream.toString())
                .contains("not found");
    }

    @Test
    void helpOption_printsUsage() {
        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("--help");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("weather-info");
    }

    @Test
    void shortAndLongOptions_work() {
        when(mockWeatherService.getWeatherByLocationYearAndSessionName("Austin", 2024, SessionName.RACE))
                .thenReturn(sampleWeatherContext);

        int exitCode = new CommandLine(new WeatherCommand(mockWeatherService))
                .execute("--location", "Austin", "--year", "2024", "--session-name", "Race");

        assertThat(exitCode).isZero();
    }

    @Test
    void invalidSessionName_printsErrorMessage() {
        int exitCode = new CommandLine(
                new WeatherCommand(mockWeatherService)
        ).execute("-l", "Austin", "-y", "2024", "-sn", "Cruising");

        assertThat(exitCode).isEqualTo(BUSINESS_LOGIC_ERROR);
        assertThat(errorStream.toString())
                .contains("Unknown session name")
                .contains("Cruising");
    }


    @Test
    void fieldSessionName_differentAliasesAreAcceptedForRace() {
        when(mockWeatherService.getWeatherByLocationYearAndSessionName("Austin", 2024, SessionName.RACE))
                .thenReturn(sampleWeatherContext);

        int exitCode = new CommandLine(new WeatherCommand(mockWeatherService))
                .execute("-l","Austin", "-y", "2024", "-sn", "GP");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("Austin");
        assertThat(outputStream.toString()).contains("Race");
    }

    @Test
    void fieldSessionName_differentAliasesAreAcceptedForQualifying() {
        WeatherWithContext sample = new WeatherWithContext(
                "Austin",
                "United States",
                SessionName.QUALIFYING,
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
        when(mockWeatherService.getWeatherByLocationYearAndSessionName("Austin", 2024, SessionName.QUALIFYING))
                .thenReturn(sample);

        int exitCode = new CommandLine(new WeatherCommand(mockWeatherService))
                .execute("-l","Austin", "-y", "2024", "-sn", "Quali");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("Austin");
        assertThat(outputStream.toString()).contains("Qualifying");
    }

    @Test
    void fieldSessionName_differentAliasesAreAcceptedForPractice() {
        WeatherWithContext sample = new WeatherWithContext(
                "Austin",
                "United States",
                SessionName.PRACTICE1,
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
        when(mockWeatherService.getWeatherByLocationYearAndSessionName("Austin", 2024, SessionName.PRACTICE1))
                .thenReturn(sample);

        int exitCode = new CommandLine(new WeatherCommand(mockWeatherService))
                .execute("-l","Austin", "-y", "2024", "-sn", "FP1");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("Austin");
        assertThat(outputStream.toString()).contains("Practice 1");
    }

    @Test
    void fieldSessionName_ignoresLetterCase() {
        when(mockWeatherService.getWeatherByLocationYearAndSessionName("Austin", 2024, SessionName.RACE))
                .thenReturn(sampleWeatherContext);

        int exitCode = new CommandLine(new WeatherCommand(mockWeatherService))
                .execute("-l","Austin", "-y", "2024", "-sn", "rAcE");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("Austin");
        assertThat(outputStream.toString()).contains("Race");
    }
}
