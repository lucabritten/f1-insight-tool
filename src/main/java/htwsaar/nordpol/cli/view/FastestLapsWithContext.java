package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.SessionName;

import java.util.List;

public record FastestLapsWithContext(
        String location,
        SessionName sessionName,
        List<FastestLapEntry> entries
) {
}
