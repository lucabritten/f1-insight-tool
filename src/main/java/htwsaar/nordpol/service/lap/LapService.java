package htwsaar.nordpol.service.lap;

import htwsaar.nordpol.api.lap.ILapClient;
import htwsaar.nordpol.api.dto.LapDto;
import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.cli.view.FastestLapEntry;
import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.repository.lap.ILapRepo;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.util.Mapper;

import java.util.ArrayList;
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
        return new LapsWithContext(meeting.meetingName(),
                driver.firstName() + " " + driver.lastName(),
                session.sessionName(),
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
    public FastestLapsWithContext getFastestLapByLocationYearAndSessionName(String location, int year, SessionName sessionName, int count) {
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        List<Lap> fastestLaps = getFastestLapsBySessionKey(sessionKey, count);

        List<Driver> drivers = new ArrayList<>();

        fastestLaps.forEach((lap) -> drivers.add(driverService.getDriverByNumberAndYear(lap.driverNumber(), year)));

        return new FastestLapsWithContext(meeting.meetingName(),
                session.sessionName(),
                drivers,
                fastestLaps
        );
    }

    private List<Lap> getFastestLapsBySessionKey(int sessionKey, int count) {
        List<LapDto> fastestFromDb = lapRepo.getFastestLapsBySessionKey(sessionKey, count);
        if (!fastestFromDb.isEmpty()) {
            return fastestFromDb.stream()
                    .map(Mapper::toLap)
                    .toList();
        }

        List<LapDto> apiLaps = lapClient.getLapsBySessionKey(sessionKey);
        if (apiLaps.isEmpty()) {
            throw new LapNotFoundException(sessionKey, -1);
        }

        lapRepo.saveAll(apiLaps);
        List<Lap> laps = apiLaps.stream()
                .map(Mapper::toLap)
                .toList();

        return filterFastestLaps(laps, count);
    }

    //Übergangslösung mit der driver list
    @Override
    public FastestLapsWithContext getFastestLapByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber, int count){
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        List<Lap> fastestLaps = getFastestLapBySessionKeyAndDriverNumber(sessionKey, driverNumber, count);

        Driver driver = driverService.getDriverByNumberAndYear(driverNumber, year);

        List<Driver> drivers = new ArrayList<>();
        fastestLaps.forEach(lap -> drivers.add(driver));

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

        // NOTE: Stream#toList() returns an unmodifiable List (Java 16+).
        // Using limit(count) avoids mutating the list (e.g. removeLast()), which can throw
        // UnsupportedOperationException and make it look like the loop "stops".
        return laps.stream()
                .filter(l -> l.lapDuration() > 0)
                .filter(l -> !l.isPitOutLap())
                .sorted(Comparator.comparingDouble(Lap::lapDuration))
                .limit(count)
                .toList();
    }


//    public FastestLapsWithContext getFastestLapsByLocationYearAndSessionName(
//            String location,
//            int year,
//            SessionName sessionName,
//            int topN
//    ) {
//        if (location == null || location.isBlank()) {
//            throw new IllegalArgumentException("location must not be null or blank.");
//        }
//        if (sessionName == null) {
//            throw new IllegalArgumentException("sessionName must not be null.");
//        }
//        if (topN <= 0) {
//            throw new IllegalArgumentException("topN must be positive.");
//        }
//
//        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
//        int meetingKey = meeting.meetingKey();
//
//        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
//        int sessionKey = session.sessionKey();
//
//
//        List<LapDto> fastestFromDb = lapRepo.getFastestLapsBySessionKey(sessionKey, topN);
//        if (!fastestFromDb.isEmpty()) {
//            List<Lap> laps = fastestFromDb.stream().map(Mapper::toLap).toList();
//            List<FastestLapEntry> entries = toFastestLapEntries(laps, year);
//            return new FastestLapsWithContext(meeting.meetingName(), session.sessionName(), entries);
//        }
//
//
//        List<LapDto> apiLaps = lapClient.getLapsBySessionKey(sessionKey);
//        if (apiLaps.isEmpty()) {
//            throw new LapNotFoundException(sessionKey, -1);
//        }
//
//        lapRepo.saveAll(apiLaps);
//
//        List<Lap> laps = apiLaps.stream()
//                .map(Mapper::toLap)
//                .toList();
//
//        List<Lap> topLaps = filterTopNLaps(laps, topN);
//        List<FastestLapEntry> entries = toFastestLapEntries(topLaps, year);
//
//        return new FastestLapsWithContext(meeting.meetingName(), session.sessionName(), entries);
//    }

    private List<Lap> filterTopNLaps(List<Lap> laps, int topN) {
        if (laps == null || laps.isEmpty()) {
            return List.of();
        }

        return laps.stream()
                .filter(l -> l.lapDuration() > 0)
                .filter(l -> !l.isPitOutLap())
                .sorted(Comparator.comparingDouble(Lap::lapDuration))
                .limit(topN)
                .toList();
    }

    private List<FastestLapEntry> toFastestLapEntries(List<Lap> laps, int year) {
        return laps.stream()
                .map(l -> {
                    Driver driver = driverService.getDriverByNumberAndYear(l.driverNumber(), year);
                    String driverName = driver.firstName() + " " + driver.lastName();
                    return new FastestLapEntry(
                            driverName,
                            l.lapDuration(),
                            l.lapNumber(),
                            l.driverNumber()
                    );
                })
                .toList();
    }

}