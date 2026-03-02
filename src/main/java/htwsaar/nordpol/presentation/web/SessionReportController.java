package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.service.report.ISessionReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class SessionReportController {

    private final ISessionReportService reportService;

    public SessionReportController(ISessionReportService reportService) {
        this.reportService = reportService;
    }
//
//    @GetMapping
//    public
}
