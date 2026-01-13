package htwsaar.nordpol.Repository;
import htwsaar.nordpol.API.DTO.DriverApiDto;

public interface DriverRepo {

    void saveDriver(DriverApiDto driverApiDto);
    DriverApiDto getDriverByFullname(String surname, String lastName);
    // DriverApiDto getDriverByNumber(int driverNumber);

}
