package htwsaar.nordpol.repository.lap;

import htwsaar.nordpol.api.dto.LapDto;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class LapRepoTest {

    private Connection connection;
    private DSLContext create;
    private ILapRepo lapRepo;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);
        create.execute("""
                CREATE TABLE Laps (
                driver_number int NOT NULL,
                lap_number int NOT NULL,
                session_key int NOT NULL,
                duration_sector_1 real NOT NULL,
                duration_sector_2 real NOT NULL,
                duration_sector_3 real NOT NULL,
                lap_duration real NOT NULL,
                is_pit_out_lap int NOT NULL,
                PRIMARY KEY(driver_number, lap_number, session_key)
                );
                """);

        create.execute("""
    INSERT INTO Laps (
        driver_number,
        lap_number,
        session_key,
        duration_sector_1,
        duration_sector_2,
        duration_sector_3,
        lap_duration,
        is_pit_out_lap
    ) VALUES
        -- Fahrer 44, Session 1001
        (44, 1, 1001, 30.1, 29.8, 31.2, 91.1, 0),
        (44, 2, 1001, 29.9, 29.7, 31.0, 90.6, 0),
        (44, 3, 1001, 31.0, 30.5, 32.0, 93.5, 1),

        -- Gleicher Fahrer, andere Session
        (44, 1, 2002, 30.5, 30.0, 31.8, 92.3, 0),

        -- Anderer Fahrer, gleiche Session
        (33, 1, 1001, 29.5, 29.3, 30.9, 89.7, 0),
        
        (4, 1, 3033, 20.1, 20.2, 20.3, 60.6, 1),
        (4, 2, 3033, 20.2, 20.2, 20.3, 60.7, 0),
        (4, 3, 3033, 0, 0, 0, 0, 1),
        
        (4, 1, 9999, 20.1, 20.2, 20.3, 60.6, 1);
""");

        lapRepo = new JooqLapRepo(create);
    }

    @AfterEach
    void tearDown() throws Exception {
        if(connection != null)
            connection.close();
    }

    @Test
    void saveAll_savesMultipleLaps() {
        LapDto lap1 = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);
        LapDto lap2 = new LapDto(33, 1011, 2, 30.1, 25.8, 30.2, 91.1, false);
        LapDto lap3 = new LapDto(33, 1011, 3, 30.1, 29.8, 31.2, 90.1, true);

        List<LapDto> laps = List.of(lap1, lap2, lap3);

        lapRepo.saveAll(laps);

        List<LapDto> stored = lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33);

        assertThat(stored).hasSize(3);
        assertThat(stored.getFirst().session_key()).isEqualTo(1011);
        assertThat(stored.getFirst().is_pit_out_lap()).isTrue();
    }

    @Test
    void saveAll_savesFieldsCorrectly() {
        LapDto lap1 = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);
        LapDto lap2 = new LapDto(33, 1011, 2, 30.1, 25.8, 30.2, 91.1, false);
        LapDto lap3 = new LapDto(33, 1011, 3, 30.1, 29.8, 31.2, 90.1, true);

        List<LapDto> laps = List.of(lap1, lap2, lap3);

        lapRepo.saveAll(laps);

        List<LapDto> stored = lapRepo.getLapsBySessionKeyAndDriverNumber(1011, 33);

        assertThat(stored.size()).isEqualTo(3);
        assertThat(stored.getFirst().driver_number()).isEqualTo(33);
        assertThat(stored.getFirst().session_key()).isEqualTo(1011);
        assertThat(stored.getFirst().lap_number()).isEqualTo(1);
        assertThat(stored.getFirst().duration_sector_1()).isEqualTo(30.1);
        assertThat(stored.getFirst().duration_sector_2()).isEqualTo(29.8);
        assertThat(stored.getFirst().duration_sector_3()).isEqualTo(31.2);
        assertThat(stored.getFirst().lap_duration()).isEqualTo(91.1);
        assertThat(stored.getFirst().is_pit_out_lap()).isTrue();
    }

    @Test
    void saveAll_mapsIsPitOutLapToTrue_when1() {
        LapDto lap = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);

        List<LapDto> laps = List.of(lap);

        lapRepo.saveAll(laps);

        List<LapDto> stored = lapRepo.getLapsBySessionKeyAndDriverNumber(1011,33);

        assertThat(stored).hasSize(1);
        assertThat(stored.getFirst().is_pit_out_lap()).isTrue();
    }

    @Test
    void saveAll_mapsIsPitOutLapToFalse_when0() {
        LapDto lap = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, false);

        List<LapDto> laps = List.of(lap);

        lapRepo.saveAll(laps);

        List<LapDto> stored = lapRepo.getLapsBySessionKeyAndDriverNumber(1011,33);

        assertThat(stored).hasSize(1);
        assertThat(stored.getFirst().is_pit_out_lap()).isFalse();
    }

    @Test
    void saveAll_noException_whenEmptyList() {
        List<LapDto> laps =  List.of();

        assertThatNoException()
                .isThrownBy(() -> lapRepo.saveAll(laps));
    }

    @Test
    void saveAll_throwsException_whenSessionKeyIsNegative() {
        LapDto lap = new LapDto(33, -1, 1, 30.1, 29.8, 31.2, 91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(lap)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SessionKey");
    }

    @Test
    void saveAll_throwsException_whenLapNumberIsNegative() {
        LapDto lap = new LapDto(33, 1011, -1, 30.1, 29.8, 31.2, 91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(lap)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LapNumber");
    }

    @Test
    void saveAll_throwsException_whenDriverNumberIsNegative() {
        LapDto lap = new LapDto(-33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(lap)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DriverNumber");
    }

    @Test
    void saveAll_throwsException_whenDurationSector1IsNegative() {
        LapDto lap = new LapDto(33, 1011, 1, -30.1, 29.8, 31.2, 91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(lap)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DurationSector1");
    }

    @Test
    void saveAll_throwsException_whenDurationSector2IsNegative() {
        LapDto lap = new LapDto(33, 1011, 1, 30.1, -29.8, 31.2, 91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(lap)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DurationSector2");
    }

    @Test
    void saveAll_throwsException_whenDurationSector3IsNegative() {
        LapDto lap = new LapDto(33, 1011, 1, 30.1, 29.8, -31.2, 91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(lap)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DurationSector3");
    }

    @Test
    void saveAll_throwsException_whenLapDurationIsNegative() {
        LapDto lap = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, -91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(lap)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LapDuration");
    }

    @Test
    void saveAll_throwsException_whenOneLapInListIsInvalid() {
        LapDto valid = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, true);
        LapDto invalid = new LapDto(33, 1011, -2, 30.1, 29.8, 31.2, 91.1, true);

        assertThatThrownBy(() -> lapRepo.saveAll(List.of(valid, invalid)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getLapsBySessionKeyAndDriverNumber_returnsAllLaps_relatedToDriverAndSessionKey() {
        List<LapDto> result = lapRepo.getLapsBySessionKeyAndDriverNumber(1001, 44);

        assertThat(result).hasSize(3);
        assertThat(result.getFirst().driver_number()).isEqualTo(44);
        assertThat(result.getFirst().session_key()).isEqualTo(1001);
    }

    @Test
    void getLapsBySessionKeyAndDriverNumber_lapsAreSortedByLapNumber() {
        List<LapDto> result = lapRepo.getLapsBySessionKeyAndDriverNumber(1001, 44);

        assertThat(result).hasSize(3);
        assertThat(result.getFirst().lap_number()).isEqualTo(1);
        assertThat(result.get(1).lap_number()).isEqualTo(2);
        assertThat(result.get(2).lap_number()).isEqualTo(3);
    }

    @Test
    void getLapsBySessionKeyAndDriverNumber_ReturnsEmptyList_whenNoLapExist() {
        List<LapDto> result = lapRepo.getLapsBySessionKeyAndDriverNumber(9999, 1111);

        assertThat(result).isEmpty();
    }

    @Test
    void getFastestLapsBySessionKey_returnsFastestLap_whenLimitIs1() {
        List<LapDto> result = lapRepo.getFastestLapsBySessionKey(1001,1);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().lap_duration()).isEqualTo(89.7);
    }

    @Test
    void getFastestLapsBySessionKey_respectsLimit() {
        List<LapDto> result = lapRepo.getFastestLapsBySessionKey(1001, 0);

        assertThat(result).isEmpty();
    }

    @Test
    void getFastestLapsBySessionKey_returnsAllValidLaps_whenHigherLimitThanLaps() {
        List<LapDto> result = lapRepo.getFastestLapsBySessionKey(3033, 100);

        assertThat(result).hasSize(1);
    }

    @Test
    void getFastestLapsBySessionKey_ignoresPitOutLaps() {
        List<LapDto> result = lapRepo.getFastestLapsBySessionKey(3033, 1);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().lap_duration()).isEqualTo(60.7);
    }

    @Test
    void getFastestLapsBySessionKey_ignoresZeroLapDuration() {
        List<LapDto> result = lapRepo.getFastestLapsBySessionKey(3033, 3);

        assertThat(result).hasSize(1);
    }

    @Test
    void getFastestLapsBySessionKey_returnsEmptyList_whenNoValidLapExists() {
        List<LapDto> result = lapRepo.getFastestLapsBySessionKey(9999, 1);

        assertThat(result).isEmpty();
    }

    @Test
    void getFastestLapsBySessionKey_doesNotMixSessions() {
        List<LapDto> result = lapRepo.getFastestLapsBySessionKey(1001, 5);

        assertThat(result).hasSize(3);
        assertThat(result.get(2).lap_duration()).isEqualTo(91.1);
    }
}
