package htwsaar.nordpol.presentation.web;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/session-result")
public class SessionResultController {

    private final ISessionResultService sessionResultService;

    public SessionResultController(ISessionResultService sessionResultService) {
        this.sessionResultService = sessionResultService;
    }

    @GetMapping
    public SessionResultWithContext getSessionResultsBySession(
            @RequestParam(name = "location") String location,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "session") String session
    ) {
        SessionName sessionName = SessionName.fromString(session);
        return sessionResultService.getResultByLocationYearAndSessionType(location,year,sessionName);
    }
}
