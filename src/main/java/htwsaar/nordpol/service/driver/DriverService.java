package htwsaar.nordpol.service.driver;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.repository.driver.IDriverRepo;
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
public class DriverService implements IDriverService {

    private static final int MIN_YEAR = 2023;

    private final IDriverRepo IDriverRepo;
    private final DriverClient driverClient;

    public DriverService(IDriverRepo IDriverRepo, DriverClient driverClient) {
        if (IDriverRepo == null) {
            throw new IllegalArgumentException("driverRepo must not be null.");
        }
        if (driverClient == null) {
            throw new IllegalArgumentException("driverClient must not be null.");
        }

        this.IDriverRepo = IDriverRepo;
        this.driverClient = driverClient;
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

        Optional<DriverDto> dtoFromApi = driverClient.getDriverByName(firstName, lastName, year);
        if(dtoFromApi.isPresent()){
            DriverDto driverDto = dtoFromApi.get();
            IDriverRepo.saveOrUpdateDriverForYear(driverDto, year);
            return Mapper.toDriver(driverDto);
        }
        throw new DriverNotFoundException(firstName, lastName, year);
    }

    public Driver getDriverByNumberAndYear(int number, int year){
        validateInputYear(year);

        Optional<DriverDto> dtoFromDB = IDriverRepo.getDriverByStartNumberForYear(number, year);
        if(dtoFromDB.isPresent())
            return Mapper.toDriver(dtoFromDB.get());

        Optional<DriverDto> dtoFromApi = driverClient.getDriverByNumberAndMeetingKey(number, year);
        if(dtoFromApi.isPresent()) {
            DriverDto driverDto = dtoFromApi.get();
            IDriverRepo.saveOrUpdateDriverForYear(driverDto, year);
            return Mapper.toDriver(driverDto);
        }
        throw new DriverNotFoundException(number, year);
    }

    private void validateInputYear(int year) {
        if (year < MIN_YEAR) {
            throw new IllegalArgumentException("Only data from " + MIN_YEAR + " onwards is available.");
        }
    }
}
