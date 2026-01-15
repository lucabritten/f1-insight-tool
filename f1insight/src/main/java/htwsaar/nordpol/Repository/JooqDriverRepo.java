package htwsaar.nordpol.Repository;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import org.jooq.DSLContext;

import static com.nordpol.jooq.tables.Drivers.*;

public class JooqDriverRepo implements DriverRepo{

    private final DSLContext create;

    public JooqDriverRepo(DSLContext create) {
        this.create = create;
    }

    @Override
    public void saveDriver(DriverApiDto driverApiDto) {
        validateDriver(driverApiDto);

        create
                .insertInto(DRIVERS,
                        DRIVERS.DRIVER_NUMBER,
                        DRIVERS.FIRST_NAME,
                        DRIVERS.LAST_NAME,
                        DRIVERS.COUNTRY_CODE
                        )
                .values(driverApiDto.driver_number(),
                        driverApiDto.first_name(),
                        driverApiDto.last_name(),
                        driverApiDto.country_code()
                )
                .execute();
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

    @Override
    public DriverApiDto getDriverByFullname(String firstName, String lastName) {
        return create
                .select(DRIVERS.FIRST_NAME, DRIVERS.LAST_NAME, DRIVERS.DRIVER_NUMBER, DRIVERS.COUNTRY_CODE)
                .from(DRIVERS)
                .where(DRIVERS.FIRST_NAME.eq(firstName)
                        .and(DRIVERS.LAST_NAME.eq(lastName))
                )
                .fetchOneInto(DriverApiDto.class);
    }
}
