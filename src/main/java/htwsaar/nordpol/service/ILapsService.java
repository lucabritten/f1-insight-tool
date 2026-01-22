package htwsaar.nordpol.service;

import htwsaar.nordpol.domain.Laps;

import java.util.List;

public interface ILapsService {
    List<Laps> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
