package htwsaar.nordpol.service;

import htwsaar.nordpol.api.laps.ILapsClient;
import htwsaar.nordpol.api.dto.LapDto;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.exception.LapsNotFoundException;
import htwsaar.nordpol.repository.laps.ILapsRepo;
import htwsaar.nordpol.util.Mapper;

import java.util.List;
import java.util.Optional;

public class LapsService implements ILapsService {

    private final ILapsRepo lapsRepo;
    private final ILapsClient lapsClient;

    public LapsService(ILapsRepo lapsRepo, ILapsClient lapsClient) {
        if (lapsClient == null) {
            throw new IllegalArgumentException("LapsClient cannot be null");
        }
        if (lapsRepo == null) {
            throw new IllegalArgumentException("LapsRepo cannot be null");
        }
        this.lapsRepo = lapsRepo;
        this.lapsClient = lapsClient;
    }


    public List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        List<LapDto> dtoFromDB = lapsRepo.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        if (!dtoFromDB.isEmpty()) {
            return dtoFromDB.stream()
                    .map(Mapper::toLap)
                    .toList();
        }

        Optional<List<LapDto>> dtoFromApi =
                lapsClient.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);

        if (dtoFromApi.isPresent()) {
            List<LapDto> lapsDto = dtoFromApi.get();
            lapsRepo.saveAll(lapsDto);
            return lapsDto.stream()
                    .map(Mapper::toLap)
                    .toList();
        }
        throw new LapsNotFoundException(sessionKey, driverNumber);
    }
}
