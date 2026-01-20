package htwsaar.nordpol.repository.driver;
import htwsaar.nordpol.api.dto.DriverDto;

import java.util.Optional;

public interface DriverRepo {

    void saveOrUpdateDriverForSeason(DriverDto driverDto, int season);
    Optional<DriverDto> getDriverByFullNameForSeason(String fistName, String lastName, int season);
    Optional<DriverDto> getDriverByStartNumberForSeason(int startNumber, int season);
    // DriverApiDto getDriverByNumber(int driverNumber);

}
