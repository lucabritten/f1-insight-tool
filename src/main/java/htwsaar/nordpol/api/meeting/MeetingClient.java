package htwsaar.nordpol.api.meeting;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.dto.MeetingDto;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import static htwsaar.nordpol.api.OpenF1Param.*;

public class MeetingClient extends BaseClient implements IMeetingClient {

    public MeetingClient(String baseUrl, OkHttpClient okHttpClient, ObjectMapper mapper){
        super(okHttpClient, baseUrl, mapper);
    }

    public MeetingClient(OkHttpClient okHttpClient, ObjectMapper mapper) {
        super(okHttpClient, mapper);
    }

    @Override
    public Optional<MeetingDto> getMeetingByYearAndLocation(int year, String location) {
        return fetchSingle(
                OpenF1Endpoint.MEETINGS,
                Map.of(
                        YEAR, year,
                        LOCATION, location
                ),
                MeetingDto[].class
        );
    }

    @Override
    public List<MeetingDto> getMeetingsByYear (int year) {
        return fetchList(
                OpenF1Endpoint.MEETINGS,
                Map.of(YEAR, year),
                MeetingDto[].class
        );
    }
}
