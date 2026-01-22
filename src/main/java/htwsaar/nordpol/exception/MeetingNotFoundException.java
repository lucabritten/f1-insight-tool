package htwsaar.nordpol.exception;

public class MeetingNotFoundException extends RuntimeException {
    public MeetingNotFoundException(int year, String location){
        super("Meeting not found: " + year + " " + location);
    }
}

