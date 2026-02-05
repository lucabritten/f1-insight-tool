package htwsaar.nordpol.api.session;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.api.dto.SessionDto;
import java.util.Map;
import java.util.Optional;
import static htwsaar.nordpol.api.OpenF1Param.*;

public class SessionClient extends BaseClient implements ISessionClient {

    public SessionClient(String baseUrl, ObjectMapper mapper){
        super(baseUrl, mapper);
    }

    public SessionClient(ObjectMapper mapper) {
        super(mapper);
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

