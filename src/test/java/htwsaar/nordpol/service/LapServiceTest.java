package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.LapDto;
import htwsaar.nordpol.api.lap.ILapClient;
import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.repository.lap.ILapRepo;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LapServiceTest {

    @Mock
    ILapRepo lapRepo;

    @Mock
    ILapClient lapClient;

    @Mock
    MeetingService meetingService;

    @Mock
    SessionService sessionService;

    @Mock
    DriverService driverService;

    @InjectMocks
    LapService lapService;

    @Test
    void getLapsBySessionKeyAndDriverNumber_returnsLapsFromDatabase(){
        LapDto lap1 = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);
        LapDto lap2 = new LapDto(33, 1011, 2, 30.1, 25.8, 30.2, 91.1, false);
        LapDto lap3 = new LapDto(33, 1011, 3, 30.1, 29.8, 31.2, 90.1, true);

        List<LapDto> laps = List.of(lap1, lap2, lap3);

        when(lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(laps);

        List<Lap> result =
                lapService.getLapsBySessionKeyAndDriverNumber(1011, 33);

        assertThat(result.getFirst().sessionKey()).isEqualTo(1011);
        assertThat(result.getFirst().driverNumber()).isEqualTo(33);
        assertThat(result.getFirst().lapNumber()).isEqualTo(1);
        assertThat(result.getLast().sessionKey()).isEqualTo(1011);
        assertThat(result.getLast().driverNumber()).isEqualTo(33);
        assertThat(result.getLast().lapNumber()).isEqualTo(3);

        verify(lapClient, never()).getLapsBySessionKeyAndDriverNumber(1011, 33);
        verify(lapRepo).getLapsBySessionKeyAndDriverNumber(1011, 33);
    }

    @Test
    void getLapsBySessionKeyAndDriverNumber_fetchesFromApiAndSavesAllLaps(){
        when(lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(List.of());

        LapDto lap1 = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);
        LapDto lap2 = new LapDto(33, 1011, 2, 30.1, 25.8, 30.2, 91.1, false);
        LapDto lap3 = new LapDto(33, 1011, 3, 30.1, 29.8, 31.2, 90.1, true);

        List<LapDto> apiDto = List.of(lap1, lap2, lap3);

        when(lapClient.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(apiDto);

        List<Lap> result =
                lapService.getLapsBySessionKeyAndDriverNumber(1011, 33);

        assertThat(result.getFirst().sessionKey()).isEqualTo(1011);
        assertThat(result.getFirst().driverNumber()).isEqualTo(33);

        verify(lapRepo).saveAll(apiDto);
    }

    @Test
    void getLapsBySessionKeyAndDriverNumber_throwsException_IfLapNotFound(){
        when(lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(List.of());

        when(lapClient.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(List.of());

        assertThatThrownBy(() -> lapService.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .isInstanceOf(LapNotFoundException.class).hasMessageContaining("Laps not found with given parameters ");
    }

    @Test
    void getFastestLapByLocationYearAndSessionName_returnsFromDb() {
        String meetingName = "Italy GP";
        String location = "Monza";
        int year = 2024;
        SessionName sessionName = SessionName.QUALIFYING;
        Meeting meeting = new Meeting(100, "IT", "Italy", "Monza",meetingName, year);
        Session session = new Session(200, 100, sessionName, "Qualifying");

        when(meetingService.getMeetingByYearAndLocation(year, location)).thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(100, sessionName)).thenReturn(session);

        LapDto fastestDto = new LapDto(44, 200, 7, 25.0, 28.0, 27.123, 80.123, false);
        when(lapRepo.getFastestLapsBySessionKey(200, 1)).thenReturn(List.of(fastestDto));

        when(driverService.getDriverByNumberAndYear(44, year)).thenReturn(new Driver("Lewis", "Hamilton", 44, "Mercedes"));

        FastestLapsWithContext result = lapService.getFastestLapByLocationYearAndSessionName(location, year, sessionName, 1);

        assertThat(result.meetingName()).isEqualTo(meetingName);
        assertThat(result.sessionName().displayName()).isEqualTo(sessionName.displayName());
        assertThat(result.drivers().getFirst().lastName()).isEqualTo("Hamilton");
        assertThat(result.fastestLaps()).hasSize(1);
        Lap lap = result.fastestLaps().getFirst();
        assertThat(lap.sessionKey()).isEqualTo(200);
        assertThat(lap.driverNumber()).isEqualTo(44);
        assertThat(lap.lapNumber()).isEqualTo(7);

        verify(lapRepo).getFastestLapsBySessionKey(200, 1);
        verify(lapClient, never()).getLapsBySessionKey(anyInt());
        verify(lapRepo, never()).saveAll(anyList());
    }

    @Test
    void getFastestLapByLocationYearAndSessionName_fallbacksToApiAndSaves() {
        String meetingName = "Italy GP";
        String location = "Monza";
        int year = 2024;
        SessionName sessionName = SessionName.QUALIFYING;
        Meeting meeting = new Meeting(100, "IT", "Italy", "Monza", meetingName,year);
        Session session = new Session(200, 100, sessionName, "Qualifying");

        when(meetingService.getMeetingByYearAndLocation(year, location)).thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(100, sessionName)).thenReturn(session);

        when(lapRepo.getFastestLapsBySessionKey(200, 1)).thenReturn(List.of());

        // API returns various laps, only one valid (not pit-out and positive duration)
        LapDto pitOut = new LapDto(2, 200, 2, 26.0, 26.0, 26.0, 79.0, true);
        LapDto invalid = new LapDto(3, 200, 3, 0.0, 0.0, 0.0, -1.0, false);
        LapDto valid = new LapDto(1, 200, 4, 25.0, 27.0, 28.0, 80.0, false);
        when(lapClient.getLapsBySessionKey(200)).thenReturn(List.of(pitOut, invalid, valid));

        when(driverService.getDriverByNumberAndYear(1, year)).thenReturn(new Driver("Max", "Verstappen", 1, "Red Bull"));

        FastestLapsWithContext result = lapService.getFastestLapByLocationYearAndSessionName(location, year, sessionName, 1);

        assertThat(result.meetingName()).isEqualTo(meetingName);
        assertThat(result.sessionName().displayName()).isEqualTo(sessionName.displayName());
        assertThat(result.drivers().getFirst().lastName()).isEqualTo("Verstappen");
        assertThat(result.fastestLaps()).hasSize(1);
        Lap lap = result.fastestLaps().getFirst();
        assertThat(lap.driverNumber()).isEqualTo(1);
        assertThat(lap.lapDuration()).isEqualTo(80.0);

        verify(lapRepo).saveAll(List.of(pitOut, invalid, valid));
        verify(lapClient).getLapsBySessionKey(200);
    }

    @Test
    void getFastestLapByLocationYearAndSessionName_throwsWhenNoLapsAnywhere() {
        String location = "Monza";
        int year = 2024;
        SessionName sessionName = SessionName.QUALIFYING;
        Meeting meeting = new Meeting(100, "IT", "Italy", location,"Italy GP", year);
        Session session = new Session(200, 100, sessionName, "Qualifying");

        when(meetingService.getMeetingByYearAndLocation(year, location)).thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(100, sessionName)).thenReturn(session);

        when(lapRepo.getFastestLapsBySessionKey(200, 1)).thenReturn(List.of());
        when(lapClient.getLapsBySessionKey(200)).thenReturn(List.of());

        assertThatThrownBy(() -> lapService.getFastestLapByLocationYearAndSessionName(location, year, sessionName, 1))
                .isInstanceOf(LapNotFoundException.class);
    }

    @Test
    void getFastestLapByLocationYearSessionNameAndDriverNumber_usesDbAndFilters() {
        String location = "Monza";
        int year = 2024;
        SessionName sessionName = SessionName.RACE;
        int driverNumber = 16;

        Meeting meeting = new Meeting(300, "IT", "Italy", location, "Italy GP",year);
        Session session = new Session(400, 300, sessionName, "Race");

        when(meetingService.getMeetingByYearAndLocation(year, location)).thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(300, sessionName)).thenReturn(session);

        LapDto pitOut = new LapDto(driverNumber, 400, 10, 31.0, 30.0, 30.0, 95.0, true);
        LapDto valid1 = new LapDto(driverNumber, 400, 11, 29.0, 29.5, 30.0, 88.5, false);
        LapDto valid2 = new LapDto(driverNumber, 400, 12, 28.0, 30.0, 30.0, 88.0, false);
        when(lapRepo.getLapsBySessionKeyAndDriverNumber(400, driverNumber)).thenReturn(List.of(pitOut, valid1, valid2));

        when(driverService.getDriverByNumberAndYear(driverNumber, year)).thenReturn(new Driver("Charles", "Leclerc", driverNumber, "Ferrari"));

        FastestLapsWithContext result = lapService.getFastestLapByLocationYearSessionNameAndDriverNumber(location, year, sessionName, driverNumber, 1);

        assertThat(result.drivers().getFirst().lastName()).isEqualTo("Leclerc");
        assertThat(result.fastestLaps()).hasSize(1);
        Lap lap = result.fastestLaps().getFirst();
        assertThat(lap.lapNumber()).isEqualTo(12);
        assertThat(lap.lapDuration()).isEqualTo(88.0);

        verify(lapClient, never()).getLapsBySessionKeyAndDriverNumber(anyInt(), anyInt());
        verify(lapRepo, never()).saveAll(anyList());
    }

    @Test
    void getFastestLapByLocationYearSessionNameAndDriverNumber_fallbacksToApiAndSaves() {
        String location = "Monza";
        int year = 2024;
        SessionName sessionName = SessionName.RACE;
        int driverNumber = 55;

        Meeting meeting = new Meeting(500, "IT", "Italy", location, "Italy GP",year);
        Session session = new Session(600, 500, sessionName, "Race");

        when(meetingService.getMeetingByYearAndLocation(year, location)).thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(500, sessionName)).thenReturn(session);

        when(lapRepo.getLapsBySessionKeyAndDriverNumber(600, driverNumber)).thenReturn(List.of());

        LapDto invalid = new LapDto(driverNumber, 600, 5, 0.0, 0.0, 0.0, -1.0, false);
        LapDto valid = new LapDto(driverNumber, 600, 6, 29.0, 30.0, 30.0, 89.0, false);
        when(lapClient.getLapsBySessionKeyAndDriverNumber(600, driverNumber)).thenReturn(List.of(invalid, valid));

        when(driverService.getDriverByNumberAndYear(driverNumber, year)).thenReturn(new Driver("Carlos", "Sainz", driverNumber, "Ferrari"));

        FastestLapsWithContext result = lapService.getFastestLapByLocationYearSessionNameAndDriverNumber(location, year, sessionName, driverNumber, 1);

        assertThat(result.fastestLaps()).hasSize(1);
        assertThat(result.fastestLaps().getFirst().lapNumber()).isEqualTo(6);
        verify(lapRepo).saveAll(List.of(invalid, valid));
        verify(lapClient).getLapsBySessionKeyAndDriverNumber(600, driverNumber);
    }

    @Test
    void getFastestLapByLocationYearSessionNameAndDriverNumber_throwsWhenNoLapsAnywhere() {
        String location = "Monza";
        int year = 2024;
        SessionName sessionName = SessionName.RACE;
        int driverNumber = 20;

        Meeting meeting = new Meeting(700, "IT", "Italy", location, "Italy GP",year);
        Session session = new Session(800, 700, sessionName, "Race");

        when(meetingService.getMeetingByYearAndLocation(year, location)).thenReturn(meeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(700, sessionName)).thenReturn(session);

        when(lapRepo.getLapsBySessionKeyAndDriverNumber(800, driverNumber)).thenReturn(List.of());
        when(lapClient.getLapsBySessionKeyAndDriverNumber(800, driverNumber)).thenReturn(List.of());

        assertThatThrownBy(() -> lapService.getFastestLapByLocationYearSessionNameAndDriverNumber(location, year, sessionName, driverNumber, 1))
                .isInstanceOf(LapNotFoundException.class);
    }
}
