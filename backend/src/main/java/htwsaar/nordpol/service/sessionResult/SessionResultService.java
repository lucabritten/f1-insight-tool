package htwsaar.nordpol.service.sessionResult;

import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.dto.SessionResultDto;
import htwsaar.nordpol.api.sessionresult.ISessionResultClient;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.exception.SessionResultNotFoundException;
import htwsaar.nordpol.repository.sessionresult.ISessionResultRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.driver.IDriverService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.util.Mapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Application service for session result data.
 *
 * <p>This service resolves and orders session results according to
 * Formula 1 classification rules.</p>
 */
@Service
public class SessionResultService implements ISessionResultService {

    private final IMeetingService meetingService;
    private final ISessionService sessionService;

    private final ISessionResultClient sessionResultClient;
    private final ISessionResultRepo sessionResultRepo;

    private final ICacheService cacheService;
    private final IDriverService driverService;

    public SessionResultService(IMeetingService meetingService, ISessionService sessionService, ISessionResultClient sessionResultClient, ISessionResultRepo sessionResultRepo, ICacheService cacheService, IDriverService driverService) {
        requireNonNull(meetingService, "meetingService must not be null");
        requireNonNull(sessionService, "sessionService must not be null.");
        requireNonNull(sessionResultClient, "sessionResultClient must not be null.");
        requireNonNull(sessionResultRepo, "sessionResultRepo must not be null");
        requireNonNull(cacheService, "cacheService must not be null");

        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.sessionResultClient = sessionResultClient;
        this.sessionResultRepo = sessionResultRepo;
        this.driverService = driverService;
        this.cacheService = cacheService;
    }

    @Override
    public SessionResultWithContext getResultByLocationYearAndSessionType(String location, int year, SessionName sessionName){
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        List<SessionResult> results = getResultsBySessionKey(sessionKey)
                .stream()
                .sorted(CLASSIFICATION_ORDER())
                .toList();
        return new SessionResultWithContext(meeting.meetingName(),
                session.sessionName(),
                results
        );
    }

    /**
     * Defines the classification order for session results.
     *
     * <p>Drivers who did not finish, start, or were disqualified
     * are ranked after classified drivers.</p>
     */
    private Comparator<SessionResult> CLASSIFICATION_ORDER() {
        return Comparator.comparingInt(this::classificationRank)
                .thenComparing(result -> result.position() > 0 ? result.position() : Integer.MAX_VALUE)
                .thenComparing(SessionResult::driverNumber);
    }

    private int classificationRank(SessionResult result){
        if(result.dns()) return 3;
        if (result.dsq()) return 2;
        if (result.dnf()) return 1;
        return 0;
    }

    private List<SessionResult> getResultsBySessionKey(int sessionKey) {

        List<SessionResultDto> dtoList = cacheService.getOrFetchList(
                () -> sessionResultRepo.getSessionResultBySessionKey(sessionKey),
                () -> sessionResultClient.getSessionResultBySessionKey(sessionKey),
                sessionResultRepo::saveAll,
                () -> new SessionResultNotFoundException(sessionKey)
        );

        List<Driver> drivers = driverService.getDriversBySessionKey(sessionKey);

        return dtoList
                .stream()
                .map(dto -> {
                    Driver driver = drivers.stream().filter(d -> d.driverNumber() == dto.driver_number()).toList().getFirst();
                    String name = driver.firstName() + " " + driver.lastName();
                    return  Mapper.toSessionResult(dto, name);
                })
                .toList();
    }
}
