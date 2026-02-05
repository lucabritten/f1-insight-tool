package htwsaar.nordpol.repository.session;

import htwsaar.nordpol.dto.SessionDto;

import java.util.Optional;

public interface ISessionRepo {

    void save(SessionDto dto);
    Optional<SessionDto> getSessionByMeetingKeyAndSessionName(int meetingKey, String sessionName);

}
