package htwsaar.nordpol.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record LapDto(int driver_number,
                     int session_key,
                     int lap_number,
                     double duration_sector_1,
                     double duration_sector_2,
                     double duration_sector_3,
                     double lap_duration,
                     boolean is_pit_out_lap) {
}
