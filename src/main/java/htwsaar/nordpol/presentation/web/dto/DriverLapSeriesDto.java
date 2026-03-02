package htwsaar.nordpol.presentation.web.dto;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;

import java.util.List;

public record DriverLapSeriesDto(
        Driver driver,
        List<Lap> laps
) {
}

