package htwsaar.nordpol.api.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.dto.WeatherDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WeatherClient implements IWeatherClient{

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL;

    public WeatherClient(String url){
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.BASE_URL = url;
    }

    public WeatherClient() {
        this("https://api.openf1.org/v1");
    }

    @Override
    public Optional<List<WeatherDto>> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey) {
        String url = BASE_URL + "/session?"
                + "meeting_key=" + meetingKey
                + "&session_key=" + sessionKey;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){

            if(!response.isSuccessful())
                return Optional.empty();

            WeatherDto[] result = objectMapper.readValue(response.body().string(), WeatherDto[].class);

            if(result.length == 0)
                return Optional.empty();

            return Optional.of(Arrays.asList(result));

        } catch (IOException e){
            throw new RuntimeException("Failed to fetch weather from OpenF1 API", e);
        }
    }
}
