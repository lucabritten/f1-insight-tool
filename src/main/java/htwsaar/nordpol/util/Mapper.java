package htwsaar.nordpol.util;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Meeting;

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
    public static Meeting toMeeting(MeetingDto dto){
        return new Meeting(
                dto.meeting_key(),
                dto.country_code(),
                dto.country_name(),
                dto.location(),
                dto.year()
        );
    }

}
