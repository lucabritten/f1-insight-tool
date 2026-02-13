package htwsaar.nordpol.service.driver;

import htwsaar.nordpol.domain.Driver;

import java.util.List;

public interface IDriverService {
    Driver getDriverByNameAndYear(String firstName, String lastName, int year);
    Driver getDriverByNumberAndYear(int number, int year);
    Driver getDriverByNumberAndMeetingKey(int number, int year, int meetingKey);
    Driver getDriverByNumberWithFallback(int number, int year, int meetingKey);
    List<Driver> getDriversBySessionKey(int sessionKey);
    void preloadMissingDriversForMeeting(int year, int meetingKey, List<Integer> driverNumbers);
}
