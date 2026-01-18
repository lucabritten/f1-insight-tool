package htwsaar.nordpol.service;


import htwsaar.nordpol.API.DTO.DriverApiDto;
import htwsaar.nordpol.API.DriverClient;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.Repository.DriverRepo;
import htwsaar.nordpol.Service.DriverService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

        when(driverRepo.getDriverByFullname("Lewis", "Hamilton"))
                .thenReturn(Optional.of(dbDto));

        Driver result =
                driverService.getDriverByName("Lewis", "Hamilton");

        assertThat(result.firstName()).isEqualTo("Lewis");

        verify(driverClient, never()).getDriverByName(any(), any());
    }

    @Test
    void getDriverByName_fetchesFromApiAndSavesDriver() {
        when(driverRepo.getDriverByFullname("Max", "Verstappen"))
                .thenReturn(Optional.empty());

        DriverApiDto apiDto =
                new DriverApiDto("Max", "Verstappen", 1, "NLD");

        when(driverClient.getDriverByName("Max", "Verstappen"))
                .thenReturn(Optional.of(apiDto));

        Driver result =
                driverService.getDriverByName("Max", "Verstappen");

        assertThat(result.firstName()).isEqualTo("Max");

        verify(driverRepo).saveDriver(apiDto);
    }

    @Test
    void getDriverByName_throwsException_whenDriverNotFoundAnywhere() {
        when(driverRepo.getDriverByFullname(any(), any()))
                .thenReturn(Optional.empty());

        when(driverClient.getDriverByName(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                driverService.getDriverByName("Alice", "Bob")
        ).isInstanceOf(IllegalStateException.class);
    }
}
