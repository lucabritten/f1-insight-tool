package htwsaar.nordpol.exception;

public class LapsNotFoundException extends RuntimeException {
    public LapsNotFoundException(int sessionkey, int driverNumber) {
        super("Laps not found with given parameters: " + sessionkey + " - " + driverNumber);
    }
}
