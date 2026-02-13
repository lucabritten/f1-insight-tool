package htwsaar.nordpol.exception;

public class MeetingNotFoundException extends DataNotFoundException {
    public MeetingNotFoundException(int year, String location){
        super("Meeting not found for year " + year + " and location " + location);
    }
}

