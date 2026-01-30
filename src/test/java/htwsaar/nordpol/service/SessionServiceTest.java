package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.api.session.SessionClient;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.service.session.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    ISessionRepo sessionRepo;

    @Mock
    SessionClient sessionClient;

    ICacheService cacheService = new CacheService();

    SessionService sessionService;

    @BeforeEach
    void setup() {
        sessionService = new SessionService(sessionRepo, sessionClient, cacheService);
    }

    @Test
    void getSessionByMeetingKeyAndSessionName_returnsSessionFromDatabase() {
        SessionDto sessionDto =
                new SessionDto(1256, 9999, "Practice 1", "Practice 1");

        when(sessionRepo.getSessionByMeetingKeyAndSessionName(1256, SessionName.PRACTICE1.dbValue()))
                .thenReturn(Optional.of(sessionDto));

        Session result =
                sessionService.getSessionByMeetingKeyAndSessionName(1256, SessionName.PRACTICE1);

        assertThat(result.meetingKey()).isEqualTo(1256);

        verify(sessionClient, never()).getSessionByMeetingKeyAndsessionName(1256, "Practice 1");
        verify(sessionRepo).getSessionByMeetingKeyAndSessionName(1256, "Practice 1");
    }

    @Test
    void getSessionByMeetingKeyAndSessionName_fetchesFromApiAndSavesSession(){
        when(sessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Practice 1"))
                .thenReturn(Optional.empty());

        SessionDto apiDto =
                new SessionDto(1256, 9999, "Practice 1", "Practice 1");

        when(sessionClient.getSessionByMeetingKeyAndsessionName(1256, "Practice 1"))
                .thenReturn(Optional.of(apiDto));

        Session result =
                sessionService.getSessionByMeetingKeyAndSessionName(1256, SessionName.PRACTICE1);

        assertThat(result.meetingKey()).isEqualTo(1256);

        verify(sessionRepo, times(1)).save(apiDto);
    }

    @Test
    void getSessionByMeetingKeyAndSessionName_doesNotSave_whenFoundInDatabase() {
        SessionDto sessionDto = new SessionDto(1234, 4321, "Practice 1", "Practice");

        when(sessionRepo.getSessionByMeetingKeyAndSessionName(1234,"Practice 1"))
                .thenReturn(Optional.of(sessionDto));

        sessionService.getSessionByMeetingKeyAndSessionName(1234,SessionName.PRACTICE1);

        verify(sessionRepo, never()).save(any());
    }

    @Test
    void getSessionByMeetingKeyAndSessionName_throwsException_ifSessionIsNotFound() {
        when(sessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Practice 1"))
                .thenReturn(Optional.empty());

        when(sessionClient.getSessionByMeetingKeyAndsessionName(1256, "Practice 1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sessionService.getSessionByMeetingKeyAndSessionName(1256, SessionName.PRACTICE1))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("Session not found");
    }

    @Test
    void databaseIsQueriedBeforeApi() {
        when(sessionRepo.getSessionByMeetingKeyAndSessionName(anyInt(), anyString()))
                .thenReturn(Optional.empty());
        when(sessionClient.getSessionByMeetingKeyAndsessionName(anyInt(), anyString()))
                .thenReturn(Optional.of(new SessionDto(1234,4321,"Race", "Race")));

        sessionService.getSessionByMeetingKeyAndSessionName(1234, SessionName.RACE);

        InOrder inOrder = inOrder(sessionRepo, sessionClient);
        inOrder.verify(sessionRepo).getSessionByMeetingKeyAndSessionName(anyInt(), anyString());
        inOrder.verify(sessionClient).getSessionByMeetingKeyAndsessionName(anyInt(), anyString());
    }
}
