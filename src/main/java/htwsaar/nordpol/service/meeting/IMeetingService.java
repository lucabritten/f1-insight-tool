package htwsaar.nordpol.service.meeting;

import htwsaar.nordpol.domain.Meeting;

import java.util.List;

public interface IMeetingService {
    Meeting getMeetingByYearAndLocation(int year, String location);
    List<Meeting> getMeetingsForSessionReport(int year);
}
