package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.presentation.view.LapsWithContext;
import htwsaar.nordpol.service.lap.ILapService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/laps")
public class LapsController {

    private final ILapService lapService;

    public LapsController(ILapService lapService) {
        this.lapService = lapService;
    }

    @GetMapping
    public LapsWithContext getLapsByLocationYearSessionNameAndDriverNumber(
            @RequestParam(name = "location") String location,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "session") String session,
            @RequestParam(name = "driver_number") int driverNumber
            ) {
        SessionName sessionName = SessionName.fromString(session);
        return lapService.getLapsByLocationYearSessionNameAndDriverNumber(location, year, sessionName, driverNumber);
    }

}
