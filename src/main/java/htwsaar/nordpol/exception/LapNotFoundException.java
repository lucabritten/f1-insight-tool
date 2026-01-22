package htwsaar.nordpol.exception;

public class LapNotFoundException extends RuntimeException {
    public LapNotFoundException(int sessionkey, int driverNumber) {
        super("Laps not found with given parameters: " + sessionkey + " - " + driverNumber);
    }
}
