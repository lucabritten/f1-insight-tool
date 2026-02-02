package htwsaar.nordpol.exception;

public class SessionResultNotFoundException extends DataNotFoundException {
    public SessionResultNotFoundException(int sessionKey) {
        super("Session result not found: " + sessionKey);
    }
}
