package htwsaar.nordpol.api.sessionresult;

import htwsaar.nordpol.api.dto.SessionResultDto;

import java.util.List;

public interface ISessionResultClient {
    List<SessionResultDto> getSessionResultBySessionKey(int sessionKey);
}
