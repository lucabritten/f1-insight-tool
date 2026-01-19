package htwsaar.nordpol.util;

import htwsaar.nordpol.api.dto.DriverApiDto;
import htwsaar.nordpol.domain.Driver;

import java.util.Map;

public class Mapper {

    private Mapper(){

    }

    public static Driver toDriver(DriverApiDto dto){

        return new Driver(
                dto.first_name(),
                dto.last_name(),
                dto.driver_number(),
                dto.country_code()
        );
    }
}
