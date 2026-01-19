package htwsaar.nordpol.Repository;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import org.jooq.DSLContext;

import java.util.Optional;

import static com.nordpol.jooq.tables.Drivers.*;
import static com.nordpol.jooq.tables.DriverNumbers.*;

/**
 * jOOQ-based repository for persisting and retrieving drivers
 * from the local SQLite database.
 */
public class JooqDriverRepo implements DriverRepo{

    private final DSLContext create;
    private final static int FIRST_F1_SEASON = 1950;

    public JooqDriverRepo(DSLContext create) {
        this.create = create;
    }

    @Override
    public void saveOrUpdateDriverForSeason(DriverApiDto driverApiDto, int season) {
        validateDriver(driverApiDto);
        validateSeason(season);
        Integer driverId = create
                .select(DRIVERS.DRIVER_ID)
                .from(DRIVERS)
                .where(DRIVERS.FIRST_NAME.eq(driverApiDto.first_name())
                        .and(DRIVERS.LAST_NAME.eq(driverApiDto.last_name())))
                .fetchOneInto(Integer.class);

        if(driverId == null){
            driverId = create.insertInto(
                            DRIVERS,
                            DRIVERS.FIRST_NAME,
                            DRIVERS.LAST_NAME,
                            DRIVERS.COUNTRY_CODE)
                    .values(driverApiDto.first_name(),
                            driverApiDto.last_name(),
                            driverApiDto.country_code()
                    )
                    .returningResult(DRIVERS.DRIVER_ID)
                    .fetchOne(0, Integer.class);
        }

        var existing = create
                .select(DRIVER_NUMBERS.START_NUMBER)
                .from(DRIVER_NUMBERS)
                .where(DRIVER_NUMBERS.DRIVER_ID.eq(driverId)
                        .and(DRIVER_NUMBERS.SEASON.eq(season)))
                .fetchOneInto(Integer.class);

        if(existing == null) {
            create.insertInto(
                    DRIVER_NUMBERS,
                    DRIVER_NUMBERS.DRIVER_ID,
                    DRIVER_NUMBERS.SEASON,
                    DRIVER_NUMBERS.START_NUMBER)
                    .values(driverId, season, driverApiDto.driver_number())
                    .execute();

        } else if (existing != driverApiDto.driver_number()) {
            create.update(DRIVER_NUMBERS)
                    .set(DRIVER_NUMBERS.START_NUMBER, driverApiDto.driver_number())
                    .where(DRIVER_NUMBERS.DRIVER_ID.eq(driverId)
                            .and(DRIVER_NUMBERS.SEASON.eq(season)))
                    .execute();
        }
    }

    private void validateDriver(DriverApiDto driverApiDto) {
        if (driverApiDto == null)
            throw new IllegalArgumentException("driverApiDto must not be null.");

        if (driverApiDto.driver_number() <= 0)
            throw new IllegalArgumentException("driver_number must be positive.");

        if (driverApiDto.first_name() == null || driverApiDto.first_name().isBlank())
            throw new IllegalArgumentException("full_name must not be null or blank.");

        if (driverApiDto.last_name() == null || driverApiDto.last_name().isBlank())
            throw new IllegalArgumentException("last_name must not be null or blank.");

        if (driverApiDto.country_code() == null || driverApiDto.country_code().isBlank())
            throw new IllegalArgumentException("country_code must not be null or blank.");
    }

    private void validateSeason(int season){
        if (season < FIRST_F1_SEASON)
            throw new IllegalArgumentException("Season must be positive");
    }

    @Override
    public Optional<DriverApiDto> getDriverByFullNameForSeason(String firstName, String lastName, int season) {
        var record = create.select(DRIVERS.FIRST_NAME, DRIVERS.LAST_NAME, DRIVER_NUMBERS.START_NUMBER.as("driver_number"), DRIVERS.COUNTRY_CODE)
                .from(DRIVERS)
                .join(DRIVER_NUMBERS)
                    .on(DRIVER_NUMBERS.DRIVER_ID.eq(DRIVERS.DRIVER_ID))
                .where(DRIVERS.FIRST_NAME.eq(firstName)
                        .and(DRIVERS.LAST_NAME.eq(lastName))
                        .and(DRIVER_NUMBERS.SEASON.eq(season)))
                .fetchOneInto(DriverApiDto.class);

        return Optional.ofNullable(record);
    }

    @Override
    public Optional<DriverApiDto> getDriverByStartNumberForSeason(int startNumber, int season) {
        var record = create.select(
                        DRIVERS.FIRST_NAME, DRIVERS.LAST_NAME,
                        DRIVER_NUMBERS.START_NUMBER.as("driver_number"),
                        DRIVERS.COUNTRY_CODE)
                .from(DRIVER_NUMBERS)
                .join(DRIVERS)
                .on(DRIVER_NUMBERS.DRIVER_ID.eq(DRIVERS.DRIVER_ID))
                .where(DRIVER_NUMBERS.SEASON.eq(season)
                        .and(DRIVER_NUMBERS.START_NUMBER.eq(startNumber)))
                .fetchOneInto(DriverApiDto.class);

        return Optional.ofNullable(record);
    }
}
