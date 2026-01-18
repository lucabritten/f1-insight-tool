package htwsaar.nordpol.CLI;

import htwsaar.nordpol.Domain.Driver;
import htwsaar.nordpol.Service.DriverService;
import htwsaar.nordpol.config.ApplicationContext;

import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "driver-info",
        description = "Print driver infos",
        mixinStandardHelpOptions = true
)
public class DriverCommand implements Runnable {

    @Option(names = {"--firstName",
            "-fn"},
            //description
            required = true
    )
    private String firstName;

    @Option(names = {
            "--lastName",
            "-ln"},
            //description
            required = true
    )
    private String lastName;

    private final DriverService driverService;

    public DriverCommand(DriverService driverService){
        this.driverService = driverService;
    }

    public DriverCommand(){
        this(ApplicationContext.driverService());
    }

    @Override
    public void run() {
        try {
            Driver driver = driverService.getDriverByName(firstName, lastName);
            String output = Formatter.formatDriver(driver);
            System.out.println(output);
        } catch (IllegalStateException e){
            System.out.println("Driver not found.");
        }


    }
}
