package htwsaar.nordpol.exception;

public class SessionResultNotFoundException extends RuntimeException {
    public SessionResultNotFoundException(int sessionKey) {
        super("Session result not found: " + sessionKey);
    }
}
