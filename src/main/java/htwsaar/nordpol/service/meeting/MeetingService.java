package htwsaar.nordpol.service.meeting;


import htwsaar.nordpol.api.meeting.IMeetingClient;
import htwsaar.nordpol.api.dto.MeetingDto;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.util.Mapper;

import java.util.List;
import java.util.Optional;

public class MeetingService implements IMeetingService {


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

    public Meeting getMeetingByYearAndLocation(int year, String location){
        Optional<MeetingDto> dtoFromDB = meetingRepo.getMeetingByYearAndLocation(year, location);
        if (dtoFromDB.isPresent()) {
            return Mapper.toMeeting(dtoFromDB.get());
        }

        Optional<MeetingDto> dtoFromApi =
                meetingClient.getMeetingByYearAndLocation(year, location);

        if (dtoFromApi.isPresent()) {
            MeetingDto meetingDto = dtoFromApi.get();
            meetingRepo.save(meetingDto);
            return Mapper.toMeeting(meetingDto);
        }
        throw new MeetingNotFoundException(year, location);
    }

    public List<Meeting> getMeetingsForSessionReport(int year){
        List<MeetingDto> dtoFromDB = meetingRepo.getMeetingsByYear(year);
        System.out.println(dtoFromDB.size());
        if (dtoFromDB.size() > 10) {
            return dtoFromDB
                    .stream()
                    .map(Mapper::toMeeting)
                    .toList();
        }

        List<MeetingDto> dtoFromApi =
                meetingClient.getMeetingsByYear(year);

        if (!dtoFromApi.isEmpty()) {
            meetingRepo.saveList(dtoFromApi);
            return dtoFromApi.stream().map(Mapper::toMeeting).toList();
        }
        throw new MeetingNotFoundException(year, "");
    }

}
