package htwsaar.nordpol.domain;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;

import java.util.List;
import java.util.Map;

public record SessionReport(
        String meetingName,
        SessionName sessionName,
        int year,
        String location,
        WeatherWithContext weather,
        SessionResultWithContext sessionResults,
        Map<Driver, List<Lap>> lapSeriesByDriver,
        String countryFlagUrl
) {
}
