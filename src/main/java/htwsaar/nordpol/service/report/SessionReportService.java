package htwsaar.nordpol.service.report;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.report.SessionReport;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import htwsaar.nordpol.service.weather.WeatherService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SessionReportService {

    private final MeetingService meetingService;
    private final SessionService sessionService;
    private final SessionResultService sessionResultService;
    private final LapService lapService;
    private final WeatherService weatherService;
    private final DriverService driverService;

    public SessionReportService(MeetingService meetingService,
                                SessionService sessionService,
                                SessionResultService sessionResultService,
                                LapService lapService,
                                WeatherService weatherService,
                                DriverService driverService) {
        if (meetingService == null){
            throw new IllegalArgumentException("meetingService must not be null.");
        }
        if (sessionService == null) {
            throw new IllegalArgumentException("sessionService must not be null.");
        }
        if (sessionResultService == null) {
            throw new IllegalArgumentException("sessionResultService must not be null.");
        }
        if (lapService == null) {
            throw new IllegalArgumentException("lapService must not be null.");
        }
        if (weatherService == null) {
            throw new IllegalArgumentException("weatherService must not be null.");
        }
        if (driverService == null) {
            throw new IllegalArgumentException("driverService must not be null.");
        }

        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.sessionResultService = sessionResultService;
        this.lapService = lapService;
        this.weatherService = weatherService;
        this.driverService = driverService;
    }

    public SessionReport buildReport(String location, int year, SessionName sessionName, Integer topDrivers) {
        if (topDrivers != null && topDrivers <= 0) {
            throw new IllegalArgumentException("topDrivers must be greater than zero.");
        }

        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meeting.meetingKey(), sessionName);

        driverService.preloadDriversForYear(year);

        WeatherWithContext weather = weatherService.getWeatherByLocationYearAndSessionName(location, year, sessionName);

        SessionResultWithContext resultsContext = sessionResultService.getResultByLocationYearAndSessionType(location, year, sessionName);

        List<SessionResult> reportResults = filterTopDrivers(resultsContext.results(), topDrivers);

        SessionResultWithContext reportResultsContext = new SessionResultWithContext(
                resultsContext.meetingName(),
                resultsContext.sessionName(),
                reportResults
        );

        Map<Driver, List<Lap>> lapSeriesByDriver = new LinkedHashMap<>();
        int meetingKey = meeting.meetingKey();
        for (SessionResult result : reportResults) {
            Driver driver = resolveDriver(result.driverNumber(), year, meetingKey);
            List<Lap> laps = resolveLaps(session.sessionKey(), result.driverNumber());
            lapSeriesByDriver.put(driver, laps);
        }

        return new SessionReport(
                meeting.meetingName(),
                session.sessionName(),
                meeting.year(),
                meeting.location(),
                weather,
                reportResultsContext,
                lapSeriesByDriver
        );
    }

    private List<SessionResult> filterTopDrivers(List<SessionResult> results, Integer topDrivers) {
        if (topDrivers == null || results.isEmpty()) {
            return results;
        }
        int limit = Math.min(topDrivers, results.size());
        return List.copyOf(results.subList(0, limit));
    }

    private Driver resolveDriver(int driverNumber, int year, int meetingKey) {
        try {
            return driverService.getDriverByNumberWithFallback(driverNumber, year, meetingKey);
        } catch (RuntimeException ex) {
            return new Driver("Unknown", "Driver", driverNumber, "Unknown");
        }
    }

    private List<Lap> resolveLaps(int sessionKey, int driverNumber) {
        try {
            return lapService.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        } catch (RuntimeException ex) {
            return List.of();
        }
    }
}
