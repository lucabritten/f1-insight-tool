package htwsaar.nordpol.api;

import htwsaar.nordpol.api.dto.SessionDto;

import java.util.Optional;

public interface ISessionClient {

    Optional<SessionDto> getSessionByMeetingKeyAndSessionType(int meetingKey, String sessionType);
}
