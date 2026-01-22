package htwsaar.nordpol.api.lap;

import htwsaar.nordpol.api.dto.LapDto;

import java.util.List;
import java.util.Optional;

public interface ILapClient {
    Optional<List<LapDto>> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
