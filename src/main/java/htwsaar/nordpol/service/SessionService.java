package htwsaar.nordpol.service;

import htwsaar.nordpol.api.session.ISessionClient;
import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.util.Mapper;

import java.util.Optional;

public class SessionService {

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

        public Session getSessionByMeetingKeyAndSessionType (int meetingKey, String sessionType){
            Optional<SessionDto> dtoFromDB = sessionRepo.getSessionByMeetingKeyAndSessionType(meetingKey, sessionType);
            if (dtoFromDB.isPresent()) {
                return Mapper.toSession(dtoFromDB.get());
            }

            Optional<SessionDto> dtoFromApi =
                    sessionClient.getSessionByMeetingKeyAndSessionType(meetingKey, sessionType);

            if (dtoFromApi.isPresent()) {
                SessionDto sessionDto = dtoFromApi.get();
                sessionRepo.save(sessionDto);
                return Mapper.toSession(sessionDto);
            }
            throw new SessionNotFoundException(meetingKey, sessionType);
        }
}
