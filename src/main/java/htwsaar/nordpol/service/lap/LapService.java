package htwsaar.nordpol.service.lap;

import htwsaar.nordpol.api.lap.ILapClient;
import htwsaar.nordpol.dto.LapDto;
import htwsaar.nordpol.presentation.view.FastestLapsWithContext;
import htwsaar.nordpol.presentation.view.LapsWithContext;
import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.repository.lap.ILapRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.driver.IDriverService;
import htwsaar.nordpol.service.meeting.IMeetingService;
import htwsaar.nordpol.service.session.ISessionService;
import htwsaar.nordpol.util.Mapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class LapService implements ILapService {

    private final ILapRepo lapRepo;
    private final ILapClient lapClient;

    private final IMeetingService meetingService;
    private final ISessionService sessionService;
    private final IDriverService driverService;
    private final ICacheService cacheService;

    public LapService(ILapRepo lapRepo, ILapClient lapsClient, IMeetingService meetingService, ISessionService sessionService, IDriverService driverService, ICacheService cacheService) {
        validateLapConstructor(lapRepo, lapsClient, meetingService, sessionService, driverService, cacheService);
        this.lapRepo = lapRepo;
        this.lapClient = lapsClient;
        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.driverService = driverService;
        this.cacheService = cacheService;
    }

    private void validateLapConstructor(ILapRepo lapRepo, ILapClient lapsClient, IMeetingService meetingService, ISessionService sessionService, IDriverService driverService, ICacheService cacheService){
        requireNonNull(lapsClient, "lapClient");
        requireNonNull(lapRepo, "lapRepo");
        requireNonNull(meetingService, "meetingService");
        requireNonNull(sessionService, "sessionService");
        requireNonNull(driverService, "driverService");
        requireNonNull(cacheService, "cacheService");
    }

    @Override
    public LapsWithContext getLapsByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber){
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        Driver driver = driverService.getDriverByNumberAndYear(driverNumber, year);

        List<Lap> laps = getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        return new LapsWithContext(meeting.meetingName(),
                driver.firstName() + " " + driver.lastName(),
                session.sessionName(),
                laps
        );
    }

    /**
     * Retrieves laps from cache or fetches them from the OpenF1 API and persists them.
     */
    @Override
    public List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {

        List<LapDto> dtoList = cacheService.getOrFetchList(
                () -> lapRepo.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber),
                () -> lapClient.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber),
                lapRepo::saveAll,
                () -> new LapNotFoundException(sessionKey, driverNumber)
        );
        return filterValidLaps(
                dtoList
                .stream()
                .filter(lap -> lap.lap_duration() > 0)
                .map(Mapper::toLap)
                .toList(),
                true);
    }

    @Override
    public FastestLapsWithContext getFastestLapByLocationYearAndSessionName(String location, int year, SessionName sessionName, int count) {
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        List<Lap> fastestLaps = getFastestLapsBySessionKey(sessionKey, count);

        List<Driver> drivers = new ArrayList<>();

        fastestLaps.forEach(lap -> drivers.add(driverService.getDriverByNumberAndYear(lap.driverNumber(), year)));



        return new FastestLapsWithContext(meeting.meetingName(),
                session.sessionName(),
                drivers,
                fastestLaps
        );
    }

    private List<Lap> getFastestLapsBySessionKey(int sessionKey, int count) {

        List<LapDto> dtoList = cacheService.getOrFetchList(
                () -> lapRepo.getFastestLapsBySessionKey(sessionKey, count),
                () -> lapClient.getLapsBySessionKey(sessionKey),
                lapRepo::saveAll,
                () -> new LapNotFoundException(sessionKey)
        );
        return filterValidLaps(
                dtoList
                .stream()
                .map(Mapper::toLap)
                .toList(),
                true);
    }

    @Override
    public FastestLapsWithContext getFastestLapByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber, int count){
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        List<Lap> fastestLaps = getFastestLapBySessionKeyAndDriverNumber(sessionKey, driverNumber, count);

        List<Driver> drivers = fastestLaps.stream()
                .map(lap -> driverService.getDriverByNumberAndYear(lap.driverNumber(), year))
                .toList();

        return new FastestLapsWithContext(meeting.meetingName(),
                session.sessionName(),
                drivers,
                fastestLaps
        );
    }

    private List<Lap> getFastestLapBySessionKeyAndDriverNumber(int sessionKey, int driverNumber, int count) {
        List<Lap> laps = getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        return filterFastestLaps(laps, count);
    }

    private List<Lap> filterFastestLaps(List<Lap> laps, int count) {
        if (laps == null || laps.isEmpty() || count <= 0) {
            return List.of();
        }

        return filterValidLaps(
                laps.stream()
                .sorted(Comparator.comparingDouble(Lap::lapDuration))
                .limit(count)
                .toList(),
                false);
    }

    private List<Lap> filterValidLaps(List<Lap> laps, boolean includePitOutLaps) {
        if (laps == null)
            return List.of();

        return laps.stream()
                .filter(lap -> lap.lapDuration() > 0)
                .filter(lap -> includePitOutLaps || !lap.isPitOutLap())
                .toList();
    }
}