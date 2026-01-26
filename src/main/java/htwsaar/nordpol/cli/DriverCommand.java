package htwsaar.nordpol.cli;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.config.ApplicationContext;

import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "driver-info",
        description = "Print driver infos",
        mixinStandardHelpOptions = true
)
public class DriverCommand implements Callable<Integer> {

    @Option(names = {"--first-name",
            "-fn"},
            description = "The drivers first name",
            required = true
    )
    private String firstName;

    @Option(names = {
            "--last-name",
            "-ln"},
            description = "The drivers last name",
            required = true
    )
    private String lastName;

    @Option(names = {
            "--year",
            "-y"},
            description = "The year the data is related to. This tool provides data from 2023 onwards.",
            defaultValue = "2024"
    )
    private int year;

    private final DriverService driverService;

    public DriverCommand(DriverService driverService){
        this.driverService = driverService;
    }

    public DriverCommand(){
        this(ApplicationContext.driverService());
    }

    @Override
    public Integer call() {
        try {
            Driver driver = driverService.getDriverByNameAndYear(firstName, lastName, year);
            String output = Formatter.formatDriver(driver);
            System.out.println(output);
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 2;
        }
    }
}
