package htwsaar.nordpol.api.weather;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.dto.WeatherDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WeatherClient extends BaseClient implements IWeatherClient{

    public WeatherClient(String baseUrl){
        super(baseUrl);
    }

    public WeatherClient() {
        super();
    }

    @Override
    public List<WeatherDto> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey) {

        return fetchList(
                "/weather",
                Map.of(
                        "meeting_key", meetingKey,
                        "session_key", sessionKey
                ),
                WeatherDto[].class
        );
    }
}
