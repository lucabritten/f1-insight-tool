package htwsaar.nordpol.service;


import htwsaar.nordpol.api.meeting.IMeetingClient;
import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.util.Mapper;

import java.util.Optional;

public class MeetingService {


    private final IMeetingRepo meetingRepo;
    private final IMeetingClient meetingClient;

    public MeetingService(IMeetingRepo meetingRepo, IMeetingClient meetingClient) {
        if (meetingRepo == null) {
            throw new IllegalArgumentException("meetingRepo must not be null.");
        }
        if (meetingClient == null) {
            throw new IllegalArgumentException("meetingClient must not be null.");
        }
        this.meetingRepo = meetingRepo;
        this.meetingClient = meetingClient;
    }

    public Meeting getMeetingBySeasonAndLocation(int season, String location){
        Optional<MeetingDto> dtoFromDB = meetingRepo.getMeetingBySeasonAndLocation(season, location);
        if (dtoFromDB.isPresent()) {
            return Mapper.toMeeting(dtoFromDB.get());
        }

        Optional<MeetingDto> dtoFromApi =
                meetingClient.getMeetingBySeasonAndLocation(season, location);

        if (dtoFromApi.isPresent()) {
            MeetingDto meetingDto = dtoFromApi.get();
            meetingRepo.save(meetingDto);
            return Mapper.toMeeting(meetingDto);
        }
        throw new MeetingNotFoundException(season, location);
    }
}