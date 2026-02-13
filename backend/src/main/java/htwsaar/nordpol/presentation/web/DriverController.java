package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.service.driver.IDriverService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/driver")
public class DriverController {

    private final IDriverService driverService;

    public DriverController(IDriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public Driver getDriverByNameAndYear(
            @RequestParam(name = "first_name") String firstName,
            @RequestParam(name = "last_name") String lastName,
            @RequestParam(name = "year") int year
    ) {
        return driverService.getDriverByNameAndYear(firstName,lastName,year);
    }
}
