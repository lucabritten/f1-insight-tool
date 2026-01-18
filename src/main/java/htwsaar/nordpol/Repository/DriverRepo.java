package htwsaar.nordpol.Repository;
import htwsaar.nordpol.API.DTO.DriverApiDto;

import java.util.Optional;

public interface DriverRepo {

    void saveDriver(DriverApiDto driverApiDto);
    Optional<DriverApiDto> getDriverByFullname(String surname, String lastName);
    // DriverApiDto getDriverByNumber(int driverNumber);

}
