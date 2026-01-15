package htwsaar.nordpol.Service;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import htwsaar.nordpol.API.DriverClient;
import htwsaar.nordpol.Domain.Driver;
import htwsaar.nordpol.Repository.DriverRepo;
import htwsaar.nordpol.util.Mapper;

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

    public Optional<Driver> getDriverByName(String firstName, String lastName) {
        DriverApiDto dtoFromDB = driverRepo.getDriverByFullname(firstName, lastName);
        if (dtoFromDB != null) {
            return Optional.of(Mapper.toDriver(dtoFromDB));
        }

        Optional<DriverApiDto> dtoFromApi = driverClient.getDriverByName(firstName, lastName);
        dtoFromApi.ifPresent(driverRepo::saveDriver);
        if(dtoFromApi.isPresent()){
            DriverApiDto driverApiDto = dtoFromApi.get();
            driverRepo.saveDriver(driverApiDto);
            return Optional.of(Mapper.toDriver(driverApiDto));
        }
        throw new IllegalStateException("Driver service Error: getting driver by name failed. DtoFromApi is not present.");
    }
}
