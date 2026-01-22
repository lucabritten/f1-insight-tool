package htwsaar.nordpol.api.laps;

import htwsaar.nordpol.api.dto.LapsDto;

import java.util.List;
import java.util.Optional;

public interface ILaapsClient {
    Optional<List<LapsDto>> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
