package htwsaar.nordpol.service.sessionResult;

import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.exception.SessionNotFoundException;

public interface ISessionResultService {
    /**
     * Returns the session results for a given location, year and session.
     * Results are sorted by classification rules (DNF/DNS/DSQ last, then position)
     *
     * @throws SessionNotFoundException if no results are available
     */
    SessionResultWithContext getResultByLocationYearAndSessionType(String location, int year, SessionName sessionName);
}
