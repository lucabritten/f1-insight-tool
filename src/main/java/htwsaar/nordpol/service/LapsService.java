package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.LapsDto;
import htwsaar.nordpol.api.laps.ILapsClient;
import htwsaar.nordpol.exception.LapsNotFoundException;
import htwsaar.nordpol.repository.laps.ILapsRepo;

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


    public List<LapsDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        Optional<List<LapsDto>> dtoFromDB = lapsRepo.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        if (dtoFromDB.isPresent()) {
            return dtoFromDB.get();
        }

        Optional<List<LapsDto>> dtoFromApi =
                lapsClient.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);

        if (dtoFromApi.isPresent()) {
            List<LapsDto> lapsDto = dtoFromApi.get();
            lapsRepo.saveAll(lapsDto);
            return lapsDto;
        }
        throw new LapsNotFoundException(sessionKey, driverNumber);
    }
}
