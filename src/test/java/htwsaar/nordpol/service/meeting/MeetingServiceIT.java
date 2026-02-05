package htwsaar.nordpol.service.meeting;

import htwsaar.nordpol.dto.MeetingDto;
import htwsaar.nordpol.api.meeting.IMeetingClient;
import htwsaar.nordpol.api.meeting.MeetingClient;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.repository.meeting.JooqMeetingRepo;
import htwsaar.nordpol.service.CacheService;
import htwsaar.nordpol.testutil.SqlSchemaLoader;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class MeetingServiceIT {

    private Connection connection;
    private DSLContext create;
    private IMeetingService meetingService;
    private IMeetingClient meetingClient;
    private IMeetingRepo meetingRepo;

    @BeforeEach
    void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);

        SqlSchemaLoader.loadSchema(create, "schema.sql");

        meetingClient = mock(MeetingClient.class);
        meetingRepo = new JooqMeetingRepo(create);

        meetingService = new MeetingService(
                meetingRepo,
                meetingClient,
                new CacheService()
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void apiCalledOnlyOnce_whenMeetingIsCachedInDb() {
        MeetingDto dto = new MeetingDto(
                "JPN", "Japan", "Suzuka", 1234, "Japanese GP", 2025, "www.url-to-flag.jp"
        );
        when(meetingClient.getMeetingByYearAndLocation(2025, "Suzuka"))
                .thenReturn(Optional.of(dto));

        meetingService.getMeetingByYearAndLocation(2025, "Suzuka");
        meetingService.getMeetingByYearAndLocation(2025, "Suzuka");

        verify(meetingClient, times(1))
                .getMeetingByYearAndLocation(2025, "Suzuka");
    }

    @Test
    void apiIsNotCalled_whenMeetingAlreadyExistsInDb() {
        MeetingDto dto = new MeetingDto(
                "JPN", "Japan", "Suzuka", 1234, "Japanese GP", 2025, "www.url-to-flag.jp"
        );
        meetingRepo.save(dto);

        meetingService.getMeetingByYearAndLocation(2025, "Suzuka");

        verifyNoInteractions(meetingClient);
    }

    @Test
    void doesNotPersist_whenApiThrowsException() {
        when(meetingClient.getMeetingByYearAndLocation(2025, "Suzuka"))
                .thenThrow(new RuntimeException("API down"));

        assertThrows(RuntimeException.class, () ->
                meetingService.getMeetingByYearAndLocation(2025, "Suzuka")
        );

        var count = create.fetchCount(DSL.table("Meetings"));
        assertThat(count).isZero();
    }
}

