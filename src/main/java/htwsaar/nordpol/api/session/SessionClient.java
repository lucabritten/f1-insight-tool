package htwsaar.nordpol.api.session;

import htwsaar.nordpol.api.dto.SessionDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

public class SessionClient implements ISessionClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL;

    public SessionClient(String url){
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.BASE_URL = url;
    }

    public SessionClient() {
        this("https://api.openf1.org/v1");
    }

    @Override
    public Optional<SessionDto> getSessionByMeetingKeyAndsessionName(int meetingKey, String sessionName) {
            String url = BASE_URL + "/sessions?"
                    + "meeting_key=" + meetingKey
                    + "&session_name=" + sessionName;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try(Response response = okHttpClient.newCall(request).execute()){

                if(!response.isSuccessful())
                    return Optional.empty();

                SessionDto[] result =
                        objectMapper.readValue(response.body().string(),SessionDto[].class);

                if(result.length == 0)
                    return Optional.empty();

                return Optional.of(result[0]);

            } catch (IOException e){
                throw new RuntimeException("Failed to fetch session from OpenF1 API", e);
            }
    }
}

