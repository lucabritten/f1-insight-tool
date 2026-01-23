package htwsaar.nordpol.api.lap;

import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.dto.LapDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;


public class LapClient implements ILapClient{

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL;

    public LapClient(String url) {
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.BASE_URL = url;
    }
    public LapClient(){
        this("https://api.openf1.org/v1");
    }

    @Override
    public List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        String url = BASE_URL + "/laps?"
                + "sessionKey=" + sessionKey
                + "&driverNumber=" + driverNumber;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){

            if(!response.isSuccessful())
                return List.of();

            LapDto[] result =
                    objectMapper.readValue(response.body().string(), LapDto[].class);

            if(result.length == 0)
                return List.of();

            return List.of(result);

        } catch (IOException e){
            throw new RuntimeException("Failed to fetch laps from OpenF1 API", e);
        }
    }
}
