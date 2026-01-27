package htwsaar.nordpol.api.meeting;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.dto.MeetingDto;
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
                "/meetings",
                Map.of(
                        "year", year,
                        "location", location
                ),
                MeetingDto[].class
        );
    }

    @Override
    public Optional<MeetingDto> getMeetingsByYear (int year) {
        return fetchSingle(
                "/meetings",
                Map.of("year", year),
                MeetingDto[].class
        );
    }
}
