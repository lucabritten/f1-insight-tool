package htwsaar.nordpol.api.weather;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.api.dto.WeatherDto;
import java.util.List;
import java.util.Map;
import static htwsaar.nordpol.api.OpenF1Param.*;

public class WeatherClient extends BaseClient implements IWeatherClient{

    public WeatherClient(String baseUrl, ObjectMapper mapper){
        super(baseUrl, mapper);
    }

    public WeatherClient(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public List<WeatherDto> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey) {

        return fetchList(
                OpenF1Endpoint.WEATHER,
                Map.of(
                        MEETING_KEY, meetingKey,
                        SESSION_KEY, sessionKey
                ),
                WeatherDto[].class
        );
    }
}
