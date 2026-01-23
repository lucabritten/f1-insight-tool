package htwsaar.nordpol.service;

import htwsaar.nordpol.domain.Lap;

import java.util.List;

public interface ILapService {
    List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
    Lap getFastestLapBySessionKey(int sessionKey);
    Lap getFastestLapBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
