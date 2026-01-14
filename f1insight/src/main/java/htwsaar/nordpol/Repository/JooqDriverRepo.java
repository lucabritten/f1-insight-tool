package htwsaar.nordpol.Repository;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import org.jooq.DSLContext;

import static com.nordpol.jooq.tables.Drivers.*;
import static org.jooq.impl.DSL.asterisk;

public class JooqDriverRepo implements DriverRepo{

    private final DSLContext create;

    public JooqDriverRepo(DSLContext create) {
        this.create = create;
    }

    @Override
    public void saveDriver(DriverApiDto driverApiDto) {
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
                .select(asterisk())
                .from(DRIVERS)
                .where(DRIVERS.FULL_NAME.eq(surname + " " + lastName))
                .fetchOneInto(DriverApiDto.class);
    }
}
