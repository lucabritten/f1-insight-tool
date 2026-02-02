package htwsaar.nordpol.cli;

import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionReport;
import htwsaar.nordpol.exception.DataNotFoundException;
import htwsaar.nordpol.service.report.SessionReportService;
import htwsaar.nordpol.util.rendering.SessionReportRenderer;
import me.tongfei.progressbar.ProgressBar;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SessionReportCommandTest {

    private SessionReportService sessionReportService;
    private SessionReportRenderer renderer;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private MockedStatic<ApplicationContext> applicationContextMock;

    private static final int ILLEGAL_ARG_ERROR = 2;
    private static final int BUSINESS_LOGIC_ERROR = 1;


    @BeforeEach
    void setup() {
        sessionReportService = mock(SessionReportService.class);
        renderer = mock(SessionReportRenderer.class);
        ProgressBar progressBar = mock(ProgressBar.class);
        applicationContextMock = mockStatic(ApplicationContext.class);
        applicationContextMock.when(ApplicationContext::progressBar).thenReturn(progressBar);

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

        if (applicationContextMock != null) {
            applicationContextMock.close();
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        void serviceDataNotFound_causesExitCode2_andDoesNotRender() {

            when(sessionReportService.buildReport(anyString(), anyInt(), any(), any(), any()))
                    .thenThrow(new DataNotFoundException("no data"));

            SessionReportCommand cmd = new SessionReportCommand(sessionReportService, renderer);


            int exitCode = new CommandLine(cmd)
                    .execute("-l", "Monza", "-y", "2024", "-s", "Race");


            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
            verifyNoInteractions(renderer);
            assertThat(errorStream.toString())
                    .contains("Requested data not found")
                    .contains("Use --help");
        }

        @Test
        void unexpectedServiceException_causesExitCode1_andDoesNotRender() {

            when(sessionReportService.buildReport(anyString(), anyInt(), any(), any(), any()))
                    .thenThrow(new RuntimeException("boom"));

            SessionReportCommand cmd = new SessionReportCommand(sessionReportService, renderer);


            int exitCode = new CommandLine(cmd)
                    .execute("-l", "Monza", "-y", "2024", "-s", "Race");


            assertThat(exitCode).isEqualTo(BUSINESS_LOGIC_ERROR);
            verifyNoInteractions(renderer);
            assertThat(errorStream.toString()).contains("Unexpected error: boom");
        }

        @Test
        void missingRequiredLocation_causesUsageError() {

            int exitCode = new CommandLine(new SessionReportCommand(sessionReportService, renderer))
                    .execute("-y", "2024", "-s", "Race");


            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
            assertThat(errorStream.toString()).containsIgnoringCase("Missing required option");
            verifyNoInteractions(renderer);
        }

        @Test
        void invalidSessionName_causesUsageError() {

            int exitCode = new CommandLine(new SessionReportCommand(sessionReportService, renderer))
                    .execute("-l", "Monza", "-y", "2024", "-s", "NOT_A_SESSION");


            assertThat(exitCode).isEqualTo(ILLEGAL_ARG_ERROR);
            assertThat(errorStream.toString()).isNotBlank();
            verifyNoInteractions(renderer);
        }

        @Test
        void helpOption_printsUsage_andExitCode0() {

            int exitCode = new CommandLine(new SessionReportCommand(sessionReportService, renderer))
                    .execute("--help");


            assertThat(exitCode).isZero();
            assertThat(outputStream.toString()).contains("session-report");
        }

    }

    @Nested
    @DisplayName("Output Validation")
    class OutputValidation {

        @Test
        void sessionReport_buildsReportAndRendersPdf() {

            SessionReport report = mock(SessionReport.class);
            when(sessionReportService.buildReport(eq("Monza"), eq(2024), eq(SessionName.RACE), isNull(), any()))
                    .thenReturn(report);

            SessionReportCommand cmd = new SessionReportCommand(sessionReportService, renderer);


            int exitCode = new CommandLine(cmd)
                    .execute("-l", "Monza", "-y", "2024", "-s", "Race", "-o", "reports/monza-race.pdf");


            assertThat(exitCode).isZero();
            assertThat(errorStream.toString()).isBlank();

            ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
            verify(renderer, times(1)).render(eq(report), pathCaptor.capture());
            String path = Paths.get("reports", "monza-race.pdf").toString();
            assertThat(pathCaptor.getValue().toString()).endsWith(path);
        }

    }

    @Nested
    @DisplayName("Default Fallback")
    class DefaultFallback {

        @Test
        void whenNoYearGiven_appliesDefaultYear() {

            int currentYear = Year.now().getValue();
            SessionReport report = mock(SessionReport.class);
            when(sessionReportService.buildReport(eq("Austin"), eq(currentYear), eq(SessionName.QUALIFYING), isNull(), any()))
                    .thenReturn(report);

            SessionReportCommand cmd = new SessionReportCommand(sessionReportService, renderer);


            int exitCode = new CommandLine(cmd)
                    .execute("-l", "Austin", "-s", "Qualifying", "-o", "reports/austin-quali.pdf");


            assertThat(exitCode).isZero();
            verify(sessionReportService, times(1))
                    .buildReport(eq("Austin"), eq(currentYear), eq(SessionName.QUALIFYING), isNull(), any());
        }

        @Test
        void whenNoOutputGiven_usesDefaultReportsPathWithSlugifiedName() {

            SessionReport report = mock(SessionReport.class);
            when(sessionReportService.buildReport(eq("Las Vegas"), eq(2024), eq(SessionName.QUALIFYING), isNull(), any()))
                    .thenReturn(report);

            SessionReportCommand cmd = new SessionReportCommand(sessionReportService, renderer);


            int exitCode = new CommandLine(cmd)
                    .execute("-l", "Las Vegas", "-y", "2024", "-s", "Qualifying");


            assertThat(exitCode).isZero();

            ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
            verify(renderer, times(1)).render(eq(report), pathCaptor.capture());
            String path = Paths.get("reports", "session-report-las-vegas-2024-qualifying.pdf").toString();
            assertThat(pathCaptor.getValue().toString())
                    .endsWith(path);
        }

    }
}
