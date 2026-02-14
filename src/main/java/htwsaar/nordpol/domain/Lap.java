package htwsaar.nordpol.domain;

public record Lap(int driverNumber,
                  int lapNumber,
                  int sessionKey,
                  Double durationSector1,
                  Double durationSector2,
                  Double durationSector3,
                  Double lapDuration,
                  boolean isPitOutLap
) {
}
