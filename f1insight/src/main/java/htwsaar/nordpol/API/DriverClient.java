package htwsaar.nordpol.API;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;

import java.io.IOException;

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

    public DriverApiDto getDriverByName(String surname, String lastname) {
        String url = BASE_URL + "drivers?full_name=" + surname + "%20" + lastname + "&meeting_key=latest&session_key=latest";

        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){
            String json = response.body().string();

            DriverApiDto[] result = objectMapper.readValue(json, DriverApiDto[].class);

            return result[0];
        }
        catch (IOException e){
            System.out.println(e.getMessage() + "DriverClient IO Exception");
        }
        throw new RuntimeException("Did not find any Driver");
    }





}
