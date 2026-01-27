package htwsaar.nordpol.api.driver;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.dto.DriverDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class DriverClient extends BaseClient {

    public DriverClient(String baseUrl) {
        super(baseUrl);
    }

    public DriverClient() {
        super();
    }

    public Optional<DriverDto> getDriverByName(String firstName, String lastName, int meetingKey) {
        return fetchSingle(
                "/drivers",
                Map.of(
                        "first_name", firstName,
                        "last_name", lastName,
                        "meeting_key", meetingKey
                ),
                DriverDto[].class
        );
    }

    public Optional<DriverDto> getDriverByNumberAndMeetingKey(int number, int meetingKey) {
        return fetchSingle(
                "/drivers",
                Map.of(
                        "driver_number", number,
                        "meeting_key", meetingKey
                ),
                DriverDto[].class
        );
    }

    public Optional<DriverDto> getDriverByNumber(int number) {
        return fetchSingle(
                "/drivers",
                Map.of("driver_number", number),
                DriverDto[].class
        );
    }

    public List<DriverDto> getDriversByMeetingKey(int meetingKey) {
        return fetchList(
                "/drivers",
                Map.of("meeting_key", meetingKey),
                DriverDto[].class
        );
    }
}
