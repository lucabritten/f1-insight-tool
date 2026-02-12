package htwsaar.nordpol.util;

import htwsaar.nordpol.domain.*;
import htwsaar.nordpol.dto.*;

public class Mapper {

    private Mapper(){

    }

    public static Driver toDriver(DriverDto dto){

        return new Driver(
                dto.first_name(),
                dto.last_name(),
                dto.driver_number(),
                dto.team_name()
        );
    }
    public static Meeting toMeeting(MeetingDto dto) {
        return new Meeting(
                dto.meeting_key(),
                dto.country_code(),
                dto.country_name(),
                dto.location(),
                dto.meeting_name(),
                dto.year(),
                dto.country_flag()
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

    public static SessionResult toSessionResult(SessionResultDto dto, String name) {
        // Normalize null lists from API/DB into empty lists to avoid NPEs downstream
        var gapToLeader = dto.gap_to_leader() != null ? dto.gap_to_leader() : java.util.List.<String>of();
        var duration = dto.duration() != null ? dto.duration() : java.util.List.<Double>of();

        int position = dto.position() != null ? dto.position() : 0;

        return new SessionResult(
                dto.driver_number(),
                name,
                position,
                gapToLeader,
                duration,
                dto.dnf(),
                dto.dsq(),
                dto.dns()
        );
    }

}
