package htwsaar.nordpol.service.session;

import htwsaar.nordpol.api.session.ISessionClient;
import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.util.Mapper;

import static java.util.Objects.requireNonNull;

public class SessionService implements ISessionService {

    private final ISessionRepo sessionRepo;
    private final ISessionClient sessionClient;

    private final IMeetingService meetingService;
    private final ICacheService cacheService;

    public SessionService(ISessionRepo sessionRepo, ISessionClient sessionClient, IMeetingService meetingService, ICacheService cacheService) {
        requireNonNull(sessionRepo, "sessionRepo must not be null.");
        requireNonNull(sessionClient, "sessionClient must not be null.");
        requireNonNull(meetingService, "meetingService must not be null");
        requireNonNull(cacheService, "cacheService must not be null");

        this.sessionRepo = sessionRepo;
        this.sessionClient = sessionClient;
        this.meetingService = meetingService;
        this.cacheService = cacheService;
    }

    @Override
    public Session getSessionByMeetingKeyAndSessionName(int meetingKey, SessionName sessionName){
        SessionDto dto = cacheService.getOrFetchOptional(
                ()-> sessionRepo.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName.dbValue()),
                () -> sessionClient.getSessionByMeetingKeyAndsessionName(meetingKey, sessionName.dbValue()),
                sessionRepo::save,
                () -> new SessionNotFoundException(meetingKey, sessionName.displayName())
        );
        return Mapper.toSession(dto);
    }

    @Override
    public Session getSessionByLocationYearAndSessionType(String location, int year, SessionName sessionName) {
        int meetingKey = meetingService.getMeetingByYearAndLocation(year, location).meetingKey();
        return getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
    }
}
