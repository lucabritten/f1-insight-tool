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

    //
    //

}
