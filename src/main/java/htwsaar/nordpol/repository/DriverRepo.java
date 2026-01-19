package htwsaar.nordpol.repository;
import htwsaar.nordpol.api.dto.DriverApiDto;

import java.util.Optional;

public interface DriverRepo {

    void saveOrUpdateDriverForSeason(DriverApiDto driverApiDto, int season);
    Optional<DriverApiDto> getDriverByFullNameForSeason(String fistName, String lastName, int season);
    Optional<DriverApiDto> getDriverByStartNumberForSeason(int startNumber, int season);
    // DriverApiDto getDriverByNumber(int driverNumber);

}
