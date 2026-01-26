package htwsaar.nordpol.domain;

public record Meeting(int meetingKey,
                      String countryCode,
                      String countryName,
                      String location,
                      String meetingName,
                      int year) {
}
