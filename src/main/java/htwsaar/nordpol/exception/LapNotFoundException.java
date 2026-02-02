package htwsaar.nordpol.exception;

public class LapNotFoundException extends DataNotFoundException {
    public LapNotFoundException(int sessionKey, int driverNumber) {
        super("Laps not found with given parameters sessionKey: " + sessionKey + ", driverNumber: " + driverNumber);
    }

    public LapNotFoundException(int sessionKey) {
        super("Laps not found with given parameters sessionKey: " + sessionKey);
    }
}
