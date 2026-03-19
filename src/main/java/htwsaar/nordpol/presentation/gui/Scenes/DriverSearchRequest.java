package htwsaar.nordpol.presentation.gui.Scenes;

public class DriverSearchRequest {

    private final String firstName;
    private final String lastName;
    private final String number;
    private final String year;

    public DriverSearchRequest(String firstName, String lastName, String number, String year) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.number = number;
        this.year = year;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getNumber() {
        return this.number;
    }

    public String getYear() {
        return this.year;
    }

}
