package htwsaar.nordpol.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record LapDto(int driver_number,
                     int session_key,
                     int lap_number,
                     Double duration_sector_1,
                     Double duration_sector_2,
                     Double duration_sector_3,
                     Double lap_duration,
                     boolean is_pit_out_lap) {
}
