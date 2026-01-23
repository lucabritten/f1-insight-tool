package htwsaar.nordpol.repository.lap;

import htwsaar.nordpol.api.dto.LapDto;

import java.util.List;
import java.util.Optional;

public interface ILapRepo {
    void saveAll(List<LapDto> lapDto);
    List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
    List<LapDto> getFastestLapBySessionKey(int sessionKey);
}
