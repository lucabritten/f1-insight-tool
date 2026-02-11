package htwsaar.nordpol.cli;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.presentation.cli.DriverCommand;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.exception.DriverNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DriverCommandTest {

    private DriverService mockDriverService;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;

    private static final int ILLEGAL_ARG_ERROR = 2;
    private static final int BUSINESS_LOGIC_ERROR = 1;

    @BeforeEach
    void setup() {
        mockDriverService = mock(DriverService.class);

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

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessScenarios {

        @Test
        void printsFormattedDriver() {
            when(mockDriverService.getDriverByNameAndYear("Max", "Verstappen", Year.now().getValue()))
                    .thenReturn(new Driver("Max", "Verstappen", 1, "NED"));

            int exitCode = new CommandLine(
                    new DriverCommand(mockDriverService)
            ).execute("-fn", "Max", "-ln", "Verstappen");

            assertThat(exitCode).isZero();
            assertThat(outputStream.toString())
                    .contains("Verstappen");
        }

        @Test
        void shortAndLongOptions_work() {
            when(mockDriverService.getDriverByNameAndYear("Max", "Verstappen", 2024))
                    .thenReturn(new Driver("Max", "Verstappen", 1, "NED"));

            int exitCode = new CommandLine(new DriverCommand(mockDriverService))
                    .execute("--first-name", "Max", "--last-name", "Verstappen", "--year", "2024");

            assertThat(exitCode).isZero();
        }

        @Test
        void helpOption_printsUsage() {
            int exitCode = new CommandLine(
                    new DriverCommand(mockDriverService)
            ).execute("--help");

            assertThat(exitCode).isZero();
            assertThat(outputStream.toString()).contains("driver");
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        void missingLastName_causesError() {
            int exitCode = new CommandLine(
                    new DriverCommand(mockDriverService)
            ).execute("-fn", "Max");

            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
            assertThat(errorStream.toString())
                    .contains("Missing required option")
                    .contains("--last-name");
        }

        @Test
        void noArguments_printsUsageError() {
            int exitCode = new CommandLine(
                    new DriverCommand(mockDriverService)
            ).execute();

            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
        }

        @Test
        void unknownDriver_printsMessage() {
            when(mockDriverService.getDriverByNameAndYear("Foo", "Bar", 2024))
                    .thenThrow(new DriverNotFoundException("Foo", "Bar", 2024));

            int exitCode = new CommandLine(
                    new DriverCommand(mockDriverService)
            ).execute("-fn", "Foo", "-ln", "Bar", "-y", "2024");

            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
            assertThat(errorStream.toString())
                    .contains("not found");
        }

        @Test
        void invalidYear_printsErrorMessage() {
            when(mockDriverService.getDriverByNameAndYear("Max", "Verstappen", 1899))
                    .thenThrow(new IllegalArgumentException("No data for year: 1899"));

            int exitCode = new CommandLine(
                    new DriverCommand(mockDriverService)
            ).execute("-fn", "Max", "-ln", "Verstappen", "-s", "1899");

            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
            assertThat(errorStream.toString())
                    .contains("1899");
        }
    }
}
