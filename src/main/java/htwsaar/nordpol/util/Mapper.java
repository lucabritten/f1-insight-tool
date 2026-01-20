package htwsaar.nordpol.util;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.domain.Driver;

public class Mapper {

    private Mapper(){

    }

    public static Driver toDriver(DriverDto dto){

        return new Driver(
                dto.first_name(),
                dto.last_name(),
                dto.driver_number(),
                dto.country_code()
        );
    }
}
