package htwsaar.nordpol.exception;

public class DriverNotFoundException extends DataNotFoundException{
    public DriverNotFoundException(String firstName, String lastName, int year){
        super("Driver not found: " + firstName + " " + lastName + " for year " + year);
    }

    public DriverNotFoundException(int number, int year) {
        super("Driver with number " + number + " not found for year " + year);
    }
}
