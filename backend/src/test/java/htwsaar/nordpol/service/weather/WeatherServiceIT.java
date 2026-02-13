package htwsaar.nordpol.service.weather;

import htwsaar.nordpol.dto.WeatherDto;
import htwsaar.nordpol.api.weather.IWeatherClient;
import htwsaar.nordpol.api.weather.WeatherClient;

import htwsaar.nordpol.repository.weather.IWeatherRepo;
import htwsaar.nordpol.repository.weather.JooqWeatherRepo;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.testutil.SqlSchemaLoader;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class WeatherServiceIT {

    private Connection connection;
    private DSLContext create;
    private IWeatherClient weatherClient;
    private IWeatherService weatherService;
    private IWeatherRepo weatherRepo;
    private ISessionService sessionService;
    private IMeetingService meetingService;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);

        SqlSchemaLoader.loadSchema(create, "schema.sql");

        weatherClient = mock(WeatherClient.class);
        weatherRepo = new JooqWeatherRepo(create);
        sessionService = mock(SessionService.class);
        meetingService = mock(MeetingService.class);


        weatherService = new WeatherService(
                weatherClient,
                weatherRepo,
                sessionService,
                meetingService
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void savesAveragedWeatherData_toDatabase() {
        int meetingKey = 1000;
        int sessionKey = 2000;

        when(weatherClient.getWeatherDataByMeetingKeyAndSessionKey(meetingKey, sessionKey))
                .thenReturn(List.of(
                        new WeatherDto(sessionKey, meetingKey, 30, 71,0,40, 10, 20),
                        new WeatherDto(sessionKey, meetingKey, 40, 70,1,46, 20, 40)
                ));

        weatherService.getWeatherByMeetingAndSessionKey(meetingKey, sessionKey);

        var stored =
                weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(meetingKey, sessionKey);

        assertThat(stored).isPresent();

        var storedData = stored.get();
        assertThat(storedData.air_temperature()).isEqualTo(35);
        assertThat(storedData.humidity()).isEqualTo(70.5);
        assertThat(storedData.rainfall()).isEqualTo(1);
    }

    @Test
    void usesDatabaseOnSecondCall_andDoesNotCallApiAgain() {
        int meetingKey = 1000;
        int sessionKey = 2000;

        when(weatherClient.getWeatherDataByMeetingKeyAndSessionKey(meetingKey, sessionKey))
                .thenReturn(List.of(
                        new WeatherDto(sessionKey, meetingKey, 20.0, 60.0, 0, 30.0, 90.0, 5.0)
                ));

        weatherService.getWeatherByMeetingAndSessionKey(meetingKey, sessionKey);
        weatherService.getWeatherByMeetingAndSessionKey(meetingKey, sessionKey);

        var stored =
                weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(meetingKey, sessionKey);
        assertThat(stored.isPresent());

        verify(weatherClient, times(1))
                .getWeatherDataByMeetingKeyAndSessionKey(meetingKey, sessionKey);
    }
}