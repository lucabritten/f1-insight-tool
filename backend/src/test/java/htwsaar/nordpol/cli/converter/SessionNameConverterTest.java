package htwsaar.nordpol.cli.converter;

import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.presentation.cli.converter.SessionNameConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SessionNameConverterTest {

    private final SessionNameConverter converter = new SessionNameConverter();

    @Nested
    @DisplayName("Excptionhandling")
    class Excptionhandling {

        @Test
        void throwsExceptionForUnknownSessionName() {
            assertThatThrownBy(() -> converter.convert("unknown"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void throwsExceptionForBlankValue() {
            assertThatThrownBy(() -> converter.convert(" "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void throwsExceptionForNullValue() {
            assertThatThrownBy(() -> converter.convert(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("Input")
    class Input {

        @Test
        void acceptsUntrimmedString() {
            String untrimmedValue = "    Race ";

            SessionName result = converter.convert(untrimmedValue);

            assertThat(result).isEqualTo(SessionName.RACE);
        }

        @Test
        void acceptsDatabaseValue() {
            String dbValue = "Practice 1";

            SessionName result = converter.convert(dbValue);

            assertThat(result).isEqualTo(SessionName.PRACTICE1);
        }

        @Test
        void isCaseInsensitive() {
            String value = "race";

            SessionName result = converter.convert(value);

            assertThat(result).isEqualTo(SessionName.RACE);
        }

        @Test
        void allowsDifferentAliases() {
            String alias1 = "SprintQuali";
            String alias2 = "Sprint Shootout";

            SessionName result1 = converter.convert(alias1);
            SessionName result2 = converter.convert(alias2);

            assertThat(result1).isEqualTo(SessionName.SPRINT_SHOOTOUT);
            assertThat(result2).isEqualTo(SessionName.SPRINT_SHOOTOUT);
        }


    }



}
