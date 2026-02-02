package htwsaar.nordpol.service.report;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.service.driver.DriverService;

import static java.util.Objects.requireNonNull;


public class DriverResolver {

    private final DriverService driverService;

    public DriverResolver(DriverService driverService) {
        this.driverService = requireNonNull(driverService, "driverService must not be null");
    }

    public Driver resolve(int driverNumber, int year, int meetingKey) {
        try {
            return driverService.getDriverByNumberWithFallback(driverNumber, year, meetingKey);
        } catch (RuntimeException ex) {
            return createUnknownDriver(driverNumber);
        }
    }

    private Driver createUnknownDriver(int driverNumber) {
        return new Driver("Unknown", "Driver", driverNumber, "Unknown");
    }
}
