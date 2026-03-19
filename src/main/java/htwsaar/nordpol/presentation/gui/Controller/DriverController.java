package htwsaar.nordpol.presentation.gui.Controller;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.presentation.gui.Scenes.DriverSearchRequest;
import htwsaar.nordpol.service.driver.IDriverService;

public class DriverController {

    private final IDriverService driverService;

    public DriverController(IDriverService driverService) {
        this.driverService = driverService;
    }

    public Driver searchDriver(DriverSearchRequest request) {
        String yearText = request.getYear().trim();
        if (yearText.isBlank()) {
            throw new IllegalArgumentException("Year is required.");
        }

        int year = Integer.parseInt(yearText);

        if (!request.getNumber().trim().isBlank()) {
            int number = Integer.parseInt(request.getNumber());
            return driverService.getDriverByNumberAndYear(number, year);
        }

        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();

        if (firstName.isBlank() || lastName.isBlank()) {
            throw new IllegalArgumentException("Provide either driver number and year, or first name, last name, and year.");
        }

        return driverService.getDriverByNameAndYear(firstName, lastName, year);
    }
}
