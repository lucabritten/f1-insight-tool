package htwsaar.nordpol.util;

import htwsaar.nordpol.presentation.view.SessionResultWithContext;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.SessionResult;
import htwsaar.nordpol.util.formatting.CliFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CliFormatterTest {

    @Nested
    @DisplayName("Qualifying Formatting")
    class QualifyingFormatting {

        @Test
        void formatsQualifyingWithQ1Q2Q3AndGap() {
            SessionResult r1 = new SessionResult(
                    1,
                    "Verstappen",
                    1,
                    List.of("0.0", "0.0", "0.0"),
                    List.of(93.2, 92.7, 92.5),
                    false,
                    false,
                    false
            );

            SessionResult r2 = new SessionResult(
                    16,
                    "Leclerc",
                    2,
                    List.of("0.3", "0.3"),
                    List.of(93.5, 92.9),
                    false,
                    false,
                    false
            );

            SessionResultWithContext context = new SessionResultWithContext(
                    "United States Grand Prix",
                    SessionName.QUALIFYING,
                    List.of(r1, r2)
            );

            String output = CliFormatter.formatSessionResults(context);

            assertThat(output).contains("Q1(s)");
            assertThat(output).contains("Q2(s)");
            assertThat(output).contains("Q3(s)");
            assertThat(output).contains("+0.0");
            assertThat(output).contains("+0.3");
        }

        @Test
        void qualifyingGapFallsBackFromQ3ToQ2ToQ1() {
            SessionResult r1 = new SessionResult(
                    1,
                    "Verstappen",
                    1,
                    List.of("0.0", "0.0", "0.0"),
                    List.of(93.2, 92.7, 92.5),
                    false,
                    false,
                    false
            );

            SessionResultWithContext context = new SessionResultWithContext(
                    "Austin",
                    SessionName.QUALIFYING,
                    List.of(r1)
            );

            String output = CliFormatter.formatSessionResults(context);

            assertThat(output).contains("+0.0");
        }
    }

    @Nested
    @DisplayName("Race Formatting")
    class RaceFormatting {

        @Test
        void formatsRaceWithSingleGapColumn() {
            SessionResult r1 = new SessionResult(
                    1,
                    "Verstappen",
                    1,
                    List.of("0.0", "0.0", "0.0"),
                    List.of(93.2, 92.7, 92.5),
                    false,
                    false,
                    false
            );

            SessionResultWithContext context = new SessionResultWithContext(
                    "Monza",
                    SessionName.RACE,
                    List.of(r1)
            );

            String output = CliFormatter.formatSessionResults(context);

            assertThat(output).contains("Gap");
            assertThat(output).doesNotContain("Q1(s)");
            assertThat(output).contains("+0.0");
        }
    }

    @Nested
    @DisplayName("Status Formatting")
    class StatusFormatting {

        @Test
        void formatsDnfAsLastPositionWithStatus() {
            SessionResult r1 = new SessionResult(
                    1,
                    "Verstappen",
                    1,
                    List.of("0.0"),
                    List.of(92.1),
                    false,
                    false,
                    false
            );

            SessionResult r2 = new SessionResult(
                    0,
                    "Unknown",
                    6,
                    List.of(),
                    List.of(),
                    true,
                    false,
                    false
            );

            SessionResultWithContext context = new SessionResultWithContext(
                    "Austin",
                    SessionName.RACE,
                    List.of(r1, r2)
            );

            String output = CliFormatter.formatSessionResults(context);

            assertThat(output).contains("6");
            assertThat(output).contains("DNF");
        }
    }
}
