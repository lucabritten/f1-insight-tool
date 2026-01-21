package htwsaar.nordpol.util;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;

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
    public static Session toSession(SessionDto dto){
        return new Session(
                dto.session_key(),
                dto.meeting_key(),
                dto.session_type(),
                dto.session_name()

        );
    }

}
