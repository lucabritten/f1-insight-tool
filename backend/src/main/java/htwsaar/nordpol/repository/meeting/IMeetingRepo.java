package htwsaar.nordpol.repository.meeting;

import htwsaar.nordpol.dto.MeetingDto;

import java.util.List;
import java.util.Optional;

public interface IMeetingRepo {

    void save(MeetingDto dto);
    void saveList(List<MeetingDto> list);
    Optional<MeetingDto> getMeetingByYearAndLocation(int year, String location);
    List<MeetingDto> getMeetingsByYear(int year);

}
