package htwsaar.nordpol.service;


import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.api.meeting.MeetingClient;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.service.meeting.MeetingService;
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
public class MeetingServiceTest {

    @Mock
    IMeetingRepo IMeetingRepo;

    @Mock
    MeetingClient meetingClient;

    @InjectMocks
    MeetingService meetingService;

    @Test
    void getMeetingByYearAndLocation_returnsMeetingFromDatabase(){
        MeetingDto meetingDto =
                new MeetingDto("JPN", "Japan", "Suzuka", 1256, "Japanese Grand Prix", 2025);

        when(IMeetingRepo.getMeetingByYearAndLocation(2025, "Suzuka"))
                .thenReturn(Optional.of(meetingDto));

        Meeting result =
                meetingService.getMeetingByYearAndLocation(2025, "Suzuka");

        assertThat(result.year()).isEqualTo(2025);

        verify(meetingClient, never()).getMeetingByYearAndLocation(2025, "Suzuka");
        verify(IMeetingRepo).getMeetingByYearAndLocation(2025, "Suzuka");
    }

    @Test
    void getMeetingByYearAndLocation_fetchesFromApiAndSavesSession(){
        when(IMeetingRepo.getMeetingByYearAndLocation(2025, "Suzuka"))
                .thenReturn(Optional.empty());

        MeetingDto apiDto =
                new MeetingDto("JPN", "Japan", "Suzuka", 1256, "Japanese Grand Prix", 2025);

        when(meetingClient.getMeetingByYearAndLocation(2025, "Suzuka"))
                .thenReturn(Optional.of(apiDto));

        Meeting result =
                meetingService.getMeetingByYearAndLocation(2025, "Suzuka");

        assertThat(result.year()).isEqualTo(2025);
        assertThat(result.location()).isEqualTo("Suzuka");

        verify(IMeetingRepo).save(apiDto);
    }

    @Test
    void getMeetingByYearAndLocation_throwsException_IfMeetingNotFound(){
        when(IMeetingRepo.getMeetingByYearAndLocation(2025, "Suzuka"))
                .thenReturn(Optional.empty());

        when(meetingClient.getMeetingByYearAndLocation(2025, "Suzuka"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.getMeetingByYearAndLocation(2025, "Suzuka"))
                .isInstanceOf(MeetingNotFoundException.class)
                .hasMessageContaining("Meeting not found");
    }


}
