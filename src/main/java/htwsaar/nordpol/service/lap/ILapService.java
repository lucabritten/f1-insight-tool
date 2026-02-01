package htwsaar.nordpol.service.lap;

import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.LapNotFoundException;

import java.util.List;

public interface ILapService {
    /**
     * Returns all laps of a driver for a given session and meeting context.
     *
     * @throws LapNotFoundException if no laps are available in the api or are locally cached.
     */
    LapsWithContext getLapsByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber);
    List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
    FastestLapsWithContext getFastestLapByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber, int count);
    FastestLapsWithContext getFastestLapByLocationYearAndSessionName(String location, int year, SessionName sessionName, int count);
}
