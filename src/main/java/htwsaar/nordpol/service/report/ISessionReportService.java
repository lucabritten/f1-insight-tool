package htwsaar.nordpol.service.report;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionReport;

public interface ISessionReportService {
    SessionReport buildReport(String location, int year, SessionName sessionName, Integer topDrivers);
}
