package htwsaar.nordpol.exception;

public class SessionNotFoundException extends DataNotFoundException {
    public SessionNotFoundException(int meetingKey, String sessionName){
        super("Session not found for meetingKey " + meetingKey + " and sessionName" + sessionName);
    }
}
