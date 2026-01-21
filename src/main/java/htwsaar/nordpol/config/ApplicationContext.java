package htwsaar.nordpol.config;

import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.api.weather.WeatherClient;
import htwsaar.nordpol.repository.driver.IDriverRepo;
import htwsaar.nordpol.repository.driver.JooqDriverRepo;
import htwsaar.nordpol.repository.weather.IWeatherRepo;
import htwsaar.nordpol.repository.weather.JooqWeatherRepo;
import htwsaar.nordpol.service.DriverService;
import htwsaar.nordpol.service.WeatherService;

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
        IDriverRepo IDriverRepo = new JooqDriverRepo(JooqConfig.createContext());
        DriverClient driverClient = new DriverClient();
        return new DriverService(IDriverRepo, driverClient);
    }

    public static WeatherService weatherService() {
        IWeatherRepo weatherRepo = new JooqWeatherRepo(JooqConfig.createContext());
        WeatherClient weatherClient = new WeatherClient();
        return new WeatherService(weatherClient, weatherRepo);
    }
}
