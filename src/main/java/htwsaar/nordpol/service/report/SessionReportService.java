package htwsaar.nordpol.service.report;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.domain.SessionReport;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import htwsaar.nordpol.service.weather.WeatherService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SessionReportService implements ISessionReportService {

    private final MeetingService meetingService;
    private final SessionService sessionService;
    private final SessionResultService sessionResultService;
    private final WeatherService weatherService;
    private final DriverService driverService;

    private final DriverResultFilter resultFilter;
    private final DriverResolver driverResolver;
    private final LapResolver lapResolver;

    public SessionReportService(MeetingService meetingService,
                                SessionService sessionService,
                                SessionResultService sessionResultService,
                                LapService lapService,
                                WeatherService weatherService,
                                DriverService driverService) {

        this.meetingService = requireNonNull(meetingService, "meetingService must not be null");
        this.sessionService = requireNonNull(sessionService, "sessionService must not be null");
        this.sessionResultService = requireNonNull(sessionResultService, "sessionResultService must not be null");
        this.weatherService = requireNonNull(weatherService, "weatherService must not be null");
        this.driverService = requireNonNull(driverService, "driverService must not be null");
        requireNonNull(lapService, "lapService must not be null");

        this.resultFilter = new DriverResultFilter();
        this.driverResolver = new DriverResolver(driverService);
        this.lapResolver = new LapResolver(lapService);
    }

    @Override
    public SessionReport buildReport(String location, int year, SessionName sessionName, Integer topDrivers, ProgressListener progress) {
        validateTopDrivers(topDrivers);

        progress.step("Fetching Meeting");
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);

        progress.step("Fetching Session");
        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meeting.meetingKey(), sessionName);

        progress.step("Fetching Weather");
        WeatherWithContext weather = weatherService.getWeatherByLocationYearAndSessionName(location, year, sessionName);

        progress.step("Fetching Results");
        SessionResultWithContext resultsContext = sessionResultService.getResultByLocationYearAndSessionType(location, year, sessionName);

        progress.step("Filtering results");
        List<SessionResult> reportResults = resultFilter.filterTopDrivers(resultsContext.results(), topDrivers);

        progress.step("Fetching missing drivers");
        driverService.preloadMissingDriversForMeeting(
                year,
                meeting.meetingKey(),
                resultFilter.extractDriverNumbers(reportResults)
        );

        progress.step("Building report object");
        SessionResultWithContext reportResultsContext = new SessionResultWithContext(
                resultsContext.meetingName(),
                resultsContext.sessionName(),
                reportResults
        );

        progress.step("Generating report");
        Map<Driver, List<Lap>> lapSeriesByDriver = buildLapSeriesByDriver(
                reportResults, year, meeting.meetingKey(), session.sessionKey()
        );

        progress.step("Finished report generation");
        return new SessionReport(
                meeting.meetingName(),
                session.sessionName(),
                meeting.year(),
                meeting.location(),
                weather,
                reportResultsContext,
                lapSeriesByDriver,
                meeting.countryFlagUrl()
        );
    }

    private void validateTopDrivers(Integer topDrivers) {
        if (topDrivers != null && topDrivers <= 0) {
            throw new IllegalArgumentException("topDrivers must be greater than zero.");
        }
    }

    private Map<Driver, List<Lap>> buildLapSeriesByDriver(List<SessionResult> results,
                                                          int year,
                                                          int meetingKey,
                                                          int sessionKey) {
        Map<Driver, List<Lap>> lapSeriesByDriver = new LinkedHashMap<>();
        for (SessionResult result : results) {
            Driver driver = driverResolver.resolve(result.driverNumber(), year, meetingKey);
            List<Lap> laps = lapResolver.resolve(sessionKey, result.driverNumber());
            lapSeriesByDriver.put(driver, laps);
        }
        return lapSeriesByDriver;
    }
}
