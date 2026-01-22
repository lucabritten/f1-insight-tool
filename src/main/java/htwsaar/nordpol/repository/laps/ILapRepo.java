package htwsaar.nordpol.repository.laps;

import htwsaar.nordpol.api.dto.LapDto;

import java.util.List;

public interface ILapRepo {
    void saveAll(List<LapDto> lapDto);
    List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
