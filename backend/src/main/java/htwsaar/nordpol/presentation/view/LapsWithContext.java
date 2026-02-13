package htwsaar.nordpol.presentation.view;

import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;

import java.util.List;

public record LapsWithContext(
        String meetingName,
        String driverName,
        SessionName sessionName,
        List<Lap> laps) {
}
