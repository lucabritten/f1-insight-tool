package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionReport;
import htwsaar.nordpol.presentation.web.dto.DriverLapSeriesDto;
import htwsaar.nordpol.presentation.web.dto.SessionReportDto;
import htwsaar.nordpol.service.report.ISessionReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class SessionReportController {

    private final ISessionReportService reportService;

    public SessionReportController(ISessionReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public SessionReportDto buildReport(
            @RequestParam(name = "location") String location,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "session") String session,
            @RequestParam(name = "top_drivers", required = false) Integer topDrivers
    ) {
        SessionName sessionName = SessionName.fromString(session);
        SessionReport report = reportService.buildReport(
                location,
                year,
                sessionName,
                topDrivers,
                ignored -> {
                }
        );

        List<DriverLapSeriesDto> lapSeries = report.lapSeriesByDriver().entrySet().stream()
                .map(e -> new DriverLapSeriesDto(e.getKey(), e.getValue()))
                .toList();

        return new SessionReportDto(
                report.meetingName(),
                report.sessionName(),
                report.year(),
                report.location(),
                report.weather(),
                report.sessionResults(),
                lapSeries,
                report.countryFlagUrl()
        );
    }
}
