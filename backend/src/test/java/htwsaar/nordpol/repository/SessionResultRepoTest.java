
package htwsaar.nordpol.repository;

import htwsaar.nordpol.dto.SessionResultDto;
import htwsaar.nordpol.repository.sessionresult.JooqSessionResultRepo;
import htwsaar.nordpol.testutil.SqlSchemaLoader;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionResultRepoTest {

    private DSLContext create;
    private JooqSessionResultRepo repo;

    @BeforeEach
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);

        SqlSchemaLoader.loadSchema(create, "schema.sql");

        repo = new JooqSessionResultRepo(create);
    }

    @Nested
    @DisplayName("saveAll and getSessionResultBySessionKey")
    class SaveAndGet {

        @Test
        void roundtripWorks() {
            SessionResultDto dto = new SessionResultDto(
                    9640,
                    List.of("0.0", "0.2", "0.3"),
                    16,
                    false,
                    false,
                    false,
                    List.of(93.5, 92.8, 92.6),
                    3
            );

            repo.saveAll(List.of(dto));
            List<SessionResultDto> results = repo.getSessionResultBySessionKey(9640);

            assertThat(results).hasSize(1);

            SessionResultDto stored = results.getFirst();
            assertThat(stored.session_key()).isEqualTo(9640);
            assertThat(stored.driver_number()).isEqualTo(16);
            assertThat(stored.position()).isEqualTo(3);
            assertThat(stored.dnf()).isFalse();
            assertThat(stored.dns()).isFalse();
            assertThat(stored.dsq()).isFalse();
            assertThat(stored.duration()).containsExactly(93.5, 92.8, 92.6);
            assertThat(stored.gap_to_leader()).containsExactly("0.0", "0.2", "0.3");
        }

        @Test
        void returnsEmptyList_whenNoResultsExist() {
            List<SessionResultDto> results = repo.getSessionResultBySessionKey(9999);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandling {

        @Test
        void handlesNullQualifyingValuesCorrectly() {
            SessionResultDto dto = new SessionResultDto(
                    9641,
                    List.of(),
                    44,
                    true,
                    false,
                    false,
                    List.of(94.2),
                    0
            );

            repo.saveAll(List.of(dto));
            List<SessionResultDto> results = repo.getSessionResultBySessionKey(9641);

            assertThat(results).hasSize(1);
            SessionResultDto stored = results.getFirst();

            assertThat(stored.dnf()).isTrue();
            assertThat(stored.gap_to_leader()).containsExactly(null, null, null);
            assertThat(stored.duration()).containsExactly(94.2, null, null);
        }
    }
}
