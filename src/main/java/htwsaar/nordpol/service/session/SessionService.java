package htwsaar.nordpol.service.session;

import htwsaar.nordpol.api.session.ISessionClient;
import htwsaar.nordpol.dto.SessionDto;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.util.Mapper;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
public class SessionService implements ISessionService {

    private final ISessionRepo sessionRepo;
    private final ISessionClient sessionClient;
    private final ICacheService cacheService;

    public SessionService(ISessionRepo sessionRepo, ISessionClient sessionClient, ICacheService cacheService) {
        requireNonNull(sessionRepo, "sessionRepo must not be null.");
        requireNonNull(sessionClient, "sessionClient must not be null.");
        requireNonNull(cacheService, "cacheService must not be null");

        this.sessionRepo = sessionRepo;
        this.sessionClient = sessionClient;
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
}
