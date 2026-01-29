package htwsaar.nordpol.service.lap;

import htwsaar.nordpol.cli.view.FastestLapsWithContext;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;

import java.util.List;

public interface ILapService {
    List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
    FastestLapsWithContext getFastestLapByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNumber, int count);
    FastestLapsWithContext getFastestLapByLocationYearAndSessionName(String location, int year, SessionName sessionName, int count);
}
