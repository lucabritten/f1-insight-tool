package htwsaar.nordpol.cli;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.service.DriverService;
import htwsaar.nordpol.exception.DriverNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DriverCommandTest {

    private DriverService mockDriverService;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;

    @BeforeEach
    void setup(){
        mockDriverService = mock(DriverService.class);

        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown(){
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    void driverInfo_printsFormattedDriver(){
        when(mockDriverService.getDriverByNameAndYear("Max", "Verstappen", 2024))
                .thenReturn(new Driver("Max", "Verstappen", 1, "NED"));

        int exitCode = new CommandLine(
                new DriverCommand(mockDriverService)
        ).execute("-fn", "Max", "-ln", "Verstappen");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString())
                .contains("Verstappen");
    }

    @Test
    void missingLastName_causesError(){
        int exitCode = new CommandLine(
                new DriverCommand(mockDriverService)
        ).execute("-fn", "Max");

        assertThat(exitCode).isEqualTo(2);
        assertThat(errorStream.toString())
                .contains("Missing required option")
                .contains("--lastName");
    }

    @Test
    void noArguments_printsUsageError(){
        int exitCode = new CommandLine(
                new DriverCommand(mockDriverService)
        ).execute();

        assertThat(exitCode).isEqualTo(2);
    }

    @Test
    void unknownDriver_printsMessage() {
        when(mockDriverService.getDriverByNameAndYear("Foo", "Bar", 2024))
                .thenThrow(new DriverNotFoundException("Foo", "Bar", 2024));

        int exitCode = new CommandLine(
                new DriverCommand(mockDriverService)
        ).execute("-fn", "Foo", "-ln", "Bar");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString())
                .contains("not found");
    }

    @Test
    void helpOption_printsUsage() {
        int exitCode = new CommandLine(
                new DriverCommand(mockDriverService)
        ).execute("--help");

        assertThat(exitCode).isZero();
        assertThat(outputStream.toString()).contains("driver-info");
    }

    @Test
    void shortAndLongOptions_work() {
        when(mockDriverService.getDriverByNameAndYear("Max", "Verstappen", 2024))
                .thenReturn(new Driver("Max", "Verstappen", 1, "NED"));

        int exitCode = new CommandLine(new DriverCommand(mockDriverService))
                .execute("--firstName", "Max", "--lastName", "Verstappen", "--year", "2024");

        assertThat(exitCode).isZero();
    }

    @Test
    void invalidYear_printsErrorMessage() {
        when(mockDriverService.getDriverByNameAndYear("Max", "Verstappen", 1899))
                .thenThrow(new IllegalArgumentException("No data for year: 1899"));

        int exitCode = new CommandLine(
                new DriverCommand(mockDriverService)
        ).execute("-fn", "Max", "-ln", "Verstappen", "-s", "1899");

        assertThat(exitCode).isNotZero();
        assertThat(errorStream.toString())
                .contains("1899");
    }
}
