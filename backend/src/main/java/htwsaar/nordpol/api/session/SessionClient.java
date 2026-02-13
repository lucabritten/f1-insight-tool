package htwsaar.nordpol.api.session;
import tools.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.dto.SessionDto;
import okhttp3.OkHttpClient;

import java.util.Map;
import java.util.Optional;
import static htwsaar.nordpol.api.OpenF1Param.*;

public class SessionClient extends BaseClient implements ISessionClient {

    public SessionClient(String baseUrl, OkHttpClient okHttpClient, ObjectMapper mapper){
        super(okHttpClient, baseUrl, mapper);
    }

    public SessionClient(OkHttpClient okHttpClient, ObjectMapper mapper) {
        super(okHttpClient, mapper);
    }

    @Override
    public Optional<SessionDto> getSessionByMeetingKeyAndsessionName(int meetingKey, String sessionName) {
        return fetchSingle(
                OpenF1Endpoint.SESSIONS,
                Map.of(
                        MEETING_KEY, meetingKey,
                        SESSION_NAME, sessionName
                ),
                SessionDto[].class
        );
    }
}

