package htwsaar.nordpol.api.sessionresult;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.api.dto.SessionResultDto;
import java.util.List;
import java.util.Map;
import static htwsaar.nordpol.api.OpenF1Param.*;

public class SessionResultClient extends BaseClient implements ISessionResultClient {

    public SessionResultClient(String baseUrl){
        super(baseUrl);
    }

    public SessionResultClient() {
        super();
    }

    @Override
    public List<SessionResultDto> getSessionResultBySessionKey(int sessionKey) {
        return fetchList(
                OpenF1Endpoint.SESSION_RESULTS,
                Map.of(SESSION_KEY, sessionKey),
                SessionResultDto[].class
        );
    }

}

