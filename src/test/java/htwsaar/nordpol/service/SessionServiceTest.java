package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.api.session.SessionClient;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.session.ISessionRepo;
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
    void getSessionByMeetingKeyAndSessionType_returnsSessionFromDatabase() {
        SessionDto sessionDto =
                new SessionDto(1256, 9999, "Practice 1", "Practice");

        when(ISessionRepo.getSessionByMeetingKeyAndSessionType(1256, "Practice"))
                .thenReturn(Optional.of(sessionDto));

        Session result =
                sessionService.getSessionByMeetingKeyAndSessionType(1256, "Practice");

        assertThat(result.meetingKey()).isEqualTo(1256);

        verify(sessionClient, never()).getSessionByMeetingKeyAndSessionType(1256, "Practice");
        verify(ISessionRepo).getSessionByMeetingKeyAndSessionType(1256, "Practice");
    }

    @Test
    void getSessionByMeetingKeyAndSessionType_fetchesFromApiAndSavesSession(){
        when(ISessionRepo.getSessionByMeetingKeyAndSessionType(1256, "Practice"))
                .thenReturn(Optional.empty());

        SessionDto apiDto =
                new SessionDto(1256, 9999, "Practice 1", "Practice");

        when(sessionClient.getSessionByMeetingKeyAndSessionType(1256, "Practice"))
                .thenReturn(Optional.of(apiDto));

        Session result =
                sessionService.getSessionByMeetingKeyAndSessionType(1256, "Practice");

        assertThat(result.meetingKey()).isEqualTo(1256);

        verify(ISessionRepo).save(apiDto);
    }

    @Test
    void getSessionByMeetingKeyAndSessionType_throwsException_ifSessionIsNotFound() {
        when(ISessionRepo.getSessionByMeetingKeyAndSessionType(1256, "Practice"))
                .thenReturn(Optional.empty());

        when(sessionClient.getSessionByMeetingKeyAndSessionType(1256, "Practice"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sessionService.getSessionByMeetingKeyAndSessionType(1256, "Practice"))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("Session not found");
    }


}
