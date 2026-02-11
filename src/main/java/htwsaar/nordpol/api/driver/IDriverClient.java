package htwsaar.nordpol.api.driver;

import htwsaar.nordpol.dto.DriverDto;

import java.util.Optional;

public interface IDriverClient {
    Optional<DriverDto> getDriverByName(String firstName, String lastName, int meetingKey);
    Optional<DriverDto> getDriverByNumberAndMeetingKey(int number, int meetingKey);
}
