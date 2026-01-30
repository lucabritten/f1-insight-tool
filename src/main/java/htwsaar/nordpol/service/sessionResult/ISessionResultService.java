package htwsaar.nordpol.service.sessionResult;

import htwsaar.nordpol.cli.view.SessionResultWithContext;
import htwsaar.nordpol.domain.SessionName;

public interface ISessionResultService {
    SessionResultWithContext getResultByLocationYearAndSessionType(String location, int year, SessionName sessionName);
}
