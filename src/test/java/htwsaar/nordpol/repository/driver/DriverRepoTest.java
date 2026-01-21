package htwsaar.nordpol.repository.driver;

import htwsaar.nordpol.api.dto.DriverDto;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    private IDriverRepo IDriverRepo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);
        create.execute("""
            create table Drivers (
                driver_id integer primary key autoincrement,
                first_name text not null,
                last_name text not null,
                country_code text not null
            );
        """);
        create.execute("""
            create table Driver_numbers (
                id integer primary key autoincrement,
                driver_id integer not null,
                start_number integer not null,
                season integer not null,
                foreign key (driver_id) references Drivers(driver_id) on delete cascade,
                unique (driver_id, season)
            );
        """);

        IDriverRepo = new JooqDriverRepo(create);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void saveDriver_persistsDriver() {
        DriverDto driverData =
                new DriverDto("Lando", "Norris", 4, "GBR");

        IDriverRepo.saveOrUpdateDriverForSeason(driverData, 2025);

        Optional<DriverDto> stored =
                IDriverRepo.getDriverByFullNameForSeason("Lando", "Norris", 2025);

        assertThat(stored).isPresent();

        DriverDto dto = stored.get();

        assertThat(dto.driver_number()).isEqualTo(4);
        assertThat(dto.first_name()).isEqualTo("Lando");
        assertThat(dto.last_name()).isEqualTo("Norris");
        assertThat(dto.country_code()).isEqualTo("GBR");
    }

    @Test
    void getDriverByFullName_returnsNullWhenMissing() {
        Optional<DriverDto> stored =
                IDriverRepo.getDriverByFullNameForSeason("Unknown", "Driver", 2025);

        assertThat(stored).isEmpty();
    }

    @Test
    void getDriverByFullName_returnsCorrectDriver_forMultipleEntries() {
        DriverDto lando = new DriverDto("Lando", "Norris", 4, "GBR");
        DriverDto max = new DriverDto("Max", "Verstappen", 1, "NLD");

        IDriverRepo.saveOrUpdateDriverForSeason(lando, 2025);
        IDriverRepo.saveOrUpdateDriverForSeason(max, 2025);

        Optional<DriverDto> stored =
                IDriverRepo.getDriverByFullNameForSeason("Max", "Verstappen", 2025);

        assertThat(stored).isPresent();

        DriverDto dto = stored.get();
        assertThat(dto.driver_number()).isEqualTo(1);
        assertThat(dto.first_name()).isEqualTo("Max");
        assertThat(dto.last_name()).isEqualTo("Verstappen");
        assertThat(dto.country_code()).isEqualTo("NLD");
    }

    @Test
    void saveDriver_throwsException_whenDriverNumberIsNegative(){
        DriverDto driverDto = new DriverDto("Max", "Verstappen", -1, "NED");

        assertThatThrownBy(() -> IDriverRepo.saveOrUpdateDriverForSeason(driverDto, 2025))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveDriver_throwsException_whenFirstnameIsNull(){
        DriverDto driverDto = new DriverDto(null, "Verstappen", 1, "NED");

        assertThatThrownBy(() -> IDriverRepo.saveOrUpdateDriverForSeason(driverDto, 2025))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveDriver_throwsException_whenCountryCodeIsNull(){
        DriverDto driverDto = new DriverDto("Max", "Verstappen", 1, null);

        assertThatThrownBy(() -> IDriverRepo.saveOrUpdateDriverForSeason(driverDto, 2025))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sameDriver_canHaveDifferentNumbersInDifferentSeasons() {
        DriverDto max2025 =
                new DriverDto("Max", "Verstappen", 1, "NLD");

        DriverDto max2026 =
                new DriverDto("Max", "Verstappen", 3, "NLD");

        IDriverRepo.saveOrUpdateDriverForSeason(max2025, 2025);
        IDriverRepo.saveOrUpdateDriverForSeason(max2026, 2026);

        assertThat(IDriverRepo
                .getDriverByStartNumberForSeason(1, 2025)).isPresent();

        assertThat(IDriverRepo
                .getDriverByStartNumberForSeason(3, 2026)).isPresent();
    }

    @Test
    void sameDriver_sameSeason_updatesStartNumber(){
        DriverDto max1 =
                new DriverDto("Max", "Verstappen", 33, "NLD");

        DriverDto max2 =
                new DriverDto("Max", "Verstappen", 1, "NLD");

        IDriverRepo.saveOrUpdateDriverForSeason(max1, 2025);
        IDriverRepo.saveOrUpdateDriverForSeason(max2, 2025);

        Optional<DriverDto> stored =
                IDriverRepo.getDriverByFullNameForSeason("Max", "Verstappen", 2025);

        assertThat(stored).isPresent();
        assertThat(stored.get().driver_number()).isEqualTo(1);
    }

    @Test
    void deletingDriver_removesDriverNumbers() {
        DriverDto max =
                new DriverDto("Max", "Verstappen", 1, "NLD");

        IDriverRepo.saveOrUpdateDriverForSeason(max, 2025);

        create.deleteFrom(DRIVERS)
                .where(DRIVERS.FIRST_NAME.eq("Max"))
                .execute();

        assertThat(IDriverRepo.getDriverByStartNumberForSeason(1, 2025))
                .isEmpty();
    }

    @Test
    void invalidYear_throwsException(){
        DriverDto max =
                new DriverDto("Max", "Verstappen", 1, "NLD");

        assertThatThrownBy(() ->
                IDriverRepo.saveOrUpdateDriverForSeason(max, 1900))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
