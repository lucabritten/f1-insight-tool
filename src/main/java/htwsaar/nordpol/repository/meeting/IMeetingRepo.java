package htwsaar.nordpol.repository;

import htwsaar.nordpol.api.dto.MeetingDto;

import java.util.Optional;

public interface IMeetingRepo {

    void save(MeetingDto dto);
    Optional<MeetingDto> getMeetingBySeasonAndLocation(int season, String location);
}
