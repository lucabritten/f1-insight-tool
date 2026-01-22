package htwsaar.nordpol.api.driver;

import htwsaar.nordpol.api.dto.DriverDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

/**
 * HTTP client for accessing the OpenF1 API.
 *
 * <p>This client is responsible only for fetching raw API data
 * and mapping it to DTOs. No business logic is applied here.</p>
 */
public class DriverClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL;

    public DriverClient(String url){
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.BASE_URL = url;
    }

    public DriverClient(){
        this("https://api.openf1.org/v1");
    }

    /**
     * Fetches a driver from the OpenF1 APi by name and year.
     *
     * @return an Optional containing the driver DTO if found
     */
    public Optional<DriverDto> getDriverByName(String firstName, String lastName, int meetingKey) {
        String url = BASE_URL + "/drivers?"
                     + "first_name=" + firstName
                     + "&last_name=" + lastName
                     + "&meeting_key=" + meetingKey;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){

            if(!response.isSuccessful())
                return Optional.empty();

            DriverDto[] result =
                    objectMapper.readValue(response.body().string(), DriverDto[].class);

            if(result.length == 0)
                return Optional.empty();

            return Optional.of(result[0]);

        } catch (IOException e){
            throw new RuntimeException("Failed to fetch driver from OpenF1 API", e);
        }
    }
}
