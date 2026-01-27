package htwsaar.nordpol.report;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionName;

import java.util.List;
import java.util.Map;

public record SessionReport(
        String meetingName,
        SessionName sessionName,
        int year,
        String location,
        WeatherWithContext weather,
        SessionResultWithContext sessionResults,
        Map<Driver, List<Lap>> lapSeriesByDriver
) {
}
