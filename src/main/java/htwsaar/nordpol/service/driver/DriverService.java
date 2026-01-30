package htwsaar.nordpol.service.driver;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.exception.SessionNotFoundException;
import htwsaar.nordpol.repository.driver.IDriverRepo;
import htwsaar.nordpol.service.ICacheService;
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

    private final IDriverRepo driverRepo;
    private final DriverClient driverClient;
    private final MeetingService meetingService;
    private final ICacheService cacheService;


    public DriverService(IDriverRepo driverRepo, DriverClient driverClient, MeetingService meetingService, ICacheService cacheService) {

        if (driverRepo == null) {
            throw new IllegalArgumentException("driverRepo must not be null.");
        }
        if (driverClient == null) {
            throw new IllegalArgumentException("driverClient must not be null.");
        }
        if (meetingService == null) {
            throw new IllegalArgumentException("meetingService must not be null.");
        }
        if(cacheService == null) {
            throw new IllegalArgumentException("cacheService must not be null");
        }

        this.driverRepo = driverRepo;
        this.driverClient = driverClient;
        this.meetingService = meetingService;
        this.cacheService = cacheService;
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
    @Override
    public Driver getDriverByNameAndYear(String firstName, String lastName, int year) {
        validateInputYear(year);

        int meetingKey = getMeetingKeyForYear(year);

        DriverDto dto = cacheService.getOrFetchOptional(
                ()-> driverRepo.getDriverByFullNameForYear(firstName, lastName, year),
                () -> driverClient.getDriverByName(firstName, lastName, meetingKey),
                apiDto -> driverRepo.saveOrUpdateDriverForYear(apiDto, year, meetingKey),
                () -> new DriverNotFoundException(firstName, lastName, year)
        );
        return Mapper.toDriver(dto);
    }

    @Override
    public Driver getDriverByNumberAndYear(int number, int year){
        validateInputYear(year);
        int meetingKey = getMeetingKeyForYear(year);

        DriverDto dto = cacheService.getOrFetchOptional(
                ()-> driverRepo.getDriverByStartNumberForYear(number, year),
                () -> driverClient.getDriverByNumberAndMeetingKey(number, meetingKey),
                apiDto -> driverRepo.saveOrUpdateDriverForYear(apiDto, year, meetingKey),
                () -> new DriverNotFoundException(number, year)
        );
        return Mapper.toDriver(dto);
    }

    @Override
    public Driver getDriverByNumberAndMeetingKey(int number, int year, int meetingKey) {
        validateInputYear(year);

        DriverDto dto = cacheService.getOrFetchOptional(
                ()-> driverRepo.getDriverByStartNumberForYear(number, year),
                () -> driverClient.getDriverByNumberAndMeetingKey(number, meetingKey),
                apiDto -> driverRepo.saveOrUpdateDriverForYear(apiDto, year, meetingKey),
                () -> new DriverNotFoundException(number, year)
        );
        return Mapper.toDriver(dto);
    }

    @Override
    public Driver getDriverByNumberWithFallback(int number, int year, int meetingKey) {
        try {
            return getDriverByNumberAndMeetingKey(number, year, meetingKey);
        } catch (DriverNotFoundException ex) {
            Optional<DriverDto> dtoFromApi = driverClient.getDriverByNumber(number);
            if (dtoFromApi.isPresent()) {
                DriverDto driverDto = dtoFromApi.get();
                driverRepo.saveOrUpdateDriverForYear(driverDto, year, meetingKey);
                return Mapper.toDriver(driverDto);
            }
            List<Meeting> meetings = meetingService.getMeetingsForSessionReport(year);
            for (Meeting meeting : meetings) {
                Optional<DriverDto> dtoFromOtherMeeting =
                        driverClient.getDriverByNumberAndMeetingKey(number, meeting.meetingKey());
                if (dtoFromOtherMeeting.isPresent()) {
                    DriverDto driverDto = dtoFromOtherMeeting.get();
                    driverRepo.saveOrUpdateDriverForYear(driverDto, year, meeting.meetingKey());
                    return Mapper.toDriver(driverDto);
                }
            }
            throw ex;
        }
    }

    @Override
    public void preloadMissingDriversForMeeting(int year, int meetingKey, List<Integer> driverNumbers) {
        validateInputYear(year);
        for (Integer driverNumber : driverNumbers) {
            if (driverNumber == null) {
                continue;
            }
            if (!driverRepo.hasNamedDriverNumberForYear(driverNumber, year)) {
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
