package htwsaar.nordpol.repository.meeting;

import htwsaar.nordpol.api.dto.MeetingDto;
import org.jooq.DSLContext;

import java.util.Optional;

import static com.nordpol.jooq.tables.Meetings.MEETINGS;


public class JooqMeetingRepo implements IMeetingRepo {

    private final DSLContext create;

    public JooqMeetingRepo(DSLContext create){
        this.create = create;
    }

    @Override
    public void save(MeetingDto dto) {
        validateMeeting(dto);

        create
                .insertInto(MEETINGS,
                        MEETINGS.MEETING_KEY,
                        MEETINGS.COUNTRY_NAME,
                        MEETINGS.COUNTRY_CODE,
                        MEETINGS.LOCATION,
                        MEETINGS.MEETING_NAME,
                        MEETINGS.YEAR)
                .values(dto.meeting_key(),
                        dto.country_name(),
                        dto.country_code(),
                        dto.location(),
                        dto.meeting_name(),
                        dto.year())
                .execute();

    }

    private void validateMeeting(MeetingDto dto){
        if(dto == null)
            throw new IllegalArgumentException("MeetingDto must not be null.");
        if (dto.location() == null || dto.location().isBlank())
            throw new IllegalArgumentException("Field location must not be null or blank.");
        if(dto.country_code() == null || dto.country_code().isBlank())
            throw new IllegalArgumentException("Field country_code must not be null or blank.");
        if(dto.country_name() == null || dto.country_name().isBlank())
            throw new IllegalArgumentException("Field country_name must not be null or blank.");
        if(dto.meeting_key() < 0)
            throw new IllegalArgumentException("Field meeting_key must be greater than zero.");
        if(dto.meeting_name() == null || dto.meeting_name().isBlank())
            throw new IllegalArgumentException("Field meeting_name must not be null or blank.");
        if(dto.year() < 0){
            throw new IllegalArgumentException("Field year must be positive.");
        }
    }

    @Override
    public Optional<MeetingDto> getMeetingBySeasonAndLocation(int season, String location) {
        var record = create.select(MEETINGS.COUNTRY_CODE, MEETINGS.COUNTRY_NAME, MEETINGS.LOCATION, MEETINGS.MEETING_KEY, MEETINGS.MEETING_NAME, MEETINGS.YEAR)
                .from(MEETINGS)
                .where(MEETINGS.YEAR.eq(season)
                        .and(MEETINGS.LOCATION.eq(location)))
                .fetchOneInto(MeetingDto.class);

        return Optional.ofNullable(record);
    }
}
