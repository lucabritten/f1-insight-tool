package htwsaar.nordpol.repository;

import htwsaar.nordpol.api.dto.SessionDto;

import java.util.Optional;

public interface ISessionRepo {

    void save(SessionDto dto);
    Optional<SessionDto> getSessionByMeetingKeyAndSessionType(int meetingKey, String sessionKey);

}
