package htwsaar.nordpol.e2e;

import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.api.driver.IDriverClient;
import htwsaar.nordpol.dto.DriverDto;
import htwsaar.nordpol.dto.MeetingDto;
import htwsaar.nordpol.api.meeting.IMeetingClient;
import htwsaar.nordpol.api.meeting.MeetingClient;
import htwsaar.nordpol.cli.DriverCommand;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.repository.driver.IDriverRepo;
import htwsaar.nordpol.repository.driver.JooqDriverRepo;
import htwsaar.nordpol.repository.meeting.JooqMeetingRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.driver.IDriverService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.testutil.SqlSchemaLoader;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DriverCommandE2ETest {

    private Connection connection;
    private DSLContext create;


    private IDriverClient driverClient;
    private IMeetingClient meetingClient;

    private IMeetingService meetingService;
    private IDriverService driverService;
    private IDriverRepo driverRepo;
    private ICacheService cacheService;

    @BeforeEach
    void setup() throws Exception{
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);

        SqlSchemaLoader.loadSchema(create, "schema.sql");

        driverClient = mock(DriverClient.class);
        meetingClient = mock(MeetingClient.class);

        driverRepo = new JooqDriverRepo(create);
        cacheService = ApplicationContext.getInstance().cacheService();
        meetingService = new MeetingService(mock(JooqMeetingRepo.class), meetingClient, cacheService);


        driverService = new DriverService(
                driverRepo,
                driverClient,
                meetingService,
                cacheService
        );
    }

    @Test
    void driverCommand_printsDriverInformation_whenDriverExists() {
        int year = 2040;
        int meetingKey = 1234;
        when(driverClient.getDriverByName("Max", "Verstappen", meetingKey))
                .thenReturn(Optional.of(new DriverDto("Max", "Verstappen", 1, "Red Bull")));

        when(meetingClient.getMeetingsByYear(year))
                .thenReturn(List.of(new MeetingDto( "GER", "Germany", "Saarbrücken", meetingKey,"Lyoner GP", year, "www.this-gp-will-never-happen.gov")));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        int exitCode = new CommandLine(
                new DriverCommand(driverService)
        ).execute(
                "--first-name", "Max",
                "--last-name", "Verstappen",
                "--year", "2040"
        );

        assertThat(exitCode).isZero();

        String output = outputStream.toString();

        assertThat(output)
                .contains("Max Verstappen")
                .contains("Red Bull")
                .contains("1");

        System.setOut(originalOut);

        assertThat(driverRepo.getDriverByStartNumberForYear(1, year)).isPresent();
    }

    @Test
    void driverCommand_printsErrorMessage_whenDriverDoesNotExist() {
        when(meetingClient.getMeetingsByYear(2040))
                .thenReturn(List.of(new MeetingDto( "GER", "Germany", "Saarbrücken", 1234,"Lyoner GP", 2040, "www.this-gp-will-never-happen.gov")));

        when(driverClient.getDriverByName(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));

        int exitCode = new CommandLine(
                new DriverCommand(driverService)
        ).execute(
                "--first-name", "Darth",
                "--last-name", "Vader",
                "--year", "2040"
        );

        assertThat(exitCode).isEqualTo(2);
        assertThat(err.toString())
                .contains("Driver not found");

        System.setErr(originalErr);
    }
}