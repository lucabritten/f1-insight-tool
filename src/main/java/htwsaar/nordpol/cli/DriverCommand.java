package htwsaar.nordpol.cli;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.config.ApplicationContext;

import htwsaar.nordpol.service.driver.IDriverService;
import htwsaar.nordpol.util.formatting.CliFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DriverCommand.class);

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

    private final IDriverService driverService;

    public DriverCommand(IDriverService driverService){
        this.driverService = driverService;
    }

    public DriverCommand(){
        this(ApplicationContext.getInstance().driverService());
    }

    @Override
    public Integer call() {
        try {
            Driver driver = driverService.getDriverByNameAndYear(firstName, lastName, year);
            String output = CliFormatter.formatDriver(driver);
            logger.info(output);
            return 0;
        } catch (DataNotFoundException e) {
            logger.error("Requested data not found: {}", e.getMessage());
            logger.error("Use --help for usage information.");
            return 2;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return 1;
        }
    }
}
