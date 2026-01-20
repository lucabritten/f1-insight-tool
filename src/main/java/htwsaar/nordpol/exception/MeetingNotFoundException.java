package htwsaar.nordpol.exception;

public class MeetingNotFoundException extends RuntimeException {
    public MeetingNotFoundException(int season, String location){
        super("Meeting not found: " + season + " " + location);
    }
}

