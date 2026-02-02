package htwsaar.nordpol.exception;

public class SessionNotFoundException extends DataNotFoundException {
    public SessionNotFoundException(int meetingKey, String sessionName){
        super("Session not found: " + meetingKey + " " + sessionName);
    }
}
