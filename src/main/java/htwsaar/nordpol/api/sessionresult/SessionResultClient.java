package htwsaar.nordpol.api.sessionresult;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.dto.SessionResultDto;
import java.util.List;
import java.util.Map;

public class SessionResultClient extends BaseClient {

    public SessionResultClient(String baseUrl){
        super(baseUrl);
    }

    public SessionResultClient() {
        super();
    }

    public List<SessionResultDto> getSessionResultBySessionKey(int sessionKey) {
        return fetchList(
                "/session_result",
                Map.of("session_key", sessionKey),
                SessionResultDto[].class
        );
    }

}

