package htwsaar.nordpol.api.meeting;

import htwsaar.nordpol.api.dto.MeetingDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class MeetingClient implements IMeetingClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL;

    public MeetingClient(String url){
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.BASE_URL = url;
    }

    public MeetingClient() {
        this("https://api.openf1.org/v1");
    }

    @Override
    public Optional<MeetingDto> getMeetingByYearAndLocation(int year, String location) {
        String url = BASE_URL + "/meetings?"
                    + "year=" + year
                    + "&location=" + location;

        return getMeetingDto(url);
    }

    @Override
    public Optional<MeetingDto> getMeetingsByYear (int year) {
        String url = BASE_URL + "/meetings?"
                + "year=" + year;

        return getMeetingDto(url);
    }

    @NotNull
    private Optional<MeetingDto> getMeetingDto(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){

            if(!response.isSuccessful())
                return Optional.empty();

            MeetingDto[] result =
                    objectMapper.readValue(response.body().string(), MeetingDto[].class);

            if (result.length == 0)
                return Optional.empty();

            return Optional.of(result[0]);

        } catch (IOException e){
            throw new RuntimeException("Failed to fetch meeting from OpenF1 API", e);
        }
    }
}
