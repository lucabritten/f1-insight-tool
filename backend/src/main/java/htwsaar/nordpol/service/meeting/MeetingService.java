package htwsaar.nordpol.service.meeting;


import htwsaar.nordpol.api.meeting.IMeetingClient;
import htwsaar.nordpol.dto.MeetingDto;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.repository.meeting.IMeetingRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.util.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

    /**
     * <p>
     * This method differs from the usual cache procedure, due to the reason
     * that there are a different number of race-weekends in a formula 1 season,
     * therefore, we cannot check if every meeting is cached. As a reason, the cached
     * values for a season are only as a fallback, if the OpenF1 API is not available.
     * </p>
     */
    @Override
    public List<Meeting> getMeetingsByYear(int year){

        List<MeetingDto> dtoList = meetingClient.getMeetingsByYear(year);

        if(dtoList.isEmpty())
            dtoList = meetingRepo.getMeetingsByYear(year);

        if(dtoList.isEmpty())
            throw new MeetingNotFoundException(year, "");

        return dtoList
                .stream()
                .map(Mapper::toMeeting)
                .toList();
    }

}
