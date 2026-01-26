package htwsaar.nordpol.cli;


import htwsaar.nordpol.cli.view.LapsWithContext;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.service.lap.LapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static htwsaar.nordpol.domain.SessionName.RACE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LapCommandTest {

    private LapService mockLapService;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private LapsWithContext sampleLapContext;

    @BeforeEach
    void setup(){
        mockLapService = mock(LapService.class);

        sampleLapContext = new LapsWithContext(
                "Singapore Grand Prix",
                "Max Verstappen",
                RACE,
                List.of(
                new Lap(
                        1,
                        8,
                        9161,
                        27.462,
                        38.938,
                        46.007,
                        112.497,
                        false
                )
        ));

        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown(){
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    public void lapInfo_printsFormattedLaps(){
        when(mockLapService.getLapsByLocationYearSessionNameAndDriverNumber("Singapore Grand Prix",2025,RACE,1))
        .thenReturn(sampleLapContext);

    int exitCode = new CommandLine(
            new LapCommand(mockLapService)
    ).execute("-l", "Singapore Grand Prix", "-y","2025","-sn", "RACE", "-d","1");

    assertThat(exitCode).isZero();
    assertThat(outputStream.toString()).contains("Race");

    }

    @Test
    public void missingDriverNumber_printsErrorMessage(){
        int exitCode = new CommandLine(
                new LapCommand(mockLapService)
        ).execute("-l", "Singapore", "-y","2025","-sn", "RACE");

        assertThat(exitCode).isEqualTo(2);
        assertThat(errorStream.toString()).contains("Missing required option");
    }

    @Test
    public void helpOption_printsUsage(){
        int exitCode = new CommandLine(
                new LapCommand(mockLapService)
        ).execute("--help");

        assertThat(exitCode).isZero();
    }

    @Test
    public void noArguments_printsUsageError(){
        int exitCode = new CommandLine(
                new LapCommand(mockLapService)
        ).execute();
        assertThat(exitCode).isEqualTo(2);
    }
}
