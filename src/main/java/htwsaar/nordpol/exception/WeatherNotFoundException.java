package htwsaar.nordpol.exception;

public class WeatherNotFoundException extends RuntimeException {
    public WeatherNotFoundException(int meetingKey, int sessionKey) {
        super("No weather-data available for meetingKey: " + meetingKey + " and sessionKey: " + sessionKey);
    }
}
