package htwsaar.nordpol.repository.meeting;

import htwsaar.nordpol.api.dto.MeetingDto;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeetingRepoTest {

    private Connection connection;
    private DSLContext create;
    private IMeetingRepo meetingRepo;


    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);
        create.execute("""
                create table Meetings (
                    meeting_key integer primary key,
                    country_name text not null,
                    country_code text not null,
                    location text not null,
                    meeting_name text not null,
                    year int
                );
                """);

                meetingRepo = new JooqMeetingRepo(create);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void saveMeeting_persistsMeeting() {
        MeetingDto meetingData =
                new MeetingDto(
                        "USA", "United States", "Austin", 1247, "United States Grand Prix", 2024);

        meetingRepo.save(meetingData);

        Optional<MeetingDto> stored =
                meetingRepo.getMeetingByYearAndLocation(2024, "Austin");

        assertThat(stored).isPresent();

        MeetingDto dto = stored.get();

        assertThat(dto.country_code()).isEqualTo("USA");
        assertThat(dto.country_name()).isEqualTo("United States");
        assertThat(dto.location()).isEqualTo("Austin");
        assertThat(dto.meeting_key()).isEqualTo(1247);
        assertThat(dto.meeting_name()).isEqualTo("United States Grand Prix");
        assertThat(dto.year()).isEqualTo(2024);
    }

    @Test
    void saveMeeting_throwsException_whenLocationIsNull(){
        MeetingDto meetingDto = new MeetingDto("USA", "United States", null, 1247, "United States Grand Prix", 2024);

        assertThatThrownBy(() -> meetingRepo.save(meetingDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveMeeting_throwsException_whenCountryCodeIsNull(){
        MeetingDto meetingDto = new MeetingDto(null, "United States", "Austin", 1247, "United States Grand Prix", 2024);

        assertThatThrownBy(() -> meetingRepo.save(meetingDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveMeeting_throwsException_whenCountryNameIsNull(){
        MeetingDto meetingDto = new MeetingDto("USA", null, "Austin", 1247, "United States Grand Prix", 2024);

        assertThatThrownBy(() -> meetingRepo.save(meetingDto))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void saveMeeting_throwsException_whenMeetingKeyIsNegative(){
        MeetingDto meetingDto = new MeetingDto("USA", "United States", "Austin", -1, "United States Grand Prix", 2024);

        assertThatThrownBy(() -> meetingRepo.save(meetingDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveMeeting_throwsException_whenMeetingNameIsNull(){
        MeetingDto meetingDto = new MeetingDto("USA", "United States", "Austin", 1247, null, 2024);

        assertThatThrownBy(() -> meetingRepo.save(meetingDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveMeeting_throwsException_whenYearIsNegative(){
        MeetingDto meetingDto = new MeetingDto("USA", "United States", "Austin", 1247, "United States Grand Prix", -1);

        assertThatThrownBy(() -> meetingRepo.save(meetingDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
