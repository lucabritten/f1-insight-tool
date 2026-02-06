package htwsaar.nordpol.api.driver;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.dto.DriverDto;
import okhttp3.OkHttpClient;

import java.util.Map;
import java.util.Optional;
import static htwsaar.nordpol.api.OpenF1Param.*;


public class DriverClient extends BaseClient implements IDriverClient {

    public DriverClient(String baseUrl, OkHttpClient okHttpClient, ObjectMapper mapper) {
        super(okHttpClient, baseUrl, mapper);
    }

    public DriverClient(OkHttpClient okHttpClient, ObjectMapper mapper) {
        super(okHttpClient, mapper);
    }

    @Override
    public Optional<DriverDto> getDriverByName(String firstName, String lastName, int meetingKey) {
        return fetchSingle(
                OpenF1Endpoint.DRIVERS,
                Map.of(
                        FIRST_NAME, firstName,
                        LAST_NAME, lastName,
                        MEETING_KEY, meetingKey
                ),
                DriverDto[].class
        );
    }

    @Override
    public Optional<DriverDto> getDriverByNumberAndMeetingKey(int number, int meetingKey) {
        return fetchSingle(
                OpenF1Endpoint.DRIVERS,
                Map.of(
                        DRIVER_NUMBER, number,
                        MEETING_KEY, meetingKey
                ),
                DriverDto[].class
        );
    }
}
