package htwsaar.nordpol.api.driver;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.api.dto.DriverDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class DriverClient extends BaseClient implements IDriverClient {

    public DriverClient(String baseUrl) {
        super(baseUrl);
    }

    public DriverClient() {
        super();
    }

    @Override
    public Optional<DriverDto> getDriverByName(String firstName, String lastName, int meetingKey) {
        return fetchSingle(
                OpenF1Endpoint.DRIVERS,
                Map.of(
                        "first_name", firstName,
                        "last_name", lastName,
                        "meeting_key", meetingKey
                ),
                DriverDto[].class
        );
    }

    @Override
    public Optional<DriverDto> getDriverByNumberAndMeetingKey(int number, int meetingKey) {
        return fetchSingle(
                OpenF1Endpoint.DRIVERS,
                Map.of(
                        "driver_number", number,
                        "meeting_key", meetingKey
                ),
                DriverDto[].class
        );
    }

    @Override
    public Optional<DriverDto> getDriverByNumber(int number) {
        return fetchSingle(
                OpenF1Endpoint.DRIVERS,
                Map.of("driver_number", number),
                DriverDto[].class
        );
    }

    @Override
    public List<DriverDto> getDriversByMeetingKey(int meetingKey) {
        return fetchList(
                OpenF1Endpoint.DRIVERS,
                Map.of("meeting_key", meetingKey),
                DriverDto[].class
        );
    }
}
