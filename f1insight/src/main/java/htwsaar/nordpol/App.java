package htwsaar.nordpol;

import htwsaar.nordpol.Service.DriverService;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.util.Formatter;

public class App {
    public static void main(String[] args) {
        DriverService service = ApplicationContext.driverService();

        System.out.println(Formatter.formatDriver(service.getDriverByName("Lando", "Norris")));
    }
}
