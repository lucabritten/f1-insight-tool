package htwsaar.nordpol.api;

public interface ISessionClient {

    Optional<SessionDto> getSessionByMeetingKeyAndSessionType(int meetingKey, String sessionType);
}
