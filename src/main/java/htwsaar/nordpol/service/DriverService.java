package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.DriverClient;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.repository.DriverRepo;
import htwsaar.nordpol.util.Mapper;

import java.util.HashMap;
import java.util.Map;
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
public class DriverService {

    private final DriverRepo driverRepo;
    private final DriverClient driverClient;
    private final Map<Integer, Integer> meetingSeasonMap;

    public DriverService(DriverRepo driverRepo, DriverClient driverClient) {
        if (driverRepo == null) {
            throw new IllegalArgumentException("driverRepo must not be null.");
        }
        if (driverClient == null) {
            throw new IllegalArgumentException("driverClient must not be null.");
        }
        this.driverRepo = driverRepo;
        this.driverClient = driverClient;
        meetingSeasonMap = new HashMap<>(Map.of(
                2023, 1143,
                2024, 1231,
                2025, 1250)
        );
    }

    /**
     * Returns a driver by first and last name.
     *
     * <p>The method first checks the local database. If no entry is found,
     * the OpenF1 API is queried and the result is cached.</p>
     *
     * @throws DriverNotFoundException if the driver cannot be found
     * @throws IllegalArgumentException if season is not provided by the api
     */
    public Driver getDriverByNameAndSeason(String firstName, String lastName, int season) {
        Optional<DriverDto> dtoFromDB = driverRepo.getDriverByFullNameForSeason(firstName, lastName, season);
        if (dtoFromDB.isPresent()) {
            return Mapper.toDriver(dtoFromDB.get());
        }

        int seasonalMeetingKey = Optional.ofNullable(meetingSeasonMap.get(season))
                .orElseThrow(() -> new IllegalArgumentException("No data for season: " + season));
        Optional<DriverDto> dtoFromApi = driverClient.getDriverByName(firstName, lastName, seasonalMeetingKey);
        if(dtoFromApi.isPresent()){
            DriverDto driverDto = dtoFromApi.get();
            driverRepo.saveOrUpdateDriverForSeason(driverDto, season);
            return Mapper.toDriver(driverDto);
        }
        throw new DriverNotFoundException(firstName, lastName, season);
    }
}
