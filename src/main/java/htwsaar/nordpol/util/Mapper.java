package htwsaar.nordpol.util;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import htwsaar.nordpol.domain.Driver;

public class Mapper {

    public static Driver toDriver(DriverApiDto dto){

        return new Driver(
                dto.first_name(),
                dto.last_name(),
                dto.driver_number(),
                dto.country_code()
        );
    }
}
