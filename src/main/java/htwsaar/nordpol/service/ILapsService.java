package htwsaar.nordpol.service;

import htwsaar.nordpol.domain.Lap;

import java.util.List;

public interface ILapsService {
    List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
