package htwsaar.nordpol.api.sessionresult;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.dto.SessionResultDto;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Map;
import static htwsaar.nordpol.api.OpenF1Param.*;

public class SessionResultClient extends BaseClient implements ISessionResultClient {

    public SessionResultClient(String baseUrl, OkHttpClient okHttpClient, ObjectMapper mapper){
        super(okHttpClient, baseUrl, mapper);
    }

    public SessionResultClient(OkHttpClient okHttpClient, ObjectMapper mapper) {
        super(okHttpClient, mapper);
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

