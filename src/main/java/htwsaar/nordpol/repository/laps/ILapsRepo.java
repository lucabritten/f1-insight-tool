package htwsaar.nordpol.repository.laps;

import htwsaar.nordpol.api.dto.LapDto;

import java.util.List;

public interface ILapsRepo {
    void saveAll(List<LapDto> lapsDto);
    List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
