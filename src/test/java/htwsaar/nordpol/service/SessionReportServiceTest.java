package htwsaar.nordpol.service;

import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.report.SessionReportService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import htwsaar.nordpol.service.weather.WeatherService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SessionReportServiceTest {

    @Mock
    MeetingService meetingService;

    @Mock
    SessionService sessionService;

    @Mock
    SessionResultService sessionResultService;

    @Mock
    LapService lapService;

    @Mock
    WeatherService weatherService;

    @Mock
    DriverService driverService;

    @InjectMocks
    SessionReportService sessionReportService;

//    @Test
//    void buildReport_filtersTopDrivers_andBuildsLapSeries() {
//        Meeting meeting = new Meeting(1244, "ITA", "Italy", "Monza", "Italian Grand Prix", 2024);
//        Session session = new Session(9590, 1244, SessionName.RACE, "Race");
//        when(meetingService.getMeetingByYearAndLocation(2024, "Monza")).thenReturn(meeting);
//        when(sessionService.getSessionByMeetingKeyAndSessionName(1244, SessionName.RACE)).thenReturn(session);
//
//        List<SessionResult> results = List.of(
//                new SessionResult(55, 1, List.of("+0.000"), List.of(1.0), false, false, false),
//                new SessionResult(16, 2, List.of("+5.123"), List.of(1.0), false, false, false)
//        );
//        when(sessionResultService.getResultByLocationYearAndSessionType("Monza", 2024, SessionName.RACE))
//                .thenReturn(new SessionResultWithContext("Italian Grand Prix", SessionName.RACE, results));
//
//        Weather weather = new Weather(9590, 1244, 22.0, 55.0, false, 34.0, 180.0, 4.5);
//        when(weatherService.getWeatherByLocationYearAndSessionName("Monza", 2024, SessionName.RACE))
//                .thenReturn(new WeatherWithContext("Italian Grand Prix", "Italy", SessionName.RACE, weather));
//
//        Driver driver55 = new Driver("Carlos", "Sainz", 55, "Ferrari");
//        when(driverService.getDriverByNumberWithFallback(55, 2024, 1244)).thenReturn(driver55);
//        when(lapService.getLapsBySessionKeyAndDriverNumber(9590, 55)).thenReturn(List.of(
//                new Lap(55, 1, 9590, 28.1, 21.2, 32.0, 81.3, false)
//        ));
//
//        SessionReport report = sessionReportService.buildReport("Monza", 2024, SessionName.RACE, 1);
//
//        assertThat(report.meetingName()).isEqualTo("Italian Grand Prix");
//        assertThat(report.sessionResults().results()).hasSize(1);
//        assertThat(report.lapSeriesByDriver()).hasSize(1);
//        assertThat(report.lapSeriesByDriver().keySet()).containsExactly(driver55);
//
//        verify(driverService).preloadDriversForYear(2024);
//        verify(lapService).getLapsBySessionKeyAndDriverNumber(9590, 55);
//        verify(lapService, never()).getLapsBySessionKeyAndDriverNumber(9590, 16);
//    }
//
//    @Test
//    void buildReport_allowsMissingWeatherAndResults() {
//        Meeting meeting = new Meeting(1244, "ITA", "Italy", "Monza", "Italian Grand Prix", 2024);
//        Session session = new Session(9590, 1244, SessionName.RACE, "Race");
//        when(meetingService.getMeetingByYearAndLocation(2024, "Monza")).thenReturn(meeting);
//        when(sessionService.getSessionByMeetingKeyAndSessionName(1244, SessionName.RACE)).thenReturn(session);
//
//        when(weatherService.getWeatherByLocationYearAndSessionName("Monza", 2024, SessionName.RACE))
//                .thenThrow(new RuntimeException("Weather missing"));
//        when(sessionResultService.getResultByLocationYearAndSessionType("Monza", 2024, SessionName.RACE))
//                .thenThrow(new RuntimeException("Results missing"));
//
//        SessionReport report = sessionReportService.buildReport("Monza", 2024, SessionName.RACE, null);
//
//        assertThat(report.weather().weather()).isNull();
//        assertThat(report.sessionResults().results()).isEmpty();
//        assertThat(report.lapSeriesByDriver()).isEqualTo(Map.of());
//
//        verify(driverService).preloadDriversForYear(2024);
//        verifyNoInteractions(lapService);
//    }
}
