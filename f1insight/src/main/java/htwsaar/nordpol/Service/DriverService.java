package htwsaar.nordpol.Service;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import htwsaar.nordpol.API.DriverClient;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.Repository.DriverRepo;
import htwsaar.nordpol.util.Mapper;

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

    public DriverService(DriverRepo driverRepo, DriverClient driverClient) {
        if (driverRepo == null) {
            throw new IllegalArgumentException("driverRepo must not be null.");
        }
        if (driverClient == null) {
            throw new IllegalArgumentException("driverClient must not be null.");
        }
        this.driverRepo = driverRepo;
        this.driverClient = driverClient;
    }

    /**
     * Returns a driver by first and last name.
     *
     * <p>The method first checkss the local database. If no entry is found,
     * the OpenF1 API is quiried and the result is cached.</p>
     *
     * @throws IllegalStateException if the driver cannot be found
     */
    public Driver getDriverByName(String firstName, String lastName) {
        Optional<DriverApiDto> dtoFromDB = driverRepo.getDriverByFullname(firstName, lastName);
        if (dtoFromDB.isPresent()) {
            return Mapper.toDriver(dtoFromDB.get());
        }

        Optional<DriverApiDto> dtoFromApi = driverClient.getDriverByName(firstName, lastName);
        if(dtoFromApi.isPresent()){
            DriverApiDto driverApiDto = dtoFromApi.get();
            driverRepo.saveDriver(driverApiDto);
            return Mapper.toDriver(driverApiDto);
        }
        throw new DriverNotFoundException(firstName, lastName);
    }
}
