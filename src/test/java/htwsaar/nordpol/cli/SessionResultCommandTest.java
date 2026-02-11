package htwsaar.nordpol.cli;

import htwsaar.nordpol.presentation.cli.SessionResultCommand;
import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.exception.MeetingNotFoundException;
import htwsaar.nordpol.service.sessionResult.ISessionResultService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static htwsaar.nordpol.domain.SessionName.RACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SessionResultCommandTest {

    private ISessionResultService mockSessionResultService;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private SessionResultWithContext sampleContext;

    private static final int ILLEGAL_ARG_ERROR = 2;
    private static final int BUSINESS_LOGIC_ERROR = 1;

    @BeforeEach
    void setup() {
        mockSessionResultService = mock(ISessionResultService.class);

        sampleContext = new SessionResultWithContext(
                "Austin",
                SessionName.RACE,
                List.of(
                        new SessionResult(44, 1, List.of("+0.000"), List.of(5400.0), false, false, false),
                        new SessionResult(1, 2, List.of("+1.234"), List.of(5401.234), false, false, false)
                )
        );

        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();

        originalOut = System.out;
        originalErr = System.err;

        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        void missingRequiredOptions_returnsError() {
            int exitCode = new CommandLine(new SessionResultCommand(mockSessionResultService))
                    .execute();

            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
            assertThat(errorStream.toString()).containsIgnoringCase("Missing required option");
        }

        @Test
        void invalidParam_catchesDataNotFoundException() {
            when(mockSessionResultService.getResultByLocationYearAndSessionType("Saarbrücken", 2024, RACE))
                    .thenThrow(MeetingNotFoundException.class);

            int exitCode = new CommandLine(
                    new SessionResultCommand(mockSessionResultService)
            ).execute("-l", "Saarbrücken", "-y", "2024", "-s", "RACE");

            AssertionsForClassTypes.assertThat(errorStream.toString()).contains("Use --help");
            AssertionsForClassTypes.assertThat(exitCode).isEqualTo(2);
        }

    }

    @Nested
    @DisplayName("Output Validation")
    class OutputValidation{

        @Test
        void sessionResult_printsFormattedOutput_andReturnsZero() {
            when(mockSessionResultService.getResultByLocationYearAndSessionType(
                    "Austin", 2024, SessionName.RACE
            )).thenReturn(sampleContext);

            int exitCode = new CommandLine(new SessionResultCommand(mockSessionResultService))
                    .execute("-l", "Austin", "-y", "2024", "-s", "RACE");

            assertThat(exitCode).isZero();
            assertThat(errorStream.toString()).isBlank();
            assertThat(outputStream.toString()).isNotBlank();

            verify(mockSessionResultService, times(1))
                    .getResultByLocationYearAndSessionType("Austin", 2024, SessionName.RACE);
        }

        @Test
        void serviceThrowsException_printsMessage_andReturnsTwo() {
            when(mockSessionResultService.getResultByLocationYearAndSessionType(
                    "Monza", 2024, SessionName.QUALIFYING
            )).thenThrow(new RuntimeException("Database down"));

            int exitCode = new CommandLine(new SessionResultCommand(mockSessionResultService))
                    .execute("-l", "Monza", "-y", "2024", "-s", "QUALIFYING");

            assertThat(exitCode).isEqualTo(BUSINESS_LOGIC_ERROR);
            assertThat(outputStream.toString()).isBlank();
            assertThat(errorStream.toString()).contains("Database down");

            verify(mockSessionResultService, times(1))
                    .getResultByLocationYearAndSessionType("Monza", 2024, SessionName.QUALIFYING);
        }
    }
}
