package htwsaar.nordpol.config;

import htwsaar.nordpol.API.DriverClient;
import htwsaar.nordpol.Repository.DriverRepo;
import htwsaar.nordpol.Repository.JooqDriverRepo;
import htwsaar.nordpol.Service.DriverService;

public class ApplicationContext {

    public static DriverService driverService(){
        DriverRepo driverRepo = new JooqDriverRepo(JooqConfig.createContext());
        DriverClient driverClient = new DriverClient();
        return new DriverService(driverRepo, driverClient);
    }
}
