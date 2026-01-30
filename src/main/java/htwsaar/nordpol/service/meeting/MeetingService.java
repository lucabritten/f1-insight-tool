package htwsaar.nordpol.service.meeting;


import htwsaar.nordpol.api.meeting.IMeetingClient;
import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.util.Mapper;

import java.util.List;

public class MeetingService implements IMeetingService {


    private final IMeetingRepo meetingRepo;
    private final IMeetingClient meetingClient;
    private final ICacheService cacheService;

    public MeetingService(IMeetingRepo meetingRepo, IMeetingClient meetingClient, ICacheService cacheService) {

        if (meetingRepo == null) {
            throw new IllegalArgumentException("meetingRepo must not be null.");
        }
        if (meetingClient == null) {
            throw new IllegalArgumentException("meetingClient must not be null.");
        }
        if(cacheService == null) {
            throw new IllegalArgumentException("cacheServive must not be null");
        }
        this.meetingRepo = meetingRepo;
        this.meetingClient = meetingClient;
        this.cacheService = cacheService;
    }

    @Override
    public Meeting getMeetingByYearAndLocation(int year, String location){

        MeetingDto dto = cacheService.getOrFetchOptional(
                ()-> meetingRepo.getMeetingByYearAndLocation(year, location),
                () -> meetingClient.getMeetingByYearAndLocation(year, location),
                meetingRepo::save,
                () -> new MeetingNotFoundException(year, location)
        );
        return Mapper.toMeeting(dto);
    }

    @Override
    public List<Meeting> getMeetingsForSessionReport(int year){

        List<MeetingDto> dtoList = cacheService.getOrFetchList(
                () -> meetingRepo.getMeetingsByYear(year),
                () -> meetingClient.getMeetingsByYear(year),
                meetingRepo::saveList,
                () -> new MeetingNotFoundException(year, "")
        );
        return dtoList
                .stream()
                .map(Mapper::toMeeting)
                .toList();
    }

}
