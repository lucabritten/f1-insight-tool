package htwsaar.nordpol.cli.converter;

import htwsaar.nordpol.domain.SessionName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SessionNameConverterTest {

    private final SessionNameConverter converter = new SessionNameConverter();

    @Test
    void convert_throwsExceptionForUnknownSessionName() {
        assertThatThrownBy(() -> converter.convert("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convert_throwsExceptionForBlankValue() {
        assertThatThrownBy(() -> converter.convert(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convert_throwsExceptionForNullValue() {
        assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convert_acceptsUntrimmedString() {
        String untrimmedValue = "    Race ";

        SessionName result = converter.convert(untrimmedValue);

        assertThat(result).isEqualTo(SessionName.RACE);
    }

    @Test
    void convert_acceptsDatabaseValue() {
        String dbValue = "Practice 1";

        SessionName result = converter.convert(dbValue);

        assertThat(result).isEqualTo(SessionName.PRACTICE1);
    }

    @Test
    void convert_isCaseInsensitive() {
        String value = "race";

        SessionName result = converter.convert(value);

        assertThat(result).isEqualTo(SessionName.RACE);
    }

    @Test
    void convert_allowsDifferentAliases() {
        String alias1 = "SprintQuali";
        String alias2 = "Sprint Shootout";

        SessionName result1 = converter.convert(alias1);
        SessionName result2 = converter.convert(alias2);

        assertThat(result1).isEqualTo(SessionName.SPRINT_SHOOTOUT);
        assertThat(result2).isEqualTo(SessionName.SPRINT_SHOOTOUT);
    }


}
