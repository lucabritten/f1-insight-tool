package htwsaar.nordpol.presentation.web.dto;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.presentation.view.WeatherWithContext;

import java.util.List;

public record SessionReportDto(
        String meetingName,
        SessionName sessionName,
        int year,
        String location,
        WeatherWithContext weather,
        SessionResultWithContext sessionResults,
        List<DriverLapSeriesDto> lapSeries,
        String countryFlagUrl
) {
}

