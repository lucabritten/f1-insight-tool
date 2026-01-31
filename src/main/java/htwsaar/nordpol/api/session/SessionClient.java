package htwsaar.nordpol.api.session;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.api.dto.SessionDto;
import java.util.Map;
import java.util.Optional;

public class SessionClient extends BaseClient implements ISessionClient {

    public SessionClient(String baseUrl){
        super(baseUrl);
    }

    public SessionClient() {
        super();
    }

    @Override
    public Optional<SessionDto> getSessionByMeetingKeyAndsessionName(int meetingKey, String sessionName) {
        return fetchSingle(
                OpenF1Endpoint.SESSIONS,
                Map.of(
                        "meeting_key", meetingKey,
                        "session_name", sessionName
                ),
                SessionDto[].class
        );
    }
}

