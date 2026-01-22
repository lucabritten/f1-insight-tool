package htwsaar.nordpol.exception;

public class DriverNotFoundException extends RuntimeException{
    public DriverNotFoundException(String firstName, String lastName, int year){
        super("Driver not found: " + firstName + " " + lastName + " for year " + year);
    }
}
