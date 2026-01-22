package htwsaar.nordpol.repository.laps;

import htwsaar.nordpol.api.dto.LapsDto;

import java.util.List;
import java.util.Optional;

public interface ILapsRepo {
    void saveAll(List<LapsDto> lapsDto);
    Optional<List<LapsDto>> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
}
