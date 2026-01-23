package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.LapDto;
import htwsaar.nordpol.api.lap.ILapClient;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.exception.LapNotFoundException;
import htwsaar.nordpol.repository.lap.ILapRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LapServiceTest {

    @Mock
    ILapRepo lapRepo;

    @Mock
    ILapClient lapClient;

    @InjectMocks
    LapService lapService;

    @Test
    void getLapsBySessionKeyAndDriverNumber_returnsLapsFromDatabase(){
        LapDto lap1 = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);
        LapDto lap2 = new LapDto(33, 1011, 2, 30.1, 25.8, 30.2, 91.1, false);
        LapDto lap3 = new LapDto(33, 1011, 3, 30.1, 29.8, 31.2, 90.1, true);

        List<LapDto> laps = List.of(lap1, lap2, lap3);

        when(lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(laps);

        List<Lap> result =
                lapService.getLapsBySessionKeyAndDriverNumber(1011, 33);

        assertThat(result.getFirst().sessionKey()).isEqualTo(1011);
        assertThat(result.getFirst().driverNumber()).isEqualTo(33);
        assertThat(result.getFirst().lapNumber()).isEqualTo(1);
        assertThat(result.getLast().sessionKey()).isEqualTo(1011);
        assertThat(result.getLast().driverNumber()).isEqualTo(33);
        assertThat(result.getLast().lapNumber()).isEqualTo(3);

        verify(lapClient, never()).getLapsBySessionKeyAndDriverNumber(1011, 33);
        verify(lapRepo).getLapsBySessionKeyAndDriverNumber(1011, 33);
    }

    @Test
    void getLapsBySessionKeyAndDriverNumber_fetchesFromApiAndSavesAllLaps(){
        when(lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(List.of());

        LapDto lap1 = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);
        LapDto lap2 = new LapDto(33, 1011, 2, 30.1, 25.8, 30.2, 91.1, false);
        LapDto lap3 = new LapDto(33, 1011, 3, 30.1, 29.8, 31.2, 90.1, true);

        List<LapDto> apiDto = List.of(lap1, lap2, lap3);

        when(lapClient.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(apiDto);

        List<Lap> result =
                lapService.getLapsBySessionKeyAndDriverNumber(1011, 33);

        assertThat(result.getFirst().sessionKey()).isEqualTo(1011);
        assertThat(result.getFirst().driverNumber()).isEqualTo(33);

        verify(lapRepo).saveAll(apiDto);
    }

    @Test
    void getLapsBySessionKeyAndDriverNumber_throwsException_IfLapNotFound(){
        when(lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(List.of());

        when(lapClient.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .thenReturn(List.of());

        assertThatThrownBy(() -> lapService.getLapsBySessionKeyAndDriverNumber(1011, 33))
                .isInstanceOf(LapNotFoundException.class).hasMessageContaining("Laps not found with given parameters: ");
    }
}
