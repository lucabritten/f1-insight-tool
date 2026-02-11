package htwsaar.nordpol.repository.driver;
import htwsaar.nordpol.dto.DriverDto;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface IDriverRepo {

    void saveOrUpdateDriverForYear(DriverDto driverDto, int year, int meetingKey);
    Optional<DriverDto> getDriverByFullNameForYear(String firstName, String lastName, int year);
    Optional<DriverDto> getDriverByStartNumberForYear(int startNumber, int year);
    boolean hasNamedDriverNumberForYear(int startNumber, int year);

}
