package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;

import java.util.List;

public record FastestLapsWithContext(
        String meetingName,
        SessionName sessionName,
        List<Driver> drivers,
        List<Lap> fastestLaps
) {
}