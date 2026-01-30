package htwsaar.nordpol.service.session;

import htwsaar.nordpol.api.session.ISessionClient;
import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.util.Mapper;

public class SessionService implements ISessionService {

    private final ISessionRepo sessionRepo;
    private final ISessionClient sessionClient;
    private final ICacheService cacheService;

    public SessionService(ISessionRepo sessionRepo, ISessionClient sessionClient, ICacheService cacheService) {
        if (sessionRepo == null) {
            throw new IllegalArgumentException("sessionRepo must not be null.");
        }
        if (sessionClient == null) {
            throw new IllegalArgumentException("sessionClient must not be null.");
        }
        if(cacheService == null) {
            throw new IllegalArgumentException("cacheServive must not be null");
        }
        this.sessionRepo = sessionRepo;
        this.sessionClient = sessionClient;
        this.cacheService = cacheService;
    }

    public Session getSessionByMeetingKeyAndSessionName(int meetingKey, SessionName sessionName){
        SessionDto dto = cacheService.getOrFetchOptional(
                ()-> sessionRepo.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName.dbValue()),
                () -> sessionClient.getSessionByMeetingKeyAndsessionName(meetingKey, sessionName.dbValue()),
                sessionRepo::save,
                () -> new SessionNotFoundException(meetingKey, sessionName.displayName())
        );
        return Mapper.toSession(dto);
    }
}
