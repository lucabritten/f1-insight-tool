package htwsaar.nordpol.api.lap;

import htwsaar.nordpol.api.dto.LapDto;

import java.util.List;

public interface ILapClient {
    List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
    List<LapDto> getLapsBySessionKey(int sessionKey);
}
