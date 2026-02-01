package htwsaar.nordpol.service;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.report.SessionReportService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import htwsaar.nordpol.service.weather.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionReportServiceTest {

    private static final String LOCATION = "Monza";
    private static final int YEAR = 2024;
    private static final int MEETING_KEY = 1244;
    private static final int SESSION_KEY = 9590;
    private static final String MEETING_NAME = "Italian Grand Prix";
    private static final String COUNTRY_NAME = "Italy";
    private static final String COUNTRY_CODE = "ITA";

    @Mock
    private MeetingService meetingService;

    @Mock
    private SessionService sessionService;

    @Mock
    private SessionResultService sessionResultService;

    @Mock
    private LapService lapService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private DriverService driverService;

    private SessionReportService sessionReportService;

    private Meeting defaultMeeting;
    private Session defaultSession;
    private Weather defaultWeather;
    private WeatherWithContext defaultWeatherContext;

    @BeforeEach
    void setUp() {
        sessionReportService = new SessionReportService(
                meetingService,
                sessionService,
                sessionResultService,
                lapService,
                weatherService,
                driverService
        );

        defaultMeeting = new Meeting(MEETING_KEY, COUNTRY_CODE, COUNTRY_NAME, LOCATION, MEETING_NAME, YEAR);
        defaultSession = new Session(SESSION_KEY, MEETING_KEY, SessionName.RACE, "Race");
        defaultWeather = new Weather(SESSION_KEY, MEETING_KEY, 22.0, 55.0, false, 34.0, 180.0, 4.5);
        defaultWeatherContext = new WeatherWithContext(MEETING_NAME, COUNTRY_NAME, SessionName.RACE, defaultWeather);
    }

    @Test
    void buildReport_throwsException_whenTopDriversIsZero() {
        assertThatThrownBy(() -> sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, 0, message -> {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("topDrivers must be greater than zero.");
    }

    @Test
    void buildReport_throwsException_whenTopDriversIsNegative() {
        assertThatThrownBy(() -> sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, -1, message -> {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("topDrivers must be greater than zero.");
    }

    @Test
    void buildReport_returnsCompleteReport_withAllData() {
        Driver driver1 = new Driver("Charles", "Leclerc", 16, "Ferrari");
        Driver driver2 = new Driver("Carlos", "Sainz", 55, "Ferrari");
        List<SessionResult> results = List.of(
                new SessionResult(16, 1, List.of("+0.000"), List.of(82.5), false, false, false),
                new SessionResult(55, 2, List.of("+3.456"), List.of(83.0), false, false, false)
        );
        Lap lap1 = new Lap(16, 1, SESSION_KEY, 28.1, 21.2, 32.0, 81.3, false);
        Lap lap2 = new Lap(55, 1, SESSION_KEY, 28.5, 21.8, 32.5, 82.8, false);

        setupMocksForReport(results);
        when(driverService.getDriverByNumberWithFallback(16, YEAR, MEETING_KEY)).thenReturn(driver1);
        when(driverService.getDriverByNumberWithFallback(55, YEAR, MEETING_KEY)).thenReturn(driver2);
        when(lapService.getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 16)).thenReturn(List.of(lap1));
        when(lapService.getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 55)).thenReturn(List.of(lap2));

        SessionReport report = sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, null, message -> {});

        assertThat(report.meetingName()).isEqualTo(MEETING_NAME);
        assertThat(report.sessionName()).isEqualTo(SessionName.RACE);
        assertThat(report.year()).isEqualTo(YEAR);
        assertThat(report.location()).isEqualTo(LOCATION);
        assertThat(report.weather()).isEqualTo(defaultWeatherContext);
        assertThat(report.sessionResults().results()).hasSize(2);
        assertThat(report.lapSeriesByDriver()).hasSize(2);
        assertThat(report.lapSeriesByDriver().get(driver1)).containsExactly(lap1);
        assertThat(report.lapSeriesByDriver().get(driver2)).containsExactly(lap2);
    }

    @Test
    void buildReport_filtersResults_toTopNDrivers() {
        Driver driver55 = new Driver("Carlos", "Sainz", 55, "Ferrari");
        List<SessionResult> results = List.of(
                new SessionResult(55, 1, List.of("+0.000"), List.of(82.5), false, false, false),
                new SessionResult(16, 2, List.of("+5.123"), List.of(83.0), false, false, false),
                new SessionResult(1, 3, List.of("+10.456"), List.of(84.0), false, false, false)
        );
        Lap lap = new Lap(55, 1, SESSION_KEY, 28.1, 21.2, 32.0, 81.3, false);

        setupMocksForReport(results);
        when(driverService.getDriverByNumberWithFallback(55, YEAR, MEETING_KEY)).thenReturn(driver55);
        when(lapService.getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 55)).thenReturn(List.of(lap));

        SessionReport report = sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, 1, message -> {});

        assertThat(report.sessionResults().results()).hasSize(1);
        assertThat(report.sessionResults().results().getFirst().driverNumber()).isEqualTo(55);
        assertThat(report.lapSeriesByDriver()).hasSize(1);
        assertThat(report.lapSeriesByDriver().keySet()).containsExactly(driver55);

        verify(driverService).preloadMissingDriversForMeeting(YEAR, MEETING_KEY, List.of(55));
        verify(lapService).getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 55);
        verify(lapService, never()).getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 16);
        verify(lapService, never()).getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 1);
    }

    @Test
    void buildReport_handlesEmptyResultsList() {
        List<SessionResult> emptyResults = List.of();

        setupMocksForReport(emptyResults);

        SessionReport report = sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, null, message -> {});

        assertThat(report.sessionResults().results()).isEmpty();
        assertThat(report.lapSeriesByDriver()).isEmpty();

        verify(driverService).preloadMissingDriversForMeeting(YEAR, MEETING_KEY, List.of());
        verifyNoInteractions(lapService);
    }

    @Test
    void buildReport_preloadsDrivers_forMeetingBeforeResolving() {
        Driver driver = new Driver("Max", "Verstappen", 1, "Red Bull Racing");
        List<SessionResult> results = List.of(
                new SessionResult(1, 1, List.of("+0.000"), List.of(82.5), false, false, false)
        );

        setupMocksForReport(results);
        when(driverService.getDriverByNumberWithFallback(1, YEAR, MEETING_KEY)).thenReturn(driver);
        when(lapService.getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 1)).thenReturn(List.of());

        sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, null, message -> {});

        verify(driverService).preloadMissingDriversForMeeting(YEAR, MEETING_KEY, List.of(1));
    }

    @Test
    void buildReport_createsUnknownDriver_whenDriverLookupFails() {
        List<SessionResult> results = List.of(
                new SessionResult(99, 1, List.of("+0.000"), List.of(82.5), false, false, false)
        );

        setupMocksForReport(results);
        when(driverService.getDriverByNumberWithFallback(99, YEAR, MEETING_KEY))
                .thenThrow(new DriverNotFoundException(99, YEAR));
        when(lapService.getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 99)).thenReturn(List.of());

        SessionReport report = sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, null, message -> {});

        assertThat(report.lapSeriesByDriver()).hasSize(1);
        Driver unknownDriver = report.lapSeriesByDriver().keySet().iterator().next();
        assertThat(unknownDriver.firstName()).isEqualTo("Unknown");
        assertThat(unknownDriver.lastName()).isEqualTo("Driver");
        assertThat(unknownDriver.driverNumber()).isEqualTo(99);
        assertThat(unknownDriver.teamName()).isEqualTo("Unknown");
    }

    @Test
    void buildReport_returnsEmptyLapList_whenLapLookupFails() {
        Driver driver = new Driver("Lewis", "Hamilton", 44, "Mercedes");
        List<SessionResult> results = List.of(
                new SessionResult(44, 1, List.of("+0.000"), List.of(82.5), false, false, false)
        );

        setupMocksForReport(results);
        when(driverService.getDriverByNumberWithFallback(44, YEAR, MEETING_KEY)).thenReturn(driver);
        when(lapService.getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 44))
                .thenThrow(new LapNotFoundException(SESSION_KEY, 44));

        SessionReport report = sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, null, message -> {});

        assertThat(report.lapSeriesByDriver().get(driver)).isEmpty();
    }

    @Test
    void buildReport_propagatesException_whenWeatherServiceFails() {
        when(meetingService.getMeetingByYearAndLocation(YEAR, LOCATION)).thenReturn(defaultMeeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(MEETING_KEY, SessionName.RACE))
                .thenReturn(defaultSession);
        when(weatherService.getWeatherByLocationYearAndSessionName(LOCATION, YEAR, SessionName.RACE))
                .thenThrow(new RuntimeException("Weather service unavailable"));

        assertThatThrownBy(() -> sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, null, message -> {}))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Weather service unavailable");
    }

    @Test
    void buildReport_propagatesException_whenSessionResultServiceFails() {
        when(meetingService.getMeetingByYearAndLocation(YEAR, LOCATION)).thenReturn(defaultMeeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(MEETING_KEY, SessionName.RACE))
                .thenReturn(defaultSession);
        when(weatherService.getWeatherByLocationYearAndSessionName(LOCATION, YEAR, SessionName.RACE))
                .thenReturn(defaultWeatherContext);
        when(sessionResultService.getResultByLocationYearAndSessionType(LOCATION, YEAR, SessionName.RACE))
                .thenThrow(new RuntimeException("Results not found"));

        assertThatThrownBy(() -> sessionReportService.buildReport(LOCATION, YEAR, SessionName.RACE, null, message -> {}))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Results not found");
    }

    @Test
    void buildReport_buildsReportForQualifyingSession() {
        Session qualifyingSession = new Session(SESSION_KEY, MEETING_KEY, SessionName.QUALIFYING, "Qualifying");
        WeatherWithContext qualifyingWeather = new WeatherWithContext(
                MEETING_NAME, COUNTRY_NAME, SessionName.QUALIFYING, defaultWeather);
        Driver driver = new Driver("Max", "Verstappen", 1, "Red Bull Racing");
        List<SessionResult> results = List.of(
                new SessionResult(1, 1, List.of("+0.000", "+0.100", "+0.050"),
                        List.of(91.5, 90.8, 90.2), false, false, false)
        );

        when(meetingService.getMeetingByYearAndLocation(YEAR, LOCATION)).thenReturn(defaultMeeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(MEETING_KEY, SessionName.QUALIFYING))
                .thenReturn(qualifyingSession);
        when(weatherService.getWeatherByLocationYearAndSessionName(LOCATION, YEAR, SessionName.QUALIFYING))
                .thenReturn(qualifyingWeather);
        when(sessionResultService.getResultByLocationYearAndSessionType(LOCATION, YEAR, SessionName.QUALIFYING))
                .thenReturn(new SessionResultWithContext(MEETING_NAME, SessionName.QUALIFYING, results));
        when(driverService.getDriverByNumberWithFallback(1, YEAR, MEETING_KEY)).thenReturn(driver);
        when(lapService.getLapsBySessionKeyAndDriverNumber(SESSION_KEY, 1)).thenReturn(List.of());

        SessionReport report = sessionReportService.buildReport(LOCATION, YEAR, SessionName.QUALIFYING, null, message -> {});

        assertThat(report.sessionName()).isEqualTo(SessionName.QUALIFYING);
        assertThat(report.sessionResults().sessionName()).isEqualTo(SessionName.QUALIFYING);
    }

    @Test
    void buildReport_buildsReportForPracticeSession() {
        Session practiceSession = new Session(SESSION_KEY, MEETING_KEY, SessionName.PRACTICE1, "Practice 1");
        WeatherWithContext practiceWeather = new WeatherWithContext(
                MEETING_NAME, COUNTRY_NAME, SessionName.PRACTICE1, defaultWeather);
        List<SessionResult> results = List.of();

        when(meetingService.getMeetingByYearAndLocation(YEAR, LOCATION)).thenReturn(defaultMeeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(MEETING_KEY, SessionName.PRACTICE1))
                .thenReturn(practiceSession);
        when(weatherService.getWeatherByLocationYearAndSessionName(LOCATION, YEAR, SessionName.PRACTICE1))
                .thenReturn(practiceWeather);
        when(sessionResultService.getResultByLocationYearAndSessionType(LOCATION, YEAR, SessionName.PRACTICE1))
                .thenReturn(new SessionResultWithContext(MEETING_NAME, SessionName.PRACTICE1, results));

        SessionReport report = sessionReportService.buildReport(LOCATION, YEAR, SessionName.PRACTICE1, null, message -> {});

        assertThat(report.sessionName()).isEqualTo(SessionName.PRACTICE1);
    }

    private void setupMocksForReport(List<SessionResult> results) {
        when(meetingService.getMeetingByYearAndLocation(YEAR, LOCATION)).thenReturn(defaultMeeting);
        when(sessionService.getSessionByMeetingKeyAndSessionName(MEETING_KEY, SessionName.RACE))
                .thenReturn(defaultSession);
        when(weatherService.getWeatherByLocationYearAndSessionName(LOCATION, YEAR, SessionName.RACE))
                .thenReturn(defaultWeatherContext);
        when(sessionResultService.getResultByLocationYearAndSessionType(LOCATION, YEAR, SessionName.RACE))
                .thenReturn(new SessionResultWithContext(MEETING_NAME, SessionName.RACE, results));
    }
}
