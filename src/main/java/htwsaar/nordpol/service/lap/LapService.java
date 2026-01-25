package htwsaar.nordpol.service.lap;

import htwsaar.nordpol.api.lap.ILapClient;
import htwsaar.nordpol.api.dto.LapDto;
import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.repository.lap.ILapRepo;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.util.Mapper;

import java.util.Comparator;
import java.util.List;

public class LapService implements ILapService {

    private final ILapRepo lapRepo;
    private final ILapClient lapClient;

    private final MeetingService meetingService;
    private final SessionService sessionService;
    private final DriverService driverService;

    public LapService(ILapRepo lapRepo, ILapClient lapsClient, MeetingService meetingService, SessionService sessionService, DriverService driverService) {
        validateLapConstructor(lapRepo, lapsClient, meetingService, sessionService, driverService);
        this.lapRepo = lapRepo;
        this.lapClient = lapsClient;
        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.driverService = driverService;
    }

    private void validateLapConstructor(ILapRepo lapRepo, ILapClient lapsClient, MeetingService meetingService, SessionService sessionService, DriverService driverService){
        if (lapsClient == null)
            throw new IllegalArgumentException("LapsClient cannot be null.");
        if (lapRepo == null)
            throw new IllegalArgumentException("LapsRepo cannot be null.");
        if(meetingService == null)
            throw new IllegalArgumentException("MeetingService cannot be null.");
        if(sessionService == null)
            throw new IllegalArgumentException("SessionService cannot be null.");
        if(driverService == null)
            throw new IllegalArgumentException("DriverService cannot be null");
    }

    public LapsWithContext getLapsByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber){
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        Driver driver = driverService.getDriverByNumberAndYear(driverNumber, year);

        List<Lap> laps = getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        return new LapsWithContext(meeting.location(),
                driver.firstName() + " " + driver.lastName(),
                session.sessionName().name(),
                laps
        );
    }

    @Override
    public List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        List<LapDto> dtoFromDB = lapRepo.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        if (!dtoFromDB.isEmpty()) {
            return dtoFromDB.stream()
                    .map(Mapper::toLap)
                    .toList();
        }

        List<LapDto> dtoFromApi =
                lapClient.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);

        if (!dtoFromApi.isEmpty()) {
            lapRepo.saveAll(dtoFromApi);
            return dtoFromApi.stream()
                    .map(Mapper::toLap)
                    .toList();
        }
        throw new LapNotFoundException(sessionKey, driverNumber);
    }

    @Override
    public LapsWithContext getFastestLapByLocationYearAndSessionName(String location, int year, SessionName sessionName) {
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        Lap fastestLap = getFastestLapBySessionKey(sessionKey);

        Driver driver = driverService.getDriverByNumberAndYear(fastestLap.driverNumber(), year);

        return new LapsWithContext(meeting.location(),
                driver.firstName() + " " + driver.lastName(),
                session.sessionName().name(),
                List.of(fastestLap)
        );
    }

    private Lap getFastestLapBySessionKey(int sessionKey) {
        List<LapDto> fastestFromDb = lapRepo.getFastestLapsBySessionKey(sessionKey, 1);
        if (!fastestFromDb.isEmpty()) {
            return Mapper.toLap(fastestFromDb.getFirst());
        }

        List<LapDto> apiLaps = lapClient.getLapsBySessionKey(sessionKey);
        if (apiLaps.isEmpty()) {
            throw new LapNotFoundException(sessionKey, -1);
        }

        lapRepo.saveAll(apiLaps);
        List<Lap> laps = apiLaps.stream()
                .map(Mapper::toLap)
                .toList();

        return filterFastestLap(laps);
    }

    @Override
    public LapsWithContext getFastestLapByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber){
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        Lap fastestLap = getFastestLapBySessionKeyAndDriverNumber(sessionKey, driverNumber);

        Driver driver = driverService.getDriverByNumberAndYear(driverNumber, year);

        return new LapsWithContext(meeting.location(),
                driver.firstName() + " " + driver.lastName(),
                session.sessionName().name(),
                List.of(fastestLap)
        );
    }

    private Lap getFastestLapBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        List<Lap> laps = getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        return filterFastestLap(laps);
    }

    private Lap filterFastestLap(List<Lap> laps) {
        int sessionKey = laps.getFirst().sessionKey();
        return laps.stream()
                .filter(l -> l.lapDuration() > 0)
                .filter(l -> !l.isPitOutLap())
                .min(Comparator.comparingDouble(Lap::lapDuration))
                .orElseThrow(() -> new LapNotFoundException(sessionKey));
    }

}
