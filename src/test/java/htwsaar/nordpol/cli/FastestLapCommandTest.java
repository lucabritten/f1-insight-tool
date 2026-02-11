package htwsaar.nordpol.cli;

import htwsaar.nordpol.presentation.cli.FastestLapCommand;
import htwsaar.nordpol.presentation.view.FastestLapsWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.service.lap.LapService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static htwsaar.nordpol.domain.SessionName.RACE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FastestLapCommandTest {

    private LapService mockLapService;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private FastestLapsWithContext sampleLapContext;

    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setup() {
        originalOut = System.out;
        originalErr = System.err;

        mockLapService = mock(LapService.class);
        List<Driver> drivers = List.of(new Driver("Max", "Verstappen", 1, "Red Bull"));
        List<Lap> fastestLaps = List.of(new Lap(1, 8, 9161, 27.462, 38.938, 46.007, 112.497, false));

        sampleLapContext = new FastestLapsWithContext("Singapore Grand Prix", RACE, drivers, fastestLaps);

        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();

        System.setOut(new java.io.PrintStream(outputStream));
        System.setErr(new java.io.PrintStream(errorStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessScenarios {

        @Test
        public void printsFormattedLaps() {
            when(mockLapService.getFastestLapByLocationYearSessionNameAndDriverNumber("Singapore Grand Prix", 2025, RACE, 1, 1))
                    .thenReturn(sampleLapContext);

            int exitCode = new CommandLine(
                    new FastestLapCommand(mockLapService)
            ).execute("-l", "Singapore Grand Prix", "-y", "2025", "-s", "RACE", "-d", "1");

            assertThat(exitCode).isZero();
            assertThat(outputStream.toString()).contains("Race");
        }

        @Test
        public void missingDriverNumber_usesSessionFastestLap() {
            when(mockLapService.getFastestLapByLocationYearAndSessionName("Singapore", 2025, RACE, 1))
                    .thenReturn(sampleLapContext);

            int exitCode = new CommandLine(
                    new FastestLapCommand(mockLapService)
            ).execute("-l", "Singapore", "-y", "2025", "-s", "RACE");

            assertThat(exitCode).isZero();
            assertThat(outputStream.toString()).contains("Race");
        }

        @Test
        public void helpOption_printsUsage() {
            int exitCode = new CommandLine(
                    new FastestLapCommand(mockLapService)
            ).execute("--help");

            assertThat(exitCode).isZero();
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        public void noArguments_printsUsageError() {
            int exitCode = new CommandLine(
                    new FastestLapCommand(mockLapService)
            ).execute();
            assertThat(exitCode).isEqualTo(2);
        }

        @Test
        public void missingSessionName_printsUsageError() {
            int exitCode = new CommandLine(
                    new FastestLapCommand(mockLapService)
            ).execute("-l", "Singapore", "-y", "2025");

            assertThat(exitCode).isEqualTo(2);
            assertThat(errorStream.toString()).contains("Missing required option");
        }

        @Test
        void invalidParam_cachesDataNotFoundException() {
            when(mockLapService.getFastestLapByLocationYearAndSessionName("Saarbrücken", 2024, RACE, 1))
                    .thenThrow(MeetingNotFoundException.class);

            int exitCode = new CommandLine(
                    new FastestLapCommand(mockLapService)
            ).execute("-l", "Saarbrücken", "-y", "2024", "-s", "race", "-lim", "1");

            assertThat(errorStream.toString()).contains("Use --help");
            assertThat(exitCode).isEqualTo(2);
        }
    }
}
