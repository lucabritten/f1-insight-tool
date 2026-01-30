package htwsaar.nordpol.config;

import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.api.driver.IDriverClient;
import htwsaar.nordpol.api.lap.LapClient;
import htwsaar.nordpol.api.meeting.MeetingClient;
import htwsaar.nordpol.api.session.SessionClient;
import htwsaar.nordpol.api.sessionresult.ISessionResultClient;
import htwsaar.nordpol.api.sessionresult.SessionResultClient;
import htwsaar.nordpol.api.weather.WeatherClient;
import htwsaar.nordpol.repository.driver.IDriverRepo;
import htwsaar.nordpol.repository.driver.JooqDriverRepo;
import htwsaar.nordpol.repository.lap.ILapRepo;
import htwsaar.nordpol.repository.lap.JooqLapRepo;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.repository.meeting.JooqMeetingRepo;
import htwsaar.nordpol.repository.session.ISessionRepo;
import htwsaar.nordpol.repository.session.JooqSessionRepo;
import htwsaar.nordpol.repository.sessionresult.ISessionResultRepo;
import htwsaar.nordpol.repository.sessionresult.JooqSessionResultRepo;
import htwsaar.nordpol.repository.weather.IWeatherRepo;
import htwsaar.nordpol.repository.weather.JooqWeatherRepo;
import htwsaar.nordpol.service.CacheService;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.report.SessionReportService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import htwsaar.nordpol.service.weather.WeatherService;

/**
 * Simple application context responsible for wiring application services.
 *
 * <p>This class acts as a lightweight dependency container and is used
 * to create fully initialized service instances.</p>
 *
 * <p>It centralizes object creation and keeps CLI commands free from
 * infrastructure and configuration logic.</p>
 */
public class
ApplicationContext {

    private ApplicationContext(){

    }
    /**
     * Creates and returns a {@link DriverService} instance.
     *
     * <p>The service is composed of a repository backed by a local SQLite
     * database and a client for accessing the OpenF1 API.</p>
     *
     * @return a fully initialized DriverService
     */

    public static ICacheService cacheService() {
        return new CacheService();
    }

    public static DriverService driverService() {
        IDriverRepo IDriverRepo = new JooqDriverRepo(JooqConfig.createContext());
        IDriverClient driverClient = new DriverClient();
        return new DriverService(IDriverRepo, driverClient, meetingService(), cacheService());
    }

    public static MeetingService meetingService() {
        IMeetingRepo IMeetingRepo = new JooqMeetingRepo(JooqConfig.createContext());
        MeetingClient meetingClient = new MeetingClient();
        return new MeetingService(IMeetingRepo, meetingClient, cacheService());
    }

    public static SessionService sessionService() {
        ISessionRepo ISessionRepo = new JooqSessionRepo(JooqConfig.createContext());
        SessionClient sessionClient = new SessionClient();
        return new SessionService(ISessionRepo, sessionClient, cacheService());
    }

    public static WeatherService weatherService() {
        IWeatherRepo weatherRepo = new JooqWeatherRepo(JooqConfig.createContext());
        WeatherClient weatherClient = new WeatherClient();
        return new WeatherService(weatherClient, weatherRepo, sessionService(),meetingService());
    }

    public static LapService lapService() {
        ILapRepo lapRepo = new JooqLapRepo(JooqConfig.createContext());
        LapClient lapClient = new LapClient();
        return new LapService(lapRepo, lapClient, meetingService(), sessionService(), driverService());
    }

    public static SessionResultService sessionResultService() {
        ISessionResultClient client = new SessionResultClient();
        ISessionResultRepo repo = new JooqSessionResultRepo(JooqConfig.createContext());
        return new SessionResultService(meetingService(), sessionService(), client, repo);
    }

    public static SessionReportService sessionReportService() {
        return new SessionReportService(
                meetingService(),
                sessionService(),
                sessionResultService(),
                lapService(),
                weatherService(),
                driverService()
        );
    }
}
