package htwsaar.nordpol.api.dto;

public record WeatherDto(int session_key,
                         int meeting_key,
                         double air_temperature,
                         double humidity,
                         int rainfall,
                         double track_temperature,
                         double wind_direction,
                         double wind_speed) {
}
