package htwsaar.nordpol.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(int meetingKey, String sessionType){
        super("Session not found: " + meetingKey + " " + sessionType);
    }
}
