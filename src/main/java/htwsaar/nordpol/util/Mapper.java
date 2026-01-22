package htwsaar.nordpol.util;

import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.dto.LapDto;
import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.api.dto.SessionDto;
import htwsaar.nordpol.api.dto.WeatherDto;
import htwsaar.nordpol.domain.*;

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
                SessionName.fromString(dto.session_name()),
                dto.session_name()
        );
    }

    public static Weather toWeather(WeatherDto dto){
        boolean isRainfall = dto.rainfall() == 1;

        return new Weather(
                dto.session_key(),
                dto.meeting_key(),
                dto.air_temperature(),
                dto.humidity(),
                isRainfall,
                dto.track_temperature(),
                dto.wind_direction(),
                dto.wind_speed()
        );
    }

    public static Lap toLap(LapDto dto) {
        return new Lap(
                dto.driver_number(),
                dto.lap_number(),
                dto.session_key(),
                dto.duration_sector_1(),
                dto.duration_sector_2(),
                dto.duration_sector_3(),
                dto.lap_duration(),
                dto.is_pit_out_lap()
        );
    }

}
