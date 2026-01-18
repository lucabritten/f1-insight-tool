package htwsaar.nordpol.CLI;

import htwsaar.nordpol.Service.DriverService;
import htwsaar.nordpol.config.ApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "Driver-Info")

public class DriverCommand implements Runnable {

    @Option(names = {"--firstName",
            "-fn"},
            //description
            required = true
    )
    private String firstName;

    @Option(names = {"--lastName",
            "-ln"},
            //description
            required = true
    )
    private String lastName;

    private final DriverService driverService = ApplicationContext.driverService();

    @Override
    public void run() {
        System.out.println(driverService.getDriverByName(firstName, lastName));
    }
}
