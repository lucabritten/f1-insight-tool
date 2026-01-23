package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.Lap;

public record FastestLapWithContext(
        String meetingName,
        String sessionName,
        Lap fastestLap) {
}
