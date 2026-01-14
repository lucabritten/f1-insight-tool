package htwsaar.nordpol.Repository;

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
                    full_name text not null,
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
        DriverApiDto driverData = new DriverApiDto("Lando NORRIS", 4, "GBR");

        driverRepo.saveDriver(driverData);

        DriverApiDto stored = driverRepo.getDriverByFullname("Lando", "NORRIS");
        assertThat(stored).isNotNull();
        assertThat(stored.driver_number()).isEqualTo(4);
        assertThat(stored.full_name()).isEqualTo("Lando NORRIS");
        assertThat(stored.country_code()).isEqualTo("GBR");
    }

    @Test
    void getDriverByFullname_returnsNullWhenMissing() {
        DriverApiDto stored = driverRepo.getDriverByFullname("Unknown", "Driver");

        assertThat(stored).isNull();
    }

    @Test
    void getDriverByFullname_returnsCorrectDriver_forMultipleEntries() {
        DriverApiDto lando = new DriverApiDto("Lando NORRIS", 4, "GBR");
        DriverApiDto max = new DriverApiDto("Max VERSTAPPEN", 1, "NLD");

        driverRepo.saveDriver(lando);
        driverRepo.saveDriver(max);

        DriverApiDto stored = driverRepo.getDriverByFullname("Max", "VERSTAPPEN");
        assertThat(stored).isNotNull();
        assertThat(stored.driver_number()).isEqualTo(1);
        assertThat(stored.full_name()).isEqualTo("Max VERSTAPPEN");
        assertThat(stored.country_code()).isEqualTo("NLD");
    }

    @Test
    void saveDriver_throwsException_whenDriverNumberIsNegative(){
        DriverApiDto driverApiDto = new DriverApiDto("Max VERSTAPPEN", -1, "NED");

        assertThatThrownBy(() -> driverRepo.saveDriver(driverApiDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveDriver_throwsException_whenFullnameIsNull(){
        DriverApiDto driverApiDto = new DriverApiDto(null, 1, "NED");

        assertThatThrownBy(() -> driverRepo.saveDriver(driverApiDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveDriver_throwsException_whenCountryCodeIsNull(){
        DriverApiDto driverApiDto = new DriverApiDto("Max VERSTAPPEN", 1, null);

        assertThatThrownBy(() -> driverRepo.saveDriver(driverApiDto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
