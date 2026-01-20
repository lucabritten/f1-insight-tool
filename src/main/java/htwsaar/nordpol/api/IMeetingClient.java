package htwsaar.nordpol.api;

import htwsaar.nordpol.api.dto.MeetingDto;

import java.util.Optional;

public interface IMeetingClient {
    Optional<MeetingDto> getMeetingBySeasonAndLocation(int season, String location);
}
