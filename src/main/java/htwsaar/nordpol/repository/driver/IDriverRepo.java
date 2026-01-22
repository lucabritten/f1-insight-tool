package htwsaar.nordpol.repository.driver;
import htwsaar.nordpol.api.dto.DriverDto;

import java.util.Optional;

public interface IDriverRepo {

    void saveOrUpdateDriverForYear(DriverDto driverDto, int year);
    Optional<DriverDto> getDriverByFullNameForYear(String fistName, String lastName, int year);
    Optional<DriverDto> getDriverByStartNumberForYear(int startNumber, int year);
    // DriverApiDto getDriverByNumber(int driverNumber);

}
