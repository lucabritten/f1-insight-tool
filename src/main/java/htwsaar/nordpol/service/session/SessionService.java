package htwsaar.nordpol.service.session;

import htwsaar.nordpol.api.session.ISessionClient;
import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.util.Mapper;

import java.util.Optional;

public class SessionService implements ISessionService {

        private final ISessionRepo sessionRepo;
        private final ISessionClient sessionClient;

        public SessionService(ISessionRepo sessionRepo, ISessionClient sessionClient) {
            if (sessionRepo == null) {
                throw new IllegalArgumentException("sessionRepo must not be null.");
            }
            if (sessionClient == null) {
                throw new IllegalArgumentException("sessionClient must not be null.");
            }
            this.sessionRepo = sessionRepo;
            this.sessionClient = sessionClient;
        }

        public Session getSessionByMeetingKeyAndSessionName(int meetingKey, SessionName sessionName){
            Optional<SessionDto> dtoFromDB = sessionRepo.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName.dbValue());
            if (dtoFromDB.isPresent()) {
                return Mapper.toSession(dtoFromDB.get());
            }

            Optional<SessionDto> dtoFromApi =
                    sessionClient.getSessionByMeetingKeyAndsessionName(meetingKey, sessionName.apiValue());

            if (dtoFromApi.isPresent()) {
                SessionDto sessionDto = dtoFromApi.get();
                sessionRepo.save(sessionDto);
                return Mapper.toSession(sessionDto);
            }
            throw new SessionNotFoundException(meetingKey, sessionName.toString());
        }
}
