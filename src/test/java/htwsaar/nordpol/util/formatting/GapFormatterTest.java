package htwsaar.nordpol.util.formatting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

public class GapFormatterTest {

    private final GapFormatter gapFormatter = new GapFormatter();

    @Nested
    @DisplayName("Missing Characters")
    class MissingCharacters {

        @Test
        void dashOnMissingGap() {
            assertThat(gapFormatter.gap(null, false, false, false)).isEqualTo("-");
            assertThat(gapFormatter.gap(List.of(), false, false, false)).isEqualTo("-");
            assertThat(gapFormatter.gap(List.of("  "), false, false, false)).isEqualTo("-");
        }

    }

    @Nested
    @DisplayName("Sequence Check")
    class SequenceCheck {

        @Test
        void returnsStatusCodeFirst() {
            assertThat(gapFormatter.gap(List.of("+0.101"), true, false, false)).isEqualTo("DSQ");
            assertThat(gapFormatter.gap(List.of("+0.101"), false, true, false)).isEqualTo("DNS");
            assertThat(gapFormatter.gap(List.of("+0.101"), false, false, true)).isEqualTo("DNF");
        }

    }

    @Nested
    @DisplayName("Normalization")
    class Normalization {

        @Test
        void normalizesSignAndTrims_usesLastNonBlank() {
            assertThat(gapFormatter.gap(List.of("+0.100", " ", " 0.050"), false, false, false)).isEqualTo("+0.050");
            assertThat(gapFormatter.gap(List.of("+0.100"), false, false, false)).isEqualTo("+0.100");
        }

    }
}