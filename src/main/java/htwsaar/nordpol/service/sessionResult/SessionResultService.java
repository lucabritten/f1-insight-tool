package htwsaar.nordpol.service.sessionResult;

import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.api.dto.SessionResultDto;
import htwsaar.nordpol.api.sessionresult.ISessionResultClient;
import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.exception.SessionResultNotFoundException;
import htwsaar.nordpol.repository.sessionresult.ISessionResultRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.util.Mapper;

import java.util.Comparator;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class SessionResultService implements ISessionResultService {

    private IMeetingService meetingService;
    private ISessionService sessionService;

    private ISessionResultClient sessionResultClient;
    private ISessionResultRepo sessionResultRepo;

    private ICacheService cacheService;

    public SessionResultService(IMeetingService meetingService, ISessionService sessionService, ISessionResultClient sessionResultClient, ISessionResultRepo sessionResultRepo, ICacheService cacheService) {
        requireNonNull(meetingService, "meetingService must not be null");
        requireNonNull(sessionService, "sessionService must not be null.");
        requireNonNull(sessionResultClient, "sessionResultClient must not be null.");
        requireNonNull(sessionResultRepo, "sessionResultRepo must not be null");
        requireNonNull(cacheService, "cacheService must not be null");
        requireNonNull(cacheService, "cacheService must not be null");

        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.sessionResultClient = sessionResultClient;
        this.sessionResultRepo = sessionResultRepo;
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
                .sorted(
                        Comparator
                                // Push DSQ/DNS/DNF to the bottom
                                .comparing((SessionResult r) -> r.position() > 0 ? r.position() : Integer.MAX_VALUE)
                                .thenComparingInt(r -> (r.dnf() || r.dns() || r.dsq()) ? 1 : 0)
                                // Then by position; treat 0 (unknown) as last

                                // Stable final tiebreaker: driver number
                                .thenComparingInt(SessionResult::driverNumber)
                )
                .toList();
        return new SessionResultWithContext(meeting.meetingName(),
                session.sessionName(),
                results
        );
    }

    private List<SessionResult> getResultsBySessionKey(int sessionKey) {

        List<SessionResultDto> dtoList = cacheService.getOrFetchList(
                () -> sessionResultRepo.getSessionResultBySessionKey(sessionKey),
                () -> sessionResultClient.getSessionResultBySessionKey(sessionKey),
                sessionResultRepo::saveAll,
                () -> new SessionResultNotFoundException(sessionKey)
        );
        return dtoList
                .stream()
                .map(Mapper::toSessionResult)
                .toList();
    }
}
