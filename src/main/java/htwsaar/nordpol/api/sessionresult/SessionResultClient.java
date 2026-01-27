package htwsaar.nordpol.api.sessionresult;

import htwsaar.nordpol.api.dto.SessionResultDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SessionResultClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL;

    public SessionResultClient(String url){
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.BASE_URL = url;
    }

    public SessionResultClient() {
        this("https://api.openf1.org/v1");
    }

    public List<SessionResultDto> getSessionResultBySessionKey(int sessionKey) {
        String url = BASE_URL + "/session_result?"
                + "session_key=" + sessionKey;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){

            if(!response.isSuccessful())
                return List.of();

            SessionResultDto[] result =
                    objectMapper.readValue(response.body().string(), SessionResultDto[].class);

            if (result.length == 0)
                return List.of();

            return Arrays.asList(result);

        } catch (IOException e){
            throw new RuntimeException("Failed to fetch Session result from OpenF1 API", e);
        }
    }

}

