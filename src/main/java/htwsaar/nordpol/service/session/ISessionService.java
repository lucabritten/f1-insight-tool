package htwsaar.nordpol.service.session;

import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;

public interface ISessionService {
    Session getSessionByMeetingKeyAndSessionName(int meetingKey, SessionName sessionName);
    Session getSessionByLocationYearAndSessionType(String location, int year, SessionName sessionName);
}
