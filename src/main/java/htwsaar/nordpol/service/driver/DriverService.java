package htwsaar.nordpol.service.driver;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.repository.driver.IDriverRepo;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.util.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for driver-related operations.
 *
 * <p>This service implements a cache-first strategy:
 * it first queries the local database and falls back to the OpenF1 API
 * if the driver is not found locally.</p>
 *
 * <p>Fetched API data is persisted locally to avoid unnecessary
 * future API calls.</p>
 *
 * <p>This class serves as a template for other domain services.</p>
 */
public class DriverService implements IDriverService {

    private static final int MIN_YEAR = 2023;

    private final IDriverRepo IDriverRepo;
    private final DriverClient driverClient;
    private final MeetingService meetingService;

    public DriverService(IDriverRepo IDriverRepo, DriverClient driverClient, MeetingService meetingService) {
        if (IDriverRepo == null) {
            throw new IllegalArgumentException("driverRepo must not be null.");
        }
        if (driverClient == null) {
            throw new IllegalArgumentException("driverClient must not be null.");
        }
        if (meetingService == null) {
            throw new IllegalArgumentException("meetingService must not be null.");
        }

        this.IDriverRepo = IDriverRepo;
        this.driverClient = driverClient;
        this.meetingService = meetingService;
    }

    /**
     * Returns a driver by first and last name.
     *
     * <p>The method first checks the local database. If no entry is found,
     * the OpenF1 API is queried and the result is cached.</p>
     *
     * @throws DriverNotFoundException if the driver cannot be found
     * @throws IllegalArgumentException if year is not provided by the api
     */
    public Driver getDriverByNameAndYear(String firstName, String lastName, int year) {
        validateInputYear(year);

        Optional<DriverDto> dtoFromDB = IDriverRepo.getDriverByFullNameForYear(firstName, lastName, year);
        if (dtoFromDB.isPresent())
            return Mapper.toDriver(dtoFromDB.get());

        int meetingKey = getMeetingKeyForYear(year);

        Optional<DriverDto> dtoFromApi = driverClient.getDriverByName(firstName, lastName, meetingKey);
        if(dtoFromApi.isPresent()){
            DriverDto driverDto = dtoFromApi.get();
            IDriverRepo.saveOrUpdateDriverForYear(driverDto, year, meetingKey);
            return Mapper.toDriver(driverDto);
        }
        throw new DriverNotFoundException(firstName, lastName, year);
    }

    public Driver getDriverByNumberAndYear(int number, int year){
        validateInputYear(year);

        Optional<DriverDto> dtoFromDB = IDriverRepo.getDriverByStartNumberForYear(number, year);
        if(dtoFromDB.isPresent())
            return Mapper.toDriver(dtoFromDB.get());

        int meetingKey = getMeetingKeyForYear(year);

        Optional<DriverDto> dtoFromApi = driverClient.getDriverByNumberAndMeetingKey(number, meetingKey);
        if(dtoFromApi.isPresent()) {
            DriverDto driverDto = dtoFromApi.get();
            IDriverRepo.saveOrUpdateDriverForYear(driverDto, year, meetingKey);
            return Mapper.toDriver(driverDto);
        }
        throw new DriverNotFoundException(number, year);
    }

    public Driver getDriverByNumberAndMeetingKey(int number, int year, int meetingKey) {
        validateInputYear(year);

        Optional<DriverDto> dtoFromDB = IDriverRepo.getDriverByStartNumberForYear(number, year);
        if (dtoFromDB.isPresent())
            return Mapper.toDriver(dtoFromDB.get());

        Optional<DriverDto> dtoFromApi = driverClient.getDriverByNumberAndMeetingKey(number, meetingKey);
        if (dtoFromApi.isPresent()) {
            DriverDto driverDto = dtoFromApi.get();
            IDriverRepo.saveOrUpdateDriverForYear(driverDto, year, meetingKey);
            return Mapper.toDriver(driverDto);
        }
        throw new DriverNotFoundException(number, year);
    }

    public Driver getDriverByNumberWithFallback(int number, int year, int meetingKey) {
        try {
            return getDriverByNumberAndMeetingKey(number, year, meetingKey);
        } catch (DriverNotFoundException ex) {
            Optional<DriverDto> dtoFromApi = driverClient.getDriverByNumber(number);
            if (dtoFromApi.isPresent()) {
                DriverDto driverDto = dtoFromApi.get();
                IDriverRepo.saveOrUpdateDriverForYear(driverDto, year, meetingKey);
                return Mapper.toDriver(driverDto);
            }
            List<Meeting> meetings = meetingService.getMeetingsForSessionReport(year);
            for (Meeting meeting : meetings) {
                Optional<DriverDto> dtoFromOtherMeeting =
                        driverClient.getDriverByNumberAndMeetingKey(number, meeting.meetingKey());
                if (dtoFromOtherMeeting.isPresent()) {
                    DriverDto driverDto = dtoFromOtherMeeting.get();
                    IDriverRepo.saveOrUpdateDriverForYear(driverDto, year, meeting.meetingKey());
                    return Mapper.toDriver(driverDto);
                }
            }
            throw ex;
        }
    }

    public void preloadMissingDriversForMeeting(int year, int meetingKey, List<Integer> driverNumbers) {
        validateInputYear(year);
        for (Integer driverNumber : driverNumbers) {
            if (driverNumber == null) {
                continue;
            }
            if (!IDriverRepo.hasNamedDriverNumberForYear(driverNumber, year)) {
                getDriverByNumberAndMeetingKey(driverNumber, year, meetingKey);
            }
        }
    }

    private void validateInputYear(int year) {
        if (year < MIN_YEAR) {
            throw new IllegalArgumentException("Only data from " + MIN_YEAR + " onwards is available.");
        }
    }

    private int getMeetingKeyForYear(int year) {
        List<Meeting> meeting = meetingService.getMeetingsForSessionReport(year);
        return meeting.getFirst().meetingKey();
    }

}
