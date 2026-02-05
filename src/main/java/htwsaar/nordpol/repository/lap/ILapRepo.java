package htwsaar.nordpol.repository.lap;

import htwsaar.nordpol.dto.LapDto;

import java.util.List;

public interface ILapRepo {
    void saveAll(List<LapDto> lapDto);
    List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
    List<LapDto> getFastestLapsBySessionKey(int sessionKey, int limit);
}
