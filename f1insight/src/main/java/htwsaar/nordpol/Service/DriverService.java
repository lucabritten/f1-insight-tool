package htwsaar.nordpol.Service;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import htwsaar.nordpol.API.DriverClient;
import htwsaar.nordpol.Repository.DriverRepo;

import java.util.Optional;

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

    public Optional<DriverApiDto> getDriverByName(String firstName, String lastName) {
        DriverApiDto fromDB = driverRepo.getDriverByFullname(firstName, lastName);
        if (fromDB != null) {
            return Optional.of(fromDB);
        }

        Optional<DriverApiDto> fromApi = driverClient.getDriverByName(firstName, lastName);
        fromApi.ifPresent(driverRepo::saveDriver);
        return fromApi;
    }
}
