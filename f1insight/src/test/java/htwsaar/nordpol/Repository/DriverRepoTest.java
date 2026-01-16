package htwsaar.nordpol.Repository;

import com.nordpol.jooq.tables.Drivers;
import htwsaar.nordpol.API.DTO.DriverApiDto;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DriverRepoTest {

    private Connection connection;
    private DSLContext create;
    private DriverRepo driverRepo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);
        create.execute("""
                create table Drivers (
                    driver_number integer primary key,
                    first_name text not null,
                    last_name text not null,
                    country_code text not null
                )
                """);
        driverRepo = new JooqDriverRepo(create);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void saveDriver_persistsDriver() {
        DriverApiDto driverData = new DriverApiDto("Lando", "Norris", 4, "GBR");

        driverRepo.saveDriver(driverData);

        DriverApiDto stored = driverRepo.getDriverByFullname("Lando", "Norris");

        assertThat(stored).isNotNull();
        assertThat(stored.driver_number()).isEqualTo(4);
        assertThat(stored.first_name()).isEqualTo("Lando");
        assertThat(stored.last_name()).isEqualTo("Norris");
        assertThat(stored.country_code()).isEqualTo("GBR");
    }

    @Test
    void getDriverByFullname_returnsNullWhenMissing() {
        DriverApiDto stored = driverRepo.getDriverByFullname("Unknown", "Driver");

        assertThat(stored).isNull();
    }

    @Test
    void getDriverByFullname_returnsCorrectDriver_forMultipleEntries() {
        DriverApiDto lando = new DriverApiDto("Lando", "Norris", 4, "GBR");
        DriverApiDto max = new DriverApiDto("Max", "Verstappen", 1, "NLD");

        driverRepo.saveDriver(lando);
        driverRepo.saveDriver(max);

        DriverApiDto stored = driverRepo.getDriverByFullname("Max", "Verstappen");
        assertThat(stored).isNotNull();
        assertThat(stored.driver_number()).isEqualTo(1);
        assertThat(stored.first_name()).isEqualTo("Max");
        assertThat(stored.last_name()).isEqualTo("Verstappen");
        assertThat(stored.country_code()).isEqualTo("NLD");
    }

    @Test
    void saveDriver_throwsException_whenDriverNumberIsNegative(){
        DriverApiDto driverApiDto = new DriverApiDto("Max", "Verstappen", -1, "NED");

        assertThatThrownBy(() -> driverRepo.saveDriver(driverApiDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveDriver_throwsException_whenFirstnameIsNull(){
        DriverApiDto driverApiDto = new DriverApiDto(null, "Verstappen", 1, "NED");

        assertThatThrownBy(() -> driverRepo.saveDriver(driverApiDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveDriver_throwsException_whenCountryCodeIsNull(){
        DriverApiDto driverApiDto = new DriverApiDto("Max", "Verstappen", 1, null);

        assertThatThrownBy(() -> driverRepo.saveDriver(driverApiDto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
