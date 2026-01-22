package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.LapsDto;

import java.util.List;

public interface ILapsService {
    List<LapsDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
