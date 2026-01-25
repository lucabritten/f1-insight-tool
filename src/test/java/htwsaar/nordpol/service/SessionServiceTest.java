package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.api.session.SessionClient;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.service.session.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    ISessionRepo ISessionRepo;

    @Mock
    SessionClient sessionClient;

    @InjectMocks
    SessionService sessionService;

    @Test
    void getSessionByMeetingKeyAndSessionName_returnsSessionFromDatabase() {
        SessionDto sessionDto =
                new SessionDto(1256, 9999, "Practice 1", "Practice 1");

        when(ISessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Practice 1"))
                .thenReturn(Optional.of(sessionDto));

        Session result =
                sessionService.getSessionByMeetingKeyAndSessionName(1256, SessionName.PRACTICE1);

        assertThat(result.meetingKey()).isEqualTo(1256);

        verify(sessionClient, never()).getSessionByMeetingKeyAndsessionName(1256, "Practice%201");
        verify(ISessionRepo).getSessionByMeetingKeyAndSessionName(1256, "Practice 1");
    }

    @Test
    void getSessionByMeetingKeyAndSessionName_fetchesFromApiAndSavesSession(){
        when(ISessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Practice 1"))
                .thenReturn(Optional.empty());

        SessionDto apiDto =
                new SessionDto(1256, 9999, "Practice 1", "Practice 1");

        when(sessionClient.getSessionByMeetingKeyAndsessionName(1256, "Practice%201"))
                .thenReturn(Optional.of(apiDto));

        Session result =
                sessionService.getSessionByMeetingKeyAndSessionName(1256, SessionName.PRACTICE1);

        assertThat(result.meetingKey()).isEqualTo(1256);

        verify(ISessionRepo).save(apiDto);
    }

    @Test
    void getSessionByMeetingKeyAndSessionName_throwsException_ifSessionIsNotFound() {
        when(ISessionRepo.getSessionByMeetingKeyAndSessionName(1256, "Practice 1"))
                .thenReturn(Optional.empty());

        when(sessionClient.getSessionByMeetingKeyAndsessionName(1256, "Practice%201"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sessionService.getSessionByMeetingKeyAndSessionName(1256, SessionName.PRACTICE1))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("Session not found");
    }


}
