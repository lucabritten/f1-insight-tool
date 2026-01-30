package htwsaar.nordpol.repository.sessionresult;

import htwsaar.nordpol.api.dto.SessionResultDto;

import java.util.List;

public interface ISessionResultRepo {
    List<SessionResultDto> getSessionResultBySessionKey(int sessionKey);
    void saveAll(List<SessionResultDto> dtoList);
}
