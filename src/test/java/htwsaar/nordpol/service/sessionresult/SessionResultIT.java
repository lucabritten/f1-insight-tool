package htwsaar.nordpol.service.sessionresult;

import htwsaar.nordpol.dto.SessionResultDto;
import htwsaar.nordpol.api.sessionresult.ISessionResultClient;
import htwsaar.nordpol.api.sessionresult.SessionResultClient;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;

import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.repository.sessionresult.ISessionResultRepo;
import htwsaar.nordpol.repository.sessionresult.JooqSessionResultRepo;
import htwsaar.nordpol.service.CacheService;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.driver.IDriverService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
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

public class SessionResultIT {

    private Connection connection;
    private DSLContext create;

    private ISessionResultClient sessionResultClient;
    private ISessionResultRepo sessionResultRepo;
    private ISessionResultService sessionResultService;

    private IMeetingService meetingService;
    private ISessionService sessionService;
    private IDriverService driverService;
    private ICacheService cacheService;

    @BeforeEach
    void setup() throws Exception{
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);

        SqlSchemaLoader.loadSchema(create, "schema.sql");

        sessionResultClient = mock(SessionResultClient.class);
        sessionResultRepo = new JooqSessionResultRepo(create);

        meetingService = mock(MeetingService.class);
        sessionService = mock(SessionService.class);
        driverService = mock(DriverService.class);
        cacheService = new CacheService();

        sessionResultService = new SessionResultService(
                meetingService,
                sessionService,
                sessionResultClient,
                sessionResultRepo,
                cacheService,
                driverService
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void cachesSessionResult_andAppliesClassificationOrder() {
        int meetingKey = 1000;
        int year = 2025;
        String location = "Saarbrücken";
        SessionName sessionName = SessionName.RACE;
        int sessionKey = 2000;


        Meeting meeting = new Meeting(meetingKey, "Germany", "GER", location, "Saarland GP", year, "www.url.com");
        Session session = new Session(sessionKey, meetingKey, sessionName, "RACE");

        when(meetingService.getMeetingByYearAndLocation(year, location))
                .thenReturn(meeting);

        when(sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName))
                .thenReturn(session);

        when(sessionResultClient.getSessionResultBySessionKey(sessionKey))
                .thenReturn(List.of(
                        new SessionResultDto(sessionKey, List.of("0"), 44, true, false, false, List.of(20.0), 0), //dnf
                        new SessionResultDto(sessionKey, List.of("0"), 1, false, false, false, List.of(40.0), 1), //classified
                        new SessionResultDto(sessionKey, List.of("10"), 4, false, false, false, List.of(44.0), 2), //classified
                        new SessionResultDto(sessionKey, List.of("0"), 16, false, true, false, List.of(24.0), 0)  //dns
                ));

        SessionResultWithContext first =
                sessionResultService.getResultByLocationYearAndSessionType(location, year, sessionName);

        SessionResultWithContext second =
                sessionResultService.getResultByLocationYearAndSessionType(location, year, sessionName);

        verify(sessionResultClient, times(1))
                .getSessionResultBySessionKey(sessionKey);

        assertThat(first.meetingName()).isEqualTo("Saarland GP");
        assertThat(first.sessionName()).isEqualTo(sessionName);

        List<SessionResult> results = first.results();

        assertThat(results)
                .extracting(SessionResult::driverNumber)
                .containsExactly(1,4,44,16);

        assertThat(second.results()).hasSameSizeAs(first.results());
    }
}
