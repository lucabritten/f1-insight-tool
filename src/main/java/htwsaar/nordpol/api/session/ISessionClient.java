package htwsaar.nordpol.api.session;

import htwsaar.nordpol.dto.SessionDto;

import java.util.Optional;

public interface ISessionClient {

    Optional<SessionDto> getSessionByMeetingKeyAndsessionName(int meetingKey, String sessionName);
}
