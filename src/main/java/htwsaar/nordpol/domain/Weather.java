package htwsaar.nordpol.domain;

public record Weather(int sessionKey,
                      int meetingKey,
                      double avgAirTemperature,
                      double avgHumidity,
                      boolean isRainfall,
                      double avgTrackTemperature,
                      double avgWindDirection,
                      double avgWindSpeed) {
}
