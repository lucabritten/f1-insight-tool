package htwsaar.nordpol.exception;

public class DriverNotFoundException extends RuntimeException{
    public DriverNotFoundException(String firstName, String lastName){
        super("Driver not found: " + firstName + " " + lastName);
    }
}
