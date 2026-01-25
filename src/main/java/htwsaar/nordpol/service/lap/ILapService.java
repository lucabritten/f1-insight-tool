package htwsaar.nordpol.service.lap;

import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;

import java.util.List;

public interface ILapService {
    List<Lap> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);
    LapsWithContext getFastestLapByLocationYearSessionNameAndDriverNumber(String location, int year, SessionName sessionName, int driverNUmber);
    LapsWithContext getFastestLapByLocationYearAndSessionName(String location, int year, SessionName sessionName);
}
