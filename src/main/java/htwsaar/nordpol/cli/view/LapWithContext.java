package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.Lap;

import java.util.List;

public record LapWithContext(
        String meetingName,
        String driverName,
        String sessionName,
        List<Lap> laps) {
}
