package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.SessionResultDto;
import htwsaar.nordpol.api.sessionresult.SessionResultClient;
import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.repository.sessionresult.JooqSessionResultRepo;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionResultServiceTest {

    @Mock
    private IMeetingService meetingService;

    @Mock
    private ISessionService sessionService;

    @Mock
    private SessionResultClient sessionResultClient;

    @Mock
    private JooqSessionResultRepo sessionResultRepo;

    @InjectMocks
    private SessionResultService sessionResultService;

    @Test
    void getResultByLocationYearAndSessionType_returnsResultsFromDatabase() {
        // given
        Meeting meeting = new Meeting(1250, "USA", "USA", "Austin", "United States Grand Prix", 2025);
        Session session = new Session(9640, 1250,SessionName.RACE, "RACE");

        SessionResultDto r1 = new SessionResultDto(
                9640,
                List.of("0.0"),
                1,
                false,
                false,
                false,
                List.of(92.5),
                1
        );

        SessionResultDto r2 = new SessionResultDto(
                9640,
                List.of("0.3"),
                4,
                false,
                false,
                false,
                List.of(92.8),
                2
        );

        when(meetingService.getMeetingByYearAndLocation(2025, "Austin"))
                .thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(1250, SessionName.RACE))
                .thenReturn(session);
        when(sessionResultRepo.getSessionResultBySessionKey(9640))
                .thenReturn(List.of(r1, r2));

        // when
        SessionResultWithContext result =
                sessionResultService.getResultByLocationYearAndSessionType(
                        "Austin", 2025, SessionName.RACE
                );

        // then
        assertThat(result.meetingName()).isEqualTo("United States Grand Prix");
        assertThat(result.sessionName()).isEqualTo(SessionName.RACE);
        assertThat(result.results()).hasSize(2);

        verify(sessionResultClient, never()).getSessionResultBySessionKey(anyInt());
        verify(sessionResultRepo, never()).saveAll(any());
    }

    @Test
    void getResultByLocationYearAndSessionType_fetchesFromApiAndStoresWhenDbIsEmpty() {
        // given
        Meeting meeting = new Meeting(1250, "United States Grand Prix", "USA", "Austin", "Austin GP", 2025);
        Session session = new Session(9640, 1250,SessionName.QUALIFYING, "RACE");

        SessionResultDto apiResult = new SessionResultDto(
                9640,
                List.of("0.0"),
                1,
                false,
                false,
                false,
                List.of(93.1, 92.7, 92.5),
                1
        );

        when(meetingService.getMeetingByYearAndLocation(2025, "Austin"))
                .thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(1250, SessionName.QUALIFYING))
                .thenReturn(session);
        when(sessionResultRepo.getSessionResultBySessionKey(9640))
                .thenReturn(List.of());
        when(sessionResultClient.getSessionResultBySessionKey(9640))
                .thenReturn(List.of(apiResult));

        // when
        SessionResultWithContext result =
                sessionResultService.getResultByLocationYearAndSessionType(
                        "Austin", 2025, SessionName.QUALIFYING
                );

        // then
        assertThat(result.results()).hasSize(1);
        verify(sessionResultRepo).saveAll(List.of(apiResult));
    }

    @Test
    void getResultByLocationYearAndSessionType_sortsDnfsToBottom() {
        // given
        Meeting meeting = new Meeting(1250, "United States Grand Prix", "USA", "Austin", "Austin GP", 2025);
        Session session = new Session(9640, 1250,SessionName.RACE, "RACE");

        SessionResultDto finished = new SessionResultDto(
                9640,
                List.of("0.0"),
                1,
                false,
                false,
                false,
                List.of(92.1),
                1
        );

        SessionResultDto dnf = new SessionResultDto(
                9640,
                List.of("+1 LAP"),
                44,
                true,
                false,
                false,
                List.of(94.2),
                0
        );

        when(meetingService.getMeetingByYearAndLocation(2025, "Austin"))
                .thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(1250, SessionName.RACE))
                .thenReturn(session);
        when(sessionResultRepo.getSessionResultBySessionKey(9640))
                .thenReturn(List.of(dnf, finished));

        // when
        SessionResultWithContext result =
                sessionResultService.getResultByLocationYearAndSessionType(
                        "Austin", 2025, SessionName.RACE
                );

        // then
        assertThat(result.results().get(0).dnf()).isFalse();
        assertThat(result.results().get(1).dnf()).isTrue();
    }
}
