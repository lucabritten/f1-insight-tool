package htwsaar.nordpol.cli;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.service.DriverService;
import htwsaar.nordpol.config.ApplicationContext;

import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "driver-info",
        description = "Print driver infos",
        mixinStandardHelpOptions = true
)
public class DriverCommand implements Runnable {

    @Option(names = {"--firstName",
            "-fn"},
            description = "The drivers first name",
            required = true
    )
    private String firstName;

    @Option(names = {
            "--lastName",
            "-ln"},
            description = "The drivers last name",
            required = true
    )
    private String lastName;

    @Option(names = {
            "--season",
            "-s"},
            description = "The season the data is related to. This tool provides data from 2023 onwards.",
            defaultValue = "2024"
    )
    private int season;

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
            Driver driver = driverService.getDriverByNameAndSeason(firstName, lastName, season);
            String output = Formatter.formatDriver(driver);
            System.out.println(output);
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(
                    new CommandLine(this),
                    e.getMessage()
            );
        }
    }
}
