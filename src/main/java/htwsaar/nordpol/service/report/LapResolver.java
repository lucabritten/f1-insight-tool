package htwsaar.nordpol.service.report;

import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.service.lap.LapService;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class LapResolver {

    private final LapService lapService;

    public LapResolver(LapService lapService) {
        this.lapService = requireNonNull(lapService, "lapService must not be null");
    }

    public List<Lap> resolve(int sessionKey, int driverNumber) {
        try {
            return lapService.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        } catch (LapNotFoundException e) {
            return List.of();
        }
    }
}
