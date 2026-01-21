package htwsaar.nordpol.api.dto;

public record WeatherDto(int session_key,
                         int meeting_key,
                         double air_temperature,
                         int humidity,
                         int rainfall,
                         double track_temperature,
                         int wind_direction,
                         double wind_speed) {
}
