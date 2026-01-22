package htwsaar.nordpol.domain;

public record Laps(int driverNumber,
                   int lapNumber,
                   int sessionKey,
                   double durationSector1,
                   double durationSector2,
                   double durationSector3,
                   double lapDuration,
                   boolean isPitOutLap
) {
}
