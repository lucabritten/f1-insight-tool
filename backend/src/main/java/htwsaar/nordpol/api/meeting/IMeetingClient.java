package htwsaar.nordpol.api.meeting;

import htwsaar.nordpol.dto.MeetingDto;

import java.util.List;
import java.util.Optional;

public interface IMeetingClient {
    Optional<MeetingDto> getMeetingByYearAndLocation(int year, String location);
    List<MeetingDto> getMeetingsByYear(int year);
}
