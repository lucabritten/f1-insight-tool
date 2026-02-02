package htwsaar.nordpol.cli;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.config.ApplicationContext;

import htwsaar.nordpol.util.formatting.CliFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Year;
import java.util.concurrent.Callable;

@Command(name = "driver",
        description = {
        "Print season-specific driver infos by entering year, first- and lastname of a driver",
                "",
                "Examples:",
                "driver -fn Max -ln Verstappen -y 2024",
                "driver --first-name Max --last-name Verstappen --year 2024"
        },
        mixinStandardHelpOptions = true
)
public class DriverCommand implements Callable<Integer> {

    @Option(names = {"--first-name",
            "-fn"},
            description = "The drivers first name (e.g Lando, Max, ...)",
            required = true
    )
    private String firstName;

    @Option(names = {
            "--last-name",
            "-ln"},
            description = "The drivers last name (e.g. Norris, Verstappen, ...)",
            required = true
    )
    private String lastName;

    @Option(names = {
            "--year",
            "-y"},
            description = "The year the data is related too (default: current-year)"
    )
    private int year = Year.now().getValue();

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
            String output = CliFormatter.formatDriver(driver);
            System.out.println(output);
            return 0;
        } catch (DataNotFoundException e) {
            System.err.println("Requested data not found: " + e.getMessage());
            System.err.println("Use --help for usage information.");
            return 2;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}
