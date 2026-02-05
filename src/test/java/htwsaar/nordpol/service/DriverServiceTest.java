package htwsaar.nordpol.service;


import htwsaar.nordpol.dto.DriverDto;
import htwsaar.nordpol.api.driver.IDriverClient;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.repository.driver.IDriverRepo;

import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.service.driver.DriverService;
import htwsaar.nordpol.service.meeting.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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
    IDriverClient driverClient;

    @Mock
    MeetingService meetingService;

    ICacheService cacheService;

    DriverService driverService;

    @BeforeEach
    void setup() {
        cacheService = ApplicationContext.getInstance().cacheService();
        driverService = new DriverService(driverRepo, driverClient, meetingService, cacheService);
    }

    @Nested
    @DisplayName("Year Validation")
    class YearValidation {

        @Test
        void throwsException_whenYearIsInvalid() {
            assertThatThrownBy(() ->
                    driverService.getDriverByNameAndYear("Max", "Verstappen", 2022)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Only data from 2023 onwards is available.");
        }
    }

    @Nested
    @DisplayName("getDriverByNameAndYear")
    class GetDriverByNameAndYear {

        @Test
        void returnsDriverFromDatabase() {
            DriverDto dbDto = new DriverDto("Lewis", "Hamilton", 44, "GBR");
            Meeting meeting = new Meeting(1279, "AUS", "Australia",
                    "Melbourne", "Australia GP", 2025, "https://www.url_to_flag.com");

            when(meetingService.getMeetingsByYear(2025))
                    .thenReturn(List.of(meeting));
            when(driverRepo.getDriverByFullNameForYear("Lewis", "Hamilton", 2025))
                    .thenReturn(Optional.of(dbDto));

            Driver result = driverService.getDriverByNameAndYear("Lewis", "Hamilton", 2025);

            assertThat(result.firstName()).isEqualTo("Lewis");
            verify(driverClient, never()).getDriverByName(anyString(), anyString(), anyInt());
            verify(driverRepo).getDriverByFullNameForYear("Lewis", "Hamilton", 2025);
        }

        @Test
        void fetchesFromApiAndSavesDriver() {
            Meeting meeting = new Meeting(1279, "AUS", "Australia", "Melbourne", "Australia GP", 2026, "https://www.url_to_flag.com");

            when(driverRepo.getDriverByFullNameForYear("Max", "Verstappen", 2026))
                    .thenReturn(Optional.empty());

            DriverDto apiDto = new DriverDto("Max", "Verstappen", 1, "NLD");

            when(driverClient.getDriverByName(eq("Max"), eq("Verstappen"), anyInt()))
                    .thenReturn(Optional.of(apiDto));
            when(meetingService.getMeetingsByYear(anyInt()))
                    .thenReturn(List.of(meeting));

            Driver result = driverService.getDriverByNameAndYear("Max", "Verstappen", 2026);

            assertThat(result.firstName()).isEqualTo("Max");
            verify(driverRepo).saveOrUpdateDriverForYear(apiDto, 2026, 1279);
        }

        @Test
        void throwsException_whenDriverNotFoundAnywhere() {
            Meeting meeting = new Meeting(1279, "AUS", "Australia", "Melbourne", "Australia GP", 2026, "https://www.url_to_flag.com");

            when(driverRepo.getDriverByFullNameForYear(anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());
            when(meetingService.getMeetingsByYear(anyInt()))
                    .thenReturn(List.of(meeting));
            when(driverClient.getDriverByName(anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    driverService.getDriverByNameAndYear("Alice", "Bob", 2025)
            ).isInstanceOf(DriverNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getDriverByNumberAndYear")
    class GetDriverByNumberAndYear {

        @Test
        void returnsDriverFromDatabase() {
            DriverDto dbDto = new DriverDto("Max", "Verstappen", 1, "NLD");
            Meeting meeting = new Meeting(1279, "AUS", "Australia", "Melbourne", "Australia GP", 2025, "https://www.url_to_flag.com");

            when(meetingService.getMeetingsByYear(2025))
                    .thenReturn(List.of(meeting));
            when(driverRepo.getDriverByStartNumberForYear(1, 2025))
                    .thenReturn(Optional.of(dbDto));

            Driver result = driverService.getDriverByNumberAndYear(1, 2025);

            assertThat(result.driverNumber()).isEqualTo(1);
            assertThat(result.firstName()).isEqualTo("Max");
            verify(driverClient, never()).getDriverByNumberAndMeetingKey(anyInt(), anyInt());
        }

        @Test
        void fetchesFromApiAndSavesDriver() {
            Meeting meeting = new Meeting(1279, "AUS", "Australia", "Melbourne", "Australia GP", 2025, "https://www.url_to_flag.com");
            DriverDto apiDto = new DriverDto("Charles", "Leclerc", 16, "MCO");

            when(meetingService.getMeetingsByYear(2025))
                    .thenReturn(List.of(meeting));
            when(driverRepo.getDriverByStartNumberForYear(16, 2025))
                    .thenReturn(Optional.empty());
            when(driverClient.getDriverByNumberAndMeetingKey(16, 1279))
                    .thenReturn(Optional.of(apiDto));

            Driver result = driverService.getDriverByNumberAndYear(16, 2025);

            assertThat(result.driverNumber()).isEqualTo(16);
            verify(driverRepo).saveOrUpdateDriverForYear(apiDto, 2025, 1279);
        }

        @Test
        void throwsException_whenDriverNotFound() {
            Meeting meeting = new Meeting(1279, "AUS", "Australia", "Melbourne", "Australia GP", 2025, "https://www.url_to_flag.com");

            when(meetingService.getMeetingsByYear(2025))
                    .thenReturn(List.of(meeting));
            when(driverRepo.getDriverByStartNumberForYear(99, 2025))
                    .thenReturn(Optional.empty());
            when(driverClient.getDriverByNumberAndMeetingKey(99, 1279))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> driverService.getDriverByNumberAndYear(99, 2025))
                    .isInstanceOf(DriverNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getDriverByNumberAndMeetingKey")
    class GetDriverByNumberAndMeetingKey {

        @Test
        void returnsDriverFromDatabase() {
            DriverDto dbDto = new DriverDto("Lando", "Norris", 4, "GBR");

            when(driverRepo.getDriverByStartNumberForYear(4, 2025))
                    .thenReturn(Optional.of(dbDto));

            Driver result = driverService.getDriverByNumberAndMeetingKey(4, 2025, 1280);

            assertThat(result.driverNumber()).isEqualTo(4);
            verify(driverClient, never()).getDriverByNumberAndMeetingKey(anyInt(), anyInt());
        }

        @Test
        void fetchesFromApiAndSavesDriver() {
            DriverDto apiDto = new DriverDto("Oscar", "Piastri", 81, "AUS");

            when(driverRepo.getDriverByStartNumberForYear(81, 2025))
                    .thenReturn(Optional.empty());
            when(driverClient.getDriverByNumberAndMeetingKey(81, 1280))
                    .thenReturn(Optional.of(apiDto));

            Driver result = driverService.getDriverByNumberAndMeetingKey(81, 2025, 1280);

            assertThat(result.driverNumber()).isEqualTo(81);
            verify(driverRepo).saveOrUpdateDriverForYear(apiDto, 2025, 1280);
        }

        @Test
        void throwsException_whenDriverNotFound() {
            when(driverRepo.getDriverByStartNumberForYear(99, 2025))
                    .thenReturn(Optional.empty());
            when(driverClient.getDriverByNumberAndMeetingKey(99, 1280))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> driverService.getDriverByNumberAndMeetingKey(99, 2025, 1280))
                    .isInstanceOf(DriverNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getDriverByNumberWithFallback")
    class GetDriverByNumberWithFallback {

        @Test
        void returnsDriverFromDatabase() {
            DriverDto dbDto = new DriverDto("George", "Russell", 63, "GBR");

            when(driverRepo.getDriverByStartNumberForYear(63, 2025))
                    .thenReturn(Optional.of(dbDto));

            Driver result = driverService.getDriverByNumberWithFallback(63, 2025, 1280);

            assertThat(result.driverNumber()).isEqualTo(63);
            verify(driverClient, never()).getDriverByNumberAndMeetingKey(anyInt(), anyInt());
        }

        @Test
        void fallsBackToOtherMeetings() {
            DriverDto apiDto = new DriverDto("Fernando", "Alonso", 14, "ESP");
            Meeting meeting1 = new Meeting(1279, "AUS", "Australia", "Melbourne", "Australia GP", 2025, "https://www.url_to_flag.com");
            Meeting meeting2 = new Meeting(1280, "CHN", "China", "Shanghai", "China GP", 2025, "https://www.url_to_flag.com");

            when(driverRepo.getDriverByStartNumberForYear(14, 2025))
                    .thenReturn(Optional.empty());
            when(driverClient.getDriverByNumberAndMeetingKey(14, 1280))
                    .thenReturn(Optional.empty());
            when(meetingService.getMeetingsByYear(2025))
                    .thenReturn(List.of(meeting1, meeting2));
            when(driverClient.getDriverByNumberAndMeetingKey(14, 1279))
                    .thenReturn(Optional.of(apiDto));

            Driver result = driverService.getDriverByNumberWithFallback(14, 2025, 1280);

            assertThat(result.driverNumber()).isEqualTo(14);
            verify(driverRepo).saveOrUpdateDriverForYear(apiDto, 2025, 1279);
        }

        @Test
        void throwsException_whenAllFallbacksFail() {
            Meeting meeting = new Meeting(1279, "AUS", "Australia", "Melbourne", "Australia GP", 2025, "https://www.url_to_flag.com");

            when(driverRepo.getDriverByStartNumberForYear(99, 2025))
                    .thenReturn(Optional.empty());
            when(driverClient.getDriverByNumberAndMeetingKey(99, 1280))
                    .thenReturn(Optional.empty());
            when(meetingService.getMeetingsByYear(2025))
                    .thenReturn(List.of(meeting));
            when(driverClient.getDriverByNumberAndMeetingKey(99, 1279))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> driverService.getDriverByNumberWithFallback(99, 2025, 1280))
                    .isInstanceOf(DriverNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("preloadMissingDriversForMeeting")
    class PreloadMissingDriversForMeeting {

        @Test
        void loadsOnlyMissingDrivers() {
            DriverDto apiDto = new DriverDto("Sergio", "Perez", 11, "MEX");

            when(driverRepo.hasNamedDriverNumberForYear(1, 2025)).thenReturn(true);
            when(driverRepo.hasNamedDriverNumberForYear(11, 2025)).thenReturn(false);
            when(driverRepo.getDriverByStartNumberForYear(11, 2025)).thenReturn(Optional.empty());
            when(driverClient.getDriverByNumberAndMeetingKey(11, 1280)).thenReturn(Optional.of(apiDto));

            driverService.preloadMissingDriversForMeeting(2025, 1280, List.of(1, 11));

            verify(driverRepo, never()).getDriverByStartNumberForYear(eq(1), anyInt());
            verify(driverRepo).saveOrUpdateDriverForYear(apiDto, 2025, 1280);
        }

        @Test
        void skipsNullDriverNumbers() {
            when(driverRepo.hasNamedDriverNumberForYear(44, 2025)).thenReturn(true);

            driverService.preloadMissingDriversForMeeting(2025, 1280, Arrays.asList(44, null));

            verify(driverRepo, times(1)).hasNamedDriverNumberForYear(anyInt(), anyInt());
        }

        @Test
        void doesNotUseDriverFromDifferentYearDuringFallback() {
            int year = 2026;
            int number = 1;
            int meetingKey = 3000;

            Meeting meeting2026 = new Meeting(meetingKey, "AUS", "Australia",
                    "Melbourne", "Australia GP", 2026, "https://www.url_to_flag.com");

            when(driverRepo.getDriverByStartNumberForYear(number, year))
                    .thenReturn(Optional.empty());

            when(driverClient.getDriverByNumberAndMeetingKey(number, meetingKey))
                    .thenReturn(Optional.empty());

            when(meetingService.getMeetingsByYear(year))
                    .thenReturn(List.of(meeting2026));

            assertThatThrownBy(() ->
                    driverService.getDriverByNumberWithFallback(number, year, meetingKey)
            ).isInstanceOf(DriverNotFoundException.class);

            verify(driverRepo, never())
                    .saveOrUpdateDriverForYear(any(), anyInt(), anyInt());
        }
    }
}
