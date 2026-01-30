package htwsaar.nordpol.api.driver;

import htwsaar.nordpol.api.dto.DriverDto;

import java.util.List;
import java.util.Optional;

public interface IDriverClient {
    Optional<DriverDto> getDriverByName(String firstName, String lastName, int meetingKey);
    Optional<DriverDto> getDriverByNumberAndMeetingKey(int number, int meetingKey);
    Optional<DriverDto> getDriverByNumber(int number);
    List<DriverDto> getDriversByMeetingKey(int meetingKey);
}
