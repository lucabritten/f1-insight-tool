package htwsaar.nordpol.api.meeting;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.api.dto.MeetingDto;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public class MeetingClient extends BaseClient implements IMeetingClient {

    public MeetingClient(String baseUrl){
        super(baseUrl);
    }

    public MeetingClient() {
        super();
    }

    @Override
    public Optional<MeetingDto> getMeetingByYearAndLocation(int year, String location) {
        return fetchSingle(
                OpenF1Endpoint.MEETINGS,
                Map.of(
                        "year", year,
                        "location", location
                ),
                MeetingDto[].class
        );
    }

    @Override
    public List<MeetingDto> getMeetingsByYear (int year) {
        return fetchList(
                OpenF1Endpoint.MEETINGS,
                Map.of("year", year),
                MeetingDto[].class
        );
    }
}
