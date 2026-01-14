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
        if(driverApiDto == null)
            throw new IllegalArgumentException("driverApiDto must not be null.");

        if(driverApiDto.driver_number() <= 0)
            throw new IllegalArgumentException("driver_number must be positive.");

        if(driverApiDto.full_name() == null || driverApiDto.full_name().isBlank())
            throw new IllegalArgumentException("full_name must not be null or blank.");

        if(driverApiDto.country_code() == null || driverApiDto.country_code().isBlank())
            throw new IllegalArgumentException("country_code must not be null or blank.");

        create
                .insertInto(DRIVERS)
                .values(driverApiDto.driver_number(),
                        driverApiDto.full_name(),
                        driverApiDto.country_code()
                )
                .execute();
    }

    @Override
    public DriverApiDto getDriverByFullname(String surname, String lastName) {
        return create
                .select(DRIVERS.FULL_NAME, DRIVERS.DRIVER_NUMBER, DRIVERS.COUNTRY_CODE)
                .from(DRIVERS)
                .where(DRIVERS.FULL_NAME.eq(surname + " " + lastName))
                .fetchOneInto(DriverApiDto.class);
    }
}
