package htwsaar.nordpol.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.api.driver.IDriverClient;
import htwsaar.nordpol.api.lap.LapClient;
import htwsaar.nordpol.api.meeting.MeetingClient;
import htwsaar.nordpol.api.session.SessionClient;
import htwsaar.nordpol.api.sessionresult.ISessionResultClient;
import htwsaar.nordpol.api.sessionresult.SessionResultClient;
import htwsaar.nordpol.api.weather.WeatherClient;
import htwsaar.nordpol.config.api.ApiClientConfig;
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
import htwsaar.nordpol.service.lap.ILapService;
import htwsaar.nordpol.service.lap.LapService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.report.SessionReportService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import htwsaar.nordpol.service.sessionResult.SessionResultService;
import htwsaar.nordpol.service.weather.IWeatherService;
import htwsaar.nordpol.service.weather.WeatherService;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.jooq.DSLContext;

/**
 * Application-wide context responsible for wiring and sharing services.
 *
 * <p>This class acts as a lightweight dependency container and follows
 * the Singleton pattern. All services, repositories and infrastructure
 * components are created once and shared for the lifetime of the application</p>
 *
 * <p>It centralizes object creation and keeps CLI commands free from
 * infrastructure and configuration logic.</p>
 */
public class ApplicationContext {

    private DSLContext dslContext;

    private static ApplicationContext instance;

    private final ICacheService cacheService;
    private final IDriverRepo driverRepo;
    private final IMeetingRepo meetingRepo;
    private final ISessionRepo sessionRepo;
    private final ILapRepo lapRepo;
    private final ISessionResultRepo sessionResultRepo;
    private final IWeatherRepo weatherRepo;

    private final IDriverClient driverClient;
    private final MeetingClient meetingClient;
    private final SessionClient sessionClient;
    private final LapClient lapClient;
    private final ISessionResultClient sessionResultClient;
    private final WeatherClient weatherClient;

    private final DriverService driverService;
    private final IMeetingService meetingService;
    private final ISessionService sessionService;
    private final ILapService lapService;
    private final ISessionResultService sessionResultService;
    private final IWeatherService weatherService;
    private final SessionReportService sessionReportService;

    private final ObjectMapper objectMapper;

    private ApplicationContext() {
        this.cacheService = new CacheService();
        this.objectMapper = new ObjectMapper();

        this.driverRepo = new JooqDriverRepo(dslContext());
        this.meetingRepo = new JooqMeetingRepo(dslContext());
        this.sessionRepo = new JooqSessionRepo(dslContext());
        this.lapRepo = new JooqLapRepo(dslContext());
        this.sessionResultRepo = new JooqSessionResultRepo(dslContext());
        this.weatherRepo = new JooqWeatherRepo(dslContext());

        this.driverClient = new DriverClient(ApiClientConfig.openF1HttpClient(), objectMapper());
        this.meetingClient = new MeetingClient(ApiClientConfig.openF1HttpClient(), objectMapper());
        this.sessionClient = new SessionClient(ApiClientConfig.openF1HttpClient(), objectMapper());
        this.lapClient = new LapClient(ApiClientConfig.openF1HttpClient(), objectMapper());
        this.sessionResultClient = new SessionResultClient(ApiClientConfig.openF1HttpClient(), objectMapper());
        this.weatherClient = new WeatherClient(ApiClientConfig.openF1HttpClient(), objectMapper());

        this.meetingService = new MeetingService(meetingRepo, meetingClient, cacheService);
        this.sessionService = new SessionService(sessionRepo, sessionClient, cacheService);
        this.driverService = new DriverService(driverRepo, driverClient, meetingService, cacheService);
        this.weatherService = new WeatherService(weatherClient, weatherRepo, sessionService, meetingService);
        this.lapService = new LapService(lapRepo, lapClient, meetingService, sessionService, driverService, cacheService);
        this.sessionResultService =
                new SessionResultService(meetingService, sessionService, sessionResultClient, sessionResultRepo, cacheService);

        this.sessionReportService = new SessionReportService(
                meetingService,
                sessionService,
                sessionResultService,
                lapService,
                weatherService,
                driverService
        );
    }

    private DSLContext dslContext() {
        if (this.dslContext == null) {
            this.dslContext = JooqConfig.createContext();
        }
        return this.dslContext;
    }

    public static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public ICacheService cacheService() {
        return cacheService;
    }

    public DriverService driverService() {
        return driverService;
    }

    public IMeetingService meetingService() {
        return meetingService;
    }

    public ISessionService sessionService() {
        return sessionService;
    }

    public IWeatherService weatherService() {
        return weatherService;
    }

    public ILapService lapService() {
        return lapService;
    }

    public ISessionResultService sessionResultService() {
        return sessionResultService;
    }

    public SessionReportService sessionReportService() {
        return sessionReportService;
    }

    public ProgressBar progressBar() {
        return new ProgressBarBuilder()
                .setTaskName("Generating session report")
                .setInitialMax(9)
                .setStyle(ProgressBarStyle.ASCII)
                .setMaxRenderedLength(100)
                .setUpdateIntervalMillis(100)
                .build();
    }

    public ObjectMapper objectMapper() {
        return objectMapper;
    }
}
