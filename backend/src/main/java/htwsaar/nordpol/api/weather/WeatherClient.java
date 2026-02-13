package htwsaar.nordpol.api.weather;
import tools.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.dto.WeatherDto;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Map;
import static htwsaar.nordpol.api.OpenF1Param.*;

public class WeatherClient extends BaseClient implements IWeatherClient{

    public WeatherClient(String baseUrl, OkHttpClient okHttpClient, ObjectMapper mapper){
        super(okHttpClient, baseUrl, mapper);
    }

    public WeatherClient(OkHttpClient okHttpClient, ObjectMapper mapper) {
        super(okHttpClient, mapper);
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
