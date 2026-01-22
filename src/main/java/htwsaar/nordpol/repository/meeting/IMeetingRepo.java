package htwsaar.nordpol.repository.meeting;

import htwsaar.nordpol.api.dto.MeetingDto;

import java.util.Optional;

public interface IMeetingRepo {

    void save(MeetingDto dto);
    Optional<MeetingDto> getMeetingByYearAndLocation(int year, String location);
}
