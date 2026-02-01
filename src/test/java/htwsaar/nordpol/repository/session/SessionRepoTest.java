package htwsaar.nordpol.repository.session;

import htwsaar.nordpol.api.dto.SessionDto;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SessionRepoTest {

    private Connection connection;
    private DSLContext create;
    private ISessionRepo sessionRepo;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);
        create.execute("""
            create table Meetings (
                meeting_key integer primary key not null,
                country_name text not null,
                country_code text not null,
                location text not null,
                meeting_name text not null,
                year integer not null
            );
        """);
        create.execute("""
            create table Sessions (
                session_key integer primary key not null,
                meeting_key integer not null,
                session_name text not null,
                session_type text not null,
                foreign key (meeting_key) references Meetings(meeting_key),
                unique (meeting_key, session_name)
            );
        """);
        create.execute("""
            insert into Meetings (meeting_key, country_name, country_code, location, meeting_name, year)
            values (1256, 'Japan', 'JPN', 'Suzuka', 'Japanese Grand Prix', 2025);
        """);

        sessionRepo = new JooqSessionRepo(create);
    }

    @AfterEach
    void tearDown() throws Exception {
        if(connection != null) {
            connection.close();
        }
    }

    @Test
    void saveSession_persistsSession() {
        SessionDto sessionData =
                new SessionDto(1256, 9999, "Practice 1", "Practice");

        sessionRepo.save(sessionData);

        Optional<SessionDto> stored =
                sessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Practice 1");

        assertThat(stored).isPresent();

        SessionDto dto = stored.get();

        assertThat(dto.meeting_key()).isEqualTo(1256);
        assertThat(dto.session_key()).isEqualTo(9999);
        assertThat(dto.session_name()).isEqualTo("Practice 1");
        assertThat(dto.session_type()).isEqualTo("Practice");
    }

    @Test
    void getSessionByMeetingKeyAndsessionName_returnsEmptyWhenMissing() {
        Optional<SessionDto> stored =
                sessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Race");

        assertThat(stored).isEmpty();
    }

    @Test
    void getSessionByMeetingKeyAndsessionName_returnsCorrectSession_forMultipleEntries() {
        SessionDto practice =
                new SessionDto(1256, 9999, "Practice 1", "Practice");
        SessionDto race =
                new SessionDto(1256, 10006, "Race", "Race");

        sessionRepo.save(practice);
        sessionRepo.save(race);

        Optional<SessionDto> stored =
                sessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Race");

        assertThat(stored).isPresent();

        SessionDto dto = stored.get();
        assertThat(dto.session_key()).isEqualTo(10006);
        assertThat(dto.session_name()).isEqualTo("Race");
        assertThat(dto.session_type()).isEqualTo("Race");
    }

    @Test
    void saveSession_throwsException_whenMeetingKeyIsNegative() {
        SessionDto sessionDto =
                new SessionDto(-1, 9999, "Practice 1", "Practice");

        assertThatThrownBy(() -> sessionRepo.save(sessionDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveSession_throwsException_whenSessionKeyIsNegative() {
        SessionDto sessionDto =
                new SessionDto(1256, -1, "Practice 1", "Practice");

        assertThatThrownBy(() -> sessionRepo.save(sessionDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveSession_throwsException_whenSessionNameIsNull() {
        SessionDto sessionDto =
                new SessionDto(1256, 9999, null, "Practice");

        assertThatThrownBy(() -> sessionRepo.save(sessionDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveSession_throwsException_whensessionNameIsNull() {
        SessionDto sessionDto =
                new SessionDto(1256, 9999, "Practice 1", null);

        assertThatThrownBy(() -> sessionRepo.save(sessionDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
