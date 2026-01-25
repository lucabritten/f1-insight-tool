package htwsaar.nordpol.service.meeting;

import htwsaar.nordpol.domain.Meeting;

public interface IMeetingService {
    Meeting getMeetingByYearAndLocation(int year, String location);
}
