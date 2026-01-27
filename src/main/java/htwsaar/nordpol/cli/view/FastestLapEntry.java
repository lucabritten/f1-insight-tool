package htwsaar.nordpol.cli.view;

public record FastestLapEntry(
        String driverName,
        double lapDuration,
        int lapNumber,
        int driverNumber
) {
}
