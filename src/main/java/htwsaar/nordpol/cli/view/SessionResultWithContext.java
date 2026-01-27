package htwsaar.nordpol.cli.view;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;

import java.util.List;

public record SessionResultWithContext (
        String meetingName,
        SessionName sessionName,
        List<SessionResult> results
){
}
