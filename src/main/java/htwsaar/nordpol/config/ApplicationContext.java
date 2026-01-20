package htwsaar.nordpol.config;

import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.repository.driver.DriverRepo;
import htwsaar.nordpol.repository.driver.JooqDriverRepo;
import htwsaar.nordpol.service.DriverService;

/**
 * Simple application context responsible for wiring application services.
 *
 * <p>This class acts as a lightweight dependency container and is used
 * to create fully initialized service instances.</p>
 *
 * <p>It centralizes object creation and keeps CLI commands free from
 * infrastructure and configuration logic.</p>
 */
public class ApplicationContext {

    private ApplicationContext(){

    }
    /**
     * Creates and returns a {@link DriverService} instance.
     *
     * <p>The service is composed of a repository backed by a local SQLite
     * database and a client for accessing the OpenF1 API.</p>
     *
     * @return a fully initialized DriverService
     */
    public static DriverService driverService(){
        DriverRepo driverRepo = new JooqDriverRepo(JooqConfig.createContext());
        DriverClient driverClient = new DriverClient();
        return new DriverService(driverRepo, driverClient);
    }
}
