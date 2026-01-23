package htwsaar.nordpol.service;

import htwsaar.nordpol.api.lap.ILapClient;
import htwsaar.nordpol.api.dto.LapDto;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.repository.lap.ILapRepo;
import htwsaar.nordpol.util.Mapper;

import java.util.List;

public class LapService implements ILapService {

    private final ILapRepo lapRepo;
    private final ILapClient lapsClient;

    public LapService(ILapRepo lapRepo, ILapClient lapsClient) {
        if (lapsClient == null) {
            throw new IllegalArgumentException("LapsClient cannot be null");
        }
        if (lapRepo == null) {
            throw new IllegalArgumentException("LapsRepo cannot be null");
        }
        this.lapRepo = lapRepo;
        this.lapsClient = lapsClient;
    }

    public List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        List<LapDto> dtoFromDB = lapRepo.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        if (!dtoFromDB.isEmpty()) {
            return dtoFromDB.stream()
                    .map(Mapper::toLap)
                    .toList();
        }

        List<LapDto> dtoFromApi =
                lapsClient.getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);

        if (!dtoFromApi.isEmpty()) {
            lapRepo.saveAll(dtoFromApi);
            return dtoFromApi.stream()
                    .map(Mapper::toLap)
                    .toList();
        }
        throw new LapNotFoundException(sessionKey, driverNumber);
    }

    public Lap getFastestLapBySessionKey(int sessionKey) {
        List<LapDto> fastestFromDb = lapRepo.getFastestLapBySessionKey(sessionKey);
        if (!fastestFromDb.isEmpty()) {
            return Mapper.toLap(fastestFromDb.get(0));
        }

        List<LapDto> apiLaps = lapsClient.getLapsBySessionKey(sessionKey);
        if (apiLaps.isEmpty()) {
            throw new LapNotFoundException(sessionKey, -1);
        }

        lapRepo.saveAll(apiLaps);

        return apiLaps.stream()
                .filter(l -> l.lap_duration() > 0)
                .filter(l -> !l.is_pit_out_lap())
                .min(java.util.Comparator.comparingDouble(LapDto::lap_duration))
                .map(Mapper::toLap)
                .orElseThrow(() -> new LapNotFoundException(sessionKey, -1));
    }

    public Lap getFastestLapBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        List<Lap> laps = getLapsBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        return laps.stream()
                .filter(l -> l.lapDuration() > 0)
                .filter(l -> !l.isPitOutLap())
                .min(java.util.Comparator.comparingDouble(Lap::lapDuration))
                .orElseThrow(() -> new LapNotFoundException(sessionKey, driverNumber));
    }

}
