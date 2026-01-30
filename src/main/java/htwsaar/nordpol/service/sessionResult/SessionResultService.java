package htwsaar.nordpol.service.sessionResult;

import htwsaar.nordpol.api.dto.SessionResultDto;
import htwsaar.nordpol.api.sessionresult.SessionResultClient;
import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.repository.sessionresult.JooqSessionResultRepo;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.util.Mapper;

import java.util.Comparator;
import java.util.List;

public class SessionResultService implements ISessionResultService {

    private IMeetingService meetingService;
    private ISessionService sessionService;

    private SessionResultClient sessionResultClient;
    private JooqSessionResultRepo sessionResultRepo;

    public SessionResultService(IMeetingService meetingService, ISessionService sessionService, SessionResultClient sessionResultClient, JooqSessionResultRepo sessionResultRepo) {
        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.sessionResultClient = sessionResultClient;
        this.sessionResultRepo = sessionResultRepo;
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
                                .comparing((SessionResult r) -> (r.dnf() || r.dns() || r.dsq()) ? 1 : 0)
                                // Then by position; treat 0 (unknown) as last
                                .thenComparingInt(r -> r.position() > 0 ? r.position() : Integer.MAX_VALUE)
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
        List<SessionResultDto> dtoFromDB = sessionResultRepo.getSessionResultBySessionKey(sessionKey);
        if(!dtoFromDB.isEmpty()) {
            return dtoFromDB.stream()
                    .map(Mapper::toSessionResult)
                    .toList();
        }

        List<SessionResultDto> dtoFromApi =
                sessionResultClient.getSessionResultBySessionKey(sessionKey);

        if(!dtoFromApi.isEmpty()) {
            sessionResultRepo.saveAll(dtoFromApi);
            return dtoFromApi.stream()
                    .map(Mapper::toSessionResult)
                    .toList();
        }

        throw new RuntimeException("not found");
    }
}
