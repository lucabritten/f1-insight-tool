package htwsaar.nordpol.repository.driver;

import htwsaar.nordpol.api.dto.DriverDto;
import org.jooq.DSLContext;

import java.util.Optional;

import static com.nordpol.jooq.tables.Drivers.*;
import static com.nordpol.jooq.tables.DriverNumbers.*;

/**
 * jOOQ-based repository for persisting and retrieving drivers
 * from the local SQLite database.
 */
public class JooqDriverRepo implements IDriverRepo {

    private final DSLContext create;
    private final static int FIRST_F1_YEAR = 1950;

    public JooqDriverRepo(DSLContext create) {
        this.create = create;
    }

    @Override
    public void saveOrUpdateDriverForYear(DriverDto driverDto, int year, int meetingKey) {
        validateDriver(driverDto);
        validateYear(year);
        Integer driverId = create
                .select(DRIVERS.DRIVER_ID)
                .from(DRIVERS)
                .where(DRIVERS.FIRST_NAME.eq(driverDto.first_name())
                        .and(DRIVERS.LAST_NAME.eq(driverDto.last_name())))
                .fetchOneInto(Integer.class);

        if(driverId == null){
            driverId = create.insertInto(
                            DRIVERS,
                            DRIVERS.FIRST_NAME,
                            DRIVERS.LAST_NAME,
                            DRIVERS.TEAM_NAME)
                    .values(driverDto.first_name(),
                            driverDto.last_name(),
                            driverDto.team_name()
                    )
                    .returningResult(DRIVERS.DRIVER_ID)
                    .fetchOne(0, Integer.class);
        }

        var existing = create
                .select(DRIVER_NUMBERS.START_NUMBER)
                .from(DRIVER_NUMBERS)
                .where(DRIVER_NUMBERS.DRIVER_ID.eq(driverId)
                        .and(DRIVER_NUMBERS.YEAR.eq(year)))
                .fetchOneInto(Integer.class);

        if(existing == null) {
            create.insertInto(
                    DRIVER_NUMBERS,
                    DRIVER_NUMBERS.DRIVER_ID,
                    DRIVER_NUMBERS.YEAR,
                    DRIVER_NUMBERS.START_NUMBER,
                    DRIVER_NUMBERS.MEETING_KEY)
                    .values(driverId, year, driverDto.driver_number(), meetingKey)
                    .execute();

        } else if (existing != driverDto.driver_number()) {
            create.update(DRIVER_NUMBERS)
                    .set(DRIVER_NUMBERS.START_NUMBER, driverDto.driver_number())
                    .set(DRIVER_NUMBERS.MEETING_KEY, meetingKey)
                    .where(DRIVER_NUMBERS.DRIVER_ID.eq(driverId)
                            .and(DRIVER_NUMBERS.YEAR.eq(year)))
                    .execute();
        }
    }

    private void validateDriver(DriverDto driverDto) {
        if (driverDto == null)
            throw new IllegalArgumentException("driverApiDto must not be null.");

        if (driverDto.driver_number() <= 0)
            throw new IllegalArgumentException("driver_number must be positive.");

        if (driverDto.first_name() == null || driverDto.first_name().isBlank())
            throw new IllegalArgumentException("full_name must not be null or blank.");

        if (driverDto.last_name() == null || driverDto.last_name().isBlank())
            throw new IllegalArgumentException("last_name must not be null or blank.");

        if (driverDto.team_name() == null || driverDto.team_name().isBlank())
            throw new IllegalArgumentException("team_name must not be null or blank.");
    }

    private void validateYear(int year){
        if (year < FIRST_F1_YEAR)
            throw new IllegalArgumentException("Year must be 1950 or younger");
    }

    @Override
    public Optional<DriverDto> getDriverByFullNameForYear(String firstName, String lastName, int year) {
        var record = create.select(DRIVERS.FIRST_NAME, DRIVERS.LAST_NAME, DRIVER_NUMBERS.START_NUMBER.as("driver_number"), DRIVERS.TEAM_NAME)
                .from(DRIVERS)
                .join(DRIVER_NUMBERS)
                    .on(DRIVER_NUMBERS.DRIVER_ID.eq(DRIVERS.DRIVER_ID))
                .where(DRIVERS.FIRST_NAME.eq(firstName)
                        .and(DRIVERS.LAST_NAME.eq(lastName))
                        .and(DRIVER_NUMBERS.YEAR.eq(year)))
                .fetchOneInto(DriverDto.class);

        return Optional.ofNullable(record);
    }

    @Override
    public Optional<DriverDto> getDriverByStartNumberForYear(int startNumber, int year) {
        var record = create.select(
                        DRIVERS.FIRST_NAME, DRIVERS.LAST_NAME,
                        DRIVER_NUMBERS.START_NUMBER.as("driver_number"),
                        DRIVERS.TEAM_NAME)
                .from(DRIVER_NUMBERS)
                .join(DRIVERS)
                .on(DRIVER_NUMBERS.DRIVER_ID.eq(DRIVERS.DRIVER_ID))
                .where(DRIVER_NUMBERS.YEAR.eq(year)
                        .and(DRIVER_NUMBERS.START_NUMBER.eq(startNumber)))
                .fetchOneInto(DriverDto.class);

        return Optional.ofNullable(record);
    }
}
