package htwsaar.nordpol.util;

import htwsaar.nordpol.domain.Driver;

public class Formatter {

    private Formatter(){

    }

    public static String formatDriver(Driver driver){
        return """
                ========== DRIVER ==========
                Name         : %s %s
                Number       : %d
                Country Code : %s
                """.formatted(
                    driver.firstName(),
                    driver.lastName(),
                    driver.driverNumber(),
                    driver.countryCode()
            );
    }
}
