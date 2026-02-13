package htwsaar.nordpol.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record WeatherDto(int session_key,
                         int meeting_key,
                         double air_temperature,
                         double humidity,
                         int rainfall,
                         double track_temperature,
                         double wind_direction,
                         double wind_speed) {
}
