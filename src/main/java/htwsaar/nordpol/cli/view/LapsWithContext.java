package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.Laps;

import java.util.List;

public record LapsWithContext(
        String meetingName,
        String driverName,
        String sessionName,
        List<Laps> laps) {
}
