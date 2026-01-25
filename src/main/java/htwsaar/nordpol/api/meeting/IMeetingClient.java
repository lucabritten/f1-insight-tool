package htwsaar.nordpol.api.meeting;

import htwsaar.nordpol.api.dto.MeetingDto;

import java.util.Optional;

public interface IMeetingClient {
    Optional<MeetingDto> getMeetingByYearAndLocation(int year, String location);
    Optional<MeetingDto> getMeetingsByYear(int year);
}
