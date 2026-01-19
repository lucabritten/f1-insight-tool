package htwsaar.nordpol.service;


import htwsaar.nordpol.api.dto.DriverApiDto;
import htwsaar.nordpol.api.DriverClient;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.repository.DriverRepo;

import htwsaar.nordpol.exception.DriverNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverServiceTest {

    @Mock
    DriverRepo driverRepo;

    @Mock
    DriverClient driverClient;

    @InjectMocks
    DriverService driverService;

    @Test
    void getDriverByName_returnsDriverFromDataBase() {
        DriverApiDto dbDto =
                new DriverApiDto("Lewis", "Hamilton", 44, "GBR");

        when(driverRepo.getDriverByFullNameForSeason("Lewis", "Hamilton", 2025))
                .thenReturn(Optional.of(dbDto));

        Driver result =
                driverService.getDriverByNameAndSeason("Lewis", "Hamilton", 2025);

        assertThat(result.firstName()).isEqualTo("Lewis");

        verify(driverClient, never()).getDriverByName(anyString(), anyString(), anyInt());
        verify(driverRepo).getDriverByFullNameForSeason("Lewis", "Hamilton", 2025);
    }

    @Test
    void getDriverByName_fetchesFromApiAndSavesDriver() {
        when(driverRepo.getDriverByFullNameForSeason("Max", "Verstappen", 2025))
                .thenReturn(Optional.empty());

        DriverApiDto apiDto =
                new DriverApiDto("Max", "Verstappen", 1, "NLD");

        when(driverClient.getDriverByName(eq("Max"), eq("Verstappen"), anyInt()))
                .thenReturn(Optional.of(apiDto));

        Driver result =
                driverService.getDriverByNameAndSeason("Max", "Verstappen", 2025);

        assertThat(result.firstName()).isEqualTo("Max");

        verify(driverRepo).saveOrUpdateDriverForSeason(apiDto, 2025);
    }

    @Test
    void getDriverByName_throwsException_whenDriverNotFoundAnywhere() {
        when(driverRepo.getDriverByFullNameForSeason(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        when(driverClient.getDriverByName(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                driverService.getDriverByNameAndSeason("Alice", "Bob", 2025)
        ).isInstanceOf(DriverNotFoundException.class);
    }

    @Test
    void getDriverByName_throwsException_whenSeasonIsInvalid() {
        assertThatThrownBy(() ->
                driverService.getDriverByNameAndSeason("Max", "Verstappen", 2022)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No data for season");
    }
}
