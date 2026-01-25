package htwsaar.nordpol.service;


import htwsaar.nordpol.api.dto.DriverDto;
import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.repository.driver.IDriverRepo;

import htwsaar.nordpol.exception.DriverNotFoundException;
import htwsaar.nordpol.service.driver.DriverService;
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
    IDriverRepo IDriverRepo;

    @Mock
    DriverClient driverClient;

    @InjectMocks
    DriverService driverService;

    @Test
    void getDriverByName_returnsDriverFromDataBase() {
        DriverDto dbDto =
                new DriverDto("Lewis", "Hamilton", 44, "GBR");

        when(IDriverRepo.getDriverByFullNameForYear("Lewis", "Hamilton", 2025))
                .thenReturn(Optional.of(dbDto));

        Driver result =
                driverService.getDriverByNameAndYear("Lewis", "Hamilton", 2025);

        assertThat(result.firstName()).isEqualTo("Lewis");

        verify(driverClient, never()).getDriverByName(anyString(), anyString(), anyInt());
        verify(IDriverRepo).getDriverByFullNameForYear("Lewis", "Hamilton", 2025);
    }

    @Test
    void getDriverByName_fetchesFromApiAndSavesDriver() {
        when(IDriverRepo.getDriverByFullNameForYear("Max", "Verstappen", 2025))
                .thenReturn(Optional.empty());

        DriverDto apiDto =
                new DriverDto("Max", "Verstappen", 1, "NLD");

        when(driverClient.getDriverByName(eq("Max"), eq("Verstappen"), anyInt()))
                .thenReturn(Optional.of(apiDto));

        Driver result =
                driverService.getDriverByNameAndYear("Max", "Verstappen", 2025);

        assertThat(result.firstName()).isEqualTo("Max");

        verify(IDriverRepo).saveOrUpdateDriverForYear(apiDto, 2025);
    }

    @Test
    void getDriverByName_throwsException_whenDriverNotFoundAnywhere() {
        when(IDriverRepo.getDriverByFullNameForYear(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

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
