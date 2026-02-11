package htwsaar.nordpol.service.sessionresult;

import htwsaar.nordpol.dto.SessionResultDto;
import htwsaar.nordpol.api.sessionresult.ISessionResultClient;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.repository.sessionresult.ISessionResultRepo;
import htwsaar.nordpol.service.CacheService;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ISessionResultClient sessionResultClient;

    @Mock
    private ISessionResultRepo sessionResultRepo;

    private SessionResultService sessionResultService;

    ICacheService cacheService = new CacheService();

    @BeforeEach
    void setup() {
        sessionResultService = new SessionResultService(meetingService, sessionService, sessionResultClient, sessionResultRepo, cacheService);
    }

    @Nested
    @DisplayName("getResultByLocationYearAndSessionType")
    class GetResultByLocationYearAndSessionType {

        @Test
        void returnsResultsFromDatabase() {
            Meeting meeting = new Meeting(1250, "USA", "USA", "Austin", "United States Grand Prix", 2025, "https://www.url_to_flag.com");
            Session session = new Session(9640, 1250, SessionName.RACE, "RACE");

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

            SessionResultWithContext result =
                    sessionResultService.getResultByLocationYearAndSessionType(
                            "Austin", 2025, SessionName.RACE
                    );

            assertThat(result.meetingName()).isEqualTo("United States Grand Prix");
            assertThat(result.sessionName()).isEqualTo(SessionName.RACE);
            assertThat(result.results()).hasSize(2);

            verify(sessionResultClient, never()).getSessionResultBySessionKey(anyInt());
            verify(sessionResultRepo, never()).saveAll(any());
        }

        @Test
        void fetchesFromApiAndStoresWhenDbIsEmpty() {
            Meeting meeting = new Meeting(1250, "United States Grand Prix", "USA", "Austin", "Austin GP", 2025, "https://www.url_to_flag.com");
            Session session = new Session(9640, 1250, SessionName.QUALIFYING, "RACE");

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

            SessionResultWithContext result =
                    sessionResultService.getResultByLocationYearAndSessionType(
                            "Austin", 2025, SessionName.QUALIFYING
                    );

            assertThat(result.results()).hasSize(1);
            verify(sessionResultRepo).saveAll(List.of(apiResult));
        }
    }

    @Nested
    @DisplayName("Sorting")
    class Sorting {

        @Test
        void sortsDnfsToBottom() {
            Meeting meeting = new Meeting(1250, "United States Grand Prix", "USA", "Austin", "Austin GP", 2025, "https://www.url_to_flag.com");
            Session session = new Session(9640, 1250, SessionName.RACE, "RACE");

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

            SessionResultWithContext result =
                    sessionResultService.getResultByLocationYearAndSessionType(
                            "Austin", 2025, SessionName.RACE
                    );

            assertThat(result.results().get(0).dnf()).isFalse();
            assertThat(result.results().get(1).dnf()).isTrue();
        }
    }

    @Test
    void getResultByLocationYearAndSessionType_sortsDnsAndDsqToBottom() {
        Meeting meeting = new Meeting(1250, "US", "USA", "Austin", "Austin GP", 2025, "https://www.url_to_flag.com");
        Session session = new Session(9640, 1250, SessionName.RACE, "RACE");

        SessionResultDto normal = new SessionResultDto(
                9640,
                List.of("0.0"),
                16,
                false,
                false,
                false,
                List.of(91.5),
                3
        );

        SessionResultDto dns = new SessionResultDto(
                9640,
                List.of("DNS"),
                22,
                false,
                true,
                false,
                List.of(),
                0
        );

        SessionResultDto dsq = new SessionResultDto(
                9640,
                List.of("DSQ"),
                10,
                false,
                false,
                true,
                List.of(),
                0
        );

        when(meetingService.getMeetingByYearAndLocation(2025, "Austin"))
                .thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(1250, SessionName.RACE))
                .thenReturn(session);
        when(sessionResultRepo.getSessionResultBySessionKey(9640))
                .thenReturn(List.of(dsq, dns, normal));

        SessionResultWithContext result =
                sessionResultService.getResultByLocationYearAndSessionType(
                        "Austin", 2025, SessionName.RACE
                );

        assertThat(result.results().get(0).driverNumber()).isEqualTo(16);
        assertThat(result.results().get(1).dns() || result.results().get(1).dsq()).isTrue();
        assertThat(result.results().get(2).dns() || result.results().get(2).dsq()).isTrue();
    }

    @Test
    void getResultByLocationYearAndSessionType_sortsPositionZeroAfterValidPositions() {
        Meeting meeting = new Meeting(1250, "US", "USA", "Austin", "Austin GP", 2025, "https://www.url_to_flag.com");
        Session session = new Session(9640, 1250, SessionName.RACE, "RACE");

        SessionResultDto validPosition = new SessionResultDto(
                9640,
                List.of("0.0"),
                55,
                false,
                false,
                false,
                List.of(92.0),
                5
        );

        SessionResultDto unknownPosition = new SessionResultDto(
                9640,
                List.of("0.0"),
                99,
                false,
                false,
                false,
                List.of(92.5),
                0
        );

        when(meetingService.getMeetingByYearAndLocation(2025, "Austin"))
                .thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(1250, SessionName.RACE))
                .thenReturn(session);
        when(sessionResultRepo.getSessionResultBySessionKey(9640))
                .thenReturn(List.of(unknownPosition, validPosition));

        SessionResultWithContext result =
                sessionResultService.getResultByLocationYearAndSessionType(
                        "Austin", 2025, SessionName.RACE
                );

        assertThat(result.results().get(0).driverNumber()).isEqualTo(55);
        assertThat(result.results().get(1).driverNumber()).isEqualTo(99);
    }
}

