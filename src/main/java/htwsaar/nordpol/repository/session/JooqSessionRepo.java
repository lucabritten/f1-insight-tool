package htwsaar.nordpol.repository.session;

import com.nordpol.jooq.tables.Sessions;
import htwsaar.nordpol.api.dto.SessionDto;
import org.jooq.DSLContext;

import static com.nordpol.jooq.tables.Sessions.*;

import java.util.Optional;

public class JooqSessionRepo implements ISessionRepo{

    private final DSLContext create;

    public JooqSessionRepo(DSLContext create){
        this.create = create;
    }

    @Override
    public void save(SessionDto dto) {
        validateSession(dto);

        create
                .insertInto(SESSIONS,
                SESSIONS.SESSION_KEY,
                SESSIONS.MEETING_KEY,
                SESSIONS.SESSION_NAME,
                SESSIONS.SESSION_TYPE)
                .values(dto.session_key(),
                        dto.meeting_key(),
                        dto.session_name(),
                        dto.session_type())
                .execute();
    }

    private void validateSession(SessionDto dto){
        if(dto == null)
            throw new IllegalArgumentException("Session dto must not be null.");
        if(dto.meeting_key() < 0)
            throw new IllegalArgumentException("meeting_key must be positive.");
        if(dto.session_key() < 0)
            throw new IllegalArgumentException("session_key must be positive.");
        if(dto.session_name() == null || dto.session_name().isBlank())
            throw new IllegalArgumentException("session_name must not be null or blank.");
        if(dto.session_type() == null || dto.session_type().isBlank())
            throw new IllegalArgumentException("session_type must not be null or blank.");
    }

    @Override
    public Optional<SessionDto> getSessionByMeetingKeyAndSessionType(int meetingKey, String sessionType) {
        var record = create.select(SESSIONS.MEETING_KEY, SESSIONS.SESSION_KEY, SESSIONS.SESSION_NAME, SESSIONS.SESSION_TYPE)
                .from(SESSIONS)
                .where(SESSIONS.MEETING_KEY.eq(meetingKey)
                        .and(SESSIONS.SESSION_TYPE.eq(sessionType)))
                .fetchOneInto(SessionDto.class);

        return Optional.ofNullable(record);
    }
}
