package htwsaar.nordpol.service;


import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.api.meeting.MeetingClient;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.service.meeting.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {

    @Mock
    IMeetingRepo meetingRepo;

    @Mock
    MeetingClient meetingClient;

    ICacheService cacheService;

    MeetingService meetingService;

    @BeforeEach
    void setup() {
        cacheService = ApplicationContext.cacheService();
        meetingService = new MeetingService(meetingRepo, meetingClient, cacheService);
    }

    @Nested
    @DisplayName("getMeetingByYearAndLocation")
    class GetMeetingByYearAndLocation {

        @Test
        void returnsMeetingFromDatabase() {
            MeetingDto meetingDto =
                    new MeetingDto("JPN", "Japan", "Suzuka", 1256, "Japanese Grand Prix", 2025, "https://www.url_to_flag.com");

            when(meetingRepo.getMeetingByYearAndLocation(2025, "Suzuka"))
                    .thenReturn(Optional.of(meetingDto));

            Meeting result =
                    meetingService.getMeetingByYearAndLocation(2025, "Suzuka");

            assertThat(result.year()).isEqualTo(2025);

            verify(meetingClient, never()).getMeetingByYearAndLocation(2025, "Suzuka");
            verify(meetingRepo).getMeetingByYearAndLocation(2025, "Suzuka");
        }

        @Test
        void fetchesFromApiAndSavesSession() {
            when(meetingRepo.getMeetingByYearAndLocation(2025, "Suzuka"))
                    .thenReturn(Optional.empty());

            MeetingDto apiDto =
                    new MeetingDto("JPN", "Japan", "Suzuka", 1256, "Japanese Grand Prix", 2025, "https://www.url_to_flag.com");

            when(meetingClient.getMeetingByYearAndLocation(2025, "Suzuka"))
                    .thenReturn(Optional.of(apiDto));

            Meeting result =
                    meetingService.getMeetingByYearAndLocation(2025, "Suzuka");

            assertThat(result.year()).isEqualTo(2025);
            assertThat(result.location()).isEqualTo("Suzuka");

            verify(meetingRepo).save(apiDto);
        }

        @Test
        void throwsException_IfMeetingNotFound() {
            when(meetingRepo.getMeetingByYearAndLocation(2025, "Suzuka"))
                    .thenReturn(Optional.empty());

            when(meetingClient.getMeetingByYearAndLocation(2025, "Suzuka"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> meetingService.getMeetingByYearAndLocation(2025, "Suzuka"))
                    .isInstanceOf(MeetingNotFoundException.class)
                    .hasMessageContaining("Meeting not found");
        }
    }

    @Nested
    @DisplayName("getMeetingsForYear")
    class getMeetingsForYear {

        @Test
        void queriesApiFirst() {
            MeetingDto dto1 = new MeetingDto("ITA", "Italy", "Monza", 1234, "Monza GP", 2025, "https://www.url_to_flag.com");
            MeetingDto dto2 = new MeetingDto("ITA", "Italy", "Imola", 1235, "Imola GP", 2025, "https://www.url_to_flag.com");

            when(meetingClient.getMeetingsByYear(2025))
                    .thenReturn(List.of(dto1, dto2));

            List<Meeting> results = meetingService.getMeetingsByYear(2025);

            assertThat(results).hasSize(2);
            assertThat(results.getFirst().year()).isEqualTo(2025);
            verify(meetingRepo, never()).getMeetingsByYear(2025);
        }

        @Test
        void fallsBackToDb_whenApiResultIsEmpty() {
            MeetingDto dto1 = new MeetingDto("ITA", "Italy", "Monza", 1234, "Monza GP", 2025, "https://www.url_to_flag.com");
            MeetingDto dto2 = new MeetingDto("ITA", "Italy", "Imola", 1235, "Imola GP", 2025, "https://www.url_to_flag.com");

            when(meetingClient.getMeetingsByYear(2025))
                    .thenReturn(List.of());

            when(meetingRepo.getMeetingsByYear(2025))
                    .thenReturn(List.of(dto1, dto2));

            List<Meeting> results = meetingService.getMeetingsByYear(2025);

            assertThat(results).hasSize(2);
            assertThat(results.getFirst().year()).isEqualTo(2025);
            verify(meetingRepo).getMeetingsByYear(2025);
        }

        @Test
        void throwsException_ifNoDataIsAvailable() {
            when(meetingClient.getMeetingsByYear(2025))
                    .thenReturn(List.of());

            when(meetingRepo.getMeetingsByYear(2025))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> meetingService.getMeetingsByYear(2025)).
                    isInstanceOf(MeetingNotFoundException.class);
        }
    }
}
