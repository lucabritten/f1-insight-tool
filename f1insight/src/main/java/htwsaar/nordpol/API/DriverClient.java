package htwsaar.nordpol.API;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

public class DriverClient {


    //Input vom User aka Fahrername FORMAT https://api.openf1.org/v1/drivers?full_name
    //API-Request--> .json
    //.json refactor
    // ----> output to console

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

    public Optional<DriverApiDto> getDriverByName(String firstName, String lastName) {
        String url = BASE_URL + "/drivers?full_name="
                + firstName + "%20" + lastName
                + "&meeting_key=latest&session_key=latest";

        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){

            if(!response.isSuccessful())
                return Optional.empty();

            DriverApiDto[] result =
                    objectMapper.readValue(response.body().string(), DriverApiDto[].class);

            if(result.length == 0)
                return Optional.empty();

            return Optional.of(result[0]);

        } catch (IOException e){
            throw new RuntimeException("Failed to fetch driver from OpenF1 API", e);
        }
    }





}
