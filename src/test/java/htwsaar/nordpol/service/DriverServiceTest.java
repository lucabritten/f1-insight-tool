package htwsaar.nordpol.service;


import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.repository.driver.IDriverRepo;

import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.meeting.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverServiceTest {

    @Mock
    IDriverRepo driverRepo;

    @Mock
    DriverClient driverClient;

    @Mock
    MeetingService meetingService;

    ICacheService cacheService = ApplicationContext.cacheService();

    DriverService driverService;

    @BeforeEach
    void setup() {
        driverService = new DriverService(driverRepo, driverClient, meetingService, cacheService);
    }

    @Test
    void getDriverByName_returnsDriverFromDataBase() {
        DriverDto dbDto =
                new DriverDto("Lewis", "Hamilton", 44, "GBR");

        Meeting meeting = new Meeting(1279, "AUS", "Australia",
                "Melbourne", "Australia GP", 2025);

        when(meetingService.getMeetingsForSessionReport(2025))
                .thenReturn(List.of(meeting));

        when(driverRepo.getDriverByFullNameForYear("Lewis", "Hamilton", 2025))
                .thenReturn(Optional.of(dbDto));

        Driver result =
                driverService.getDriverByNameAndYear("Lewis", "Hamilton", 2025);

        assertThat(result.firstName()).isEqualTo("Lewis");

        verify(driverClient, never()).getDriverByName(anyString(), anyString(), anyInt());
        verify(driverRepo).getDriverByFullNameForYear("Lewis", "Hamilton", 2025);
    }

    @Test
    void getDriverByName_fetchesFromApiAndSavesDriver() {

        Meeting meeting = new Meeting(1279,"AUS", "Australia", "Melbourne", "Australia GP",2026);
        List<Meeting> meetingList = List.of(meeting);

        when(driverRepo.getDriverByFullNameForYear("Max", "Verstappen", 2026))
                .thenReturn(Optional.empty());

        DriverDto apiDto =
                new DriverDto("Max", "Verstappen", 1, "NLD");

        when(driverClient.getDriverByName(eq("Max"), eq("Verstappen"), anyInt()))
                .thenReturn(Optional.of(apiDto));

        when(meetingService.getMeetingsForSessionReport(anyInt()))
                .thenReturn(meetingList);

        Driver result =
                driverService.getDriverByNameAndYear("Max", "Verstappen", 2026);

        assertThat(result.firstName()).isEqualTo("Max");

        verify(driverRepo).saveOrUpdateDriverForYear(apiDto, 2026, 1279);
    }

    @Test
    void getDriverByName_throwsException_whenDriverNotFoundAnywhere() {

        Meeting meeting = new Meeting(1279,"AUS", "Australia", "Melbourne", "Australia GP",2026);
        List<Meeting> meetingList = List.of(meeting);

        when(driverRepo.getDriverByFullNameForYear(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        when(meetingService.getMeetingsForSessionReport(anyInt()))
                .thenReturn(meetingList);

        when(driverClient.getDriverByName(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                driverService.getDriverByNameAndYear("Alice", "Bob", 2025)
        ).isInstanceOf(DriverNotFoundException.class);
    }

    @Test
    void getDriverByName_throwsException_whenYearIsInvalid() {
        assertThatThrownBy(() ->
                driverService.getDriverByNameAndYear("Max", "Verstappen", 2022)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only data from 2023 onwards is available.");
    }
}
