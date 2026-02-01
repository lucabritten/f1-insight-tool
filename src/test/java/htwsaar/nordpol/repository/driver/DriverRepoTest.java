package htwsaar.nordpol.repository.driver;

import htwsaar.nordpol.api.dto.DriverDto;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static com.nordpol.jooq.Tables.DRIVERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DriverRepoTest {

    private Connection connection;
    private DSLContext create;
    private IDriverRepo driverRepo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);
        create.execute("""
            create table Drivers (
                driver_id integer primary key autoincrement,
                first_name text not null,
                last_name text not null,
                team_name text not null
            );
        """);
        create.execute("""
            create table Driver_numbers (
                id integer primary key autoincrement,
                driver_id integer not null,
                start_number integer not null,
                year integer not null,
                meeting_key integer,
                foreign key (driver_id) references Drivers(driver_id) on delete cascade,
                unique (driver_id, year)
            );
        """);

        driverRepo = new JooqDriverRepo(create);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Nested
    @DisplayName("saveOrUpdateDriverForYear")
    class SaveOrUpdateDriverForYear {

        @Test
        void persistsDriver() {
            DriverDto driverData =
                    new DriverDto("Lando", "Norris", 4, "McLaren RACING");

        driverRepo.saveOrUpdateDriverForYear(driverData, 2025, 1111);

        Optional<DriverDto> stored =
                driverRepo.getDriverByFullNameForYear("Lando", "Norris", 2025);

            assertThat(stored).isPresent();

            DriverDto dto = stored.get();

            assertThat(dto.driver_number()).isEqualTo(4);
            assertThat(dto.first_name()).isEqualTo("Lando");
            assertThat(dto.last_name()).isEqualTo("Norris");
            assertThat(dto.team_name()).isEqualTo("McLaren RACING");
        }

        @Test
        void sameDriver_canHaveDifferentNumbersInDifferentYear() {
            DriverDto max2025 =
                    new DriverDto("Max", "Verstappen", 1, "Red Bull Racing");

            DriverDto max2026 =
                    new DriverDto("Max", "Verstappen", 3, "NLD");

            driverRepo.saveOrUpdateDriverForYear(max2025, 2025, 1111);
            driverRepo.saveOrUpdateDriverForYear(max2026, 2026, 2222);

            assertThat(driverRepo
                    .getDriverByStartNumberForYear(1, 2025)).isPresent();

            assertThat(driverRepo
                    .getDriverByStartNumberForYear(3, 2026)).isPresent();
        }

        @Test
        void sameDriver_sameYear_updatesStartNumber() {
            DriverDto max1 =
                    new DriverDto("Max", "Verstappen", 33, "Red Bull Racing");

            DriverDto max2 =
                    new DriverDto("Max", "Verstappen", 1, "Red Bull Racing");

            driverRepo.saveOrUpdateDriverForYear(max1, 2025, 1111);
            driverRepo.saveOrUpdateDriverForYear(max2, 2025, 1112);

            Optional<DriverDto> stored =
                    driverRepo.getDriverByFullNameForYear("Max", "Verstappen", 2025);

            assertThat(stored).isPresent();
            assertThat(stored.get().driver_number()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getDriverByFullNameForYear")
    class GetDriverByFullNameForYear {

        @Test
        void returnsNullWhenMissing() {
            Optional<DriverDto> stored =
                    driverRepo.getDriverByFullNameForYear("Unknown", "Driver", 2025);

            assertThat(stored).isEmpty();
        }

        @Test
        void returnsCorrectDriver_forMultipleEntries() {
            DriverDto lando = new DriverDto("Lando", "Norris", 4, "McLaren RACING");
            DriverDto max = new DriverDto("Max", "Verstappen", 1, "Red Bull Racing");

            driverRepo.saveOrUpdateDriverForYear(lando, 2025, 1111);
            driverRepo.saveOrUpdateDriverForYear(max, 2025, 1111);

            Optional<DriverDto> stored =
                    driverRepo.getDriverByFullNameForYear("Max", "Verstappen", 2025);

            assertThat(stored).isPresent();

            DriverDto dto = stored.get();
            assertThat(dto.driver_number()).isEqualTo(1);
            assertThat(dto.first_name()).isEqualTo("Max");
            assertThat(dto.last_name()).isEqualTo("Verstappen");
            assertThat(dto.team_name()).isEqualTo("Red Bull Racing");
        }
    }

    @Nested
    @DisplayName("Cascade Delete")
    class CascadeDelete {

        @Test
        void deletingDriver_removesDriverNumbers() {
            DriverDto max =
                    new DriverDto("Max", "Verstappen", 1, "Red Bull Racing");

            driverRepo.saveOrUpdateDriverForYear(max, 2025, 1111);

            create.deleteFrom(DRIVERS)
                    .where(DRIVERS.FIRST_NAME.eq("Max"))
                    .execute();

            assertThat(driverRepo.getDriverByStartNumberForYear(1, 2025))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        void throwsException_whenDriverNumberIsNegative() {
            DriverDto driverDto = new DriverDto("Max", "Verstappen", -1, "Red Bull Racing");

            assertThatThrownBy(() -> driverRepo.saveOrUpdateDriverForYear(driverDto, 2025, 1111))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void throwsException_whenFirstnameIsNull() {
            DriverDto driverDto = new DriverDto(null, "Verstappen", 1, "Red Bull Racing");

            assertThatThrownBy(() -> driverRepo.saveOrUpdateDriverForYear(driverDto, 2025, 1111))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void throwsException_whenTeamNameIsNull() {
            DriverDto driverDto = new DriverDto("Max", "Verstappen", 1, null);

            assertThatThrownBy(() -> driverRepo.saveOrUpdateDriverForYear(driverDto, 2025, 1111))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void invalidYear_throwsException() {
            DriverDto max =
                    new DriverDto("Max", "Verstappen", 1, "Red Bull Racing");

            assertThatThrownBy(() ->
                    driverRepo.saveOrUpdateDriverForYear(max, 1900, 1111))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
