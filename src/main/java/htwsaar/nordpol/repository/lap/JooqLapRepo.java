package htwsaar.nordpol.repository.lap;

import htwsaar.nordpol.api.dto.LapDto;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Optional;

import static com.nordpol.jooq.tables.Laps.LAPS;

public class JooqLapRepo implements ILapRepo {

    private final DSLContext create;

    public JooqLapRepo(DSLContext create){
        this.create = create;
    }

    @Override
    public void saveAll(List<LapDto> lapDto) {
        validateLapDtoList(lapDto);
        lapDto.forEach(lap ->
                    create.insertInto(
                            LAPS,
                            LAPS.DRIVER_NUMBER,
                            LAPS.SESSION_KEY,
                            LAPS.LAP_NUMBER,
                            LAPS.DURATION_SECTOR_1,
                            LAPS.DURATION_SECTOR_2,
                            LAPS.DURATION_SECTOR_3,
                            LAPS.LAP_DURATION,
                            LAPS.IS_PIT_OUT_LAP
                            ).values(
                                    lap.driver_number(),
                                    lap.session_key(),
                                    lap.lap_number(),
                                    lap.duration_sector_1(),
                                    lap.duration_sector_2(),
                                    lap.duration_sector_3(),
                                    lap.lap_duration(),
                                    lap.is_pit_out_lap() ? 1 : 0
                            ).execute()
                );
    }
    
    private void validateLapDtoList(List<LapDto> laps){
        laps.forEach(lap -> {
            if(lap.session_key() < 0)
                throw new IllegalArgumentException("SessionKey must greater or equal to zero.");

            if(lap.lap_number() < 0)
                throw new IllegalArgumentException("LapNumber must greater or equal to zero.");

            if(lap.driver_number() < 0)
                throw new IllegalArgumentException("DriverNumber must greater or equal to zero.");

            if(lap.duration_sector_1() < 0)
                throw new IllegalArgumentException("DurationSector1 must greater or equal to zero.");

            if(lap.duration_sector_2() < 0)
                throw new IllegalArgumentException("DurationSector2 must greater or equal to zero.");

            if(lap.duration_sector_3() < 0)
                throw new IllegalArgumentException("DurationSector3 must greater or equal to zero.");

            if(lap.lap_duration() < 0)
                throw new IllegalArgumentException("LapDuration must greater or equal to zero.");
        });
    }

    @Override
    public List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        return create.select(
                    LAPS.DRIVER_NUMBER,
                    LAPS.SESSION_KEY,
                    LAPS.LAP_NUMBER,
                    LAPS.DURATION_SECTOR_1,
                    LAPS.DURATION_SECTOR_2,
                    LAPS.DURATION_SECTOR_3,
                    LAPS.LAP_DURATION,
                    LAPS.IS_PIT_OUT_LAP
                    ).from(LAPS)
                    .where(LAPS.SESSION_KEY.eq(sessionKey)
                            .and(LAPS.DRIVER_NUMBER.eq(driverNumber))
                    ).orderBy(LAPS.LAP_NUMBER)
                    .fetchInto(LapDto.class).stream().toList();
    }

    @Override
    public List<LapDto> getFastestLapBySessionKey(int sessionKey) {
        return create.select(
                        LAPS.DRIVER_NUMBER,
                        LAPS.SESSION_KEY,
                        LAPS.LAP_NUMBER,
                        LAPS.DURATION_SECTOR_1,
                        LAPS.DURATION_SECTOR_2,
                        LAPS.DURATION_SECTOR_3,
                        LAPS.LAP_DURATION,
                        LAPS.IS_PIT_OUT_LAP
                )
                .from(LAPS)
                .where(LAPS.SESSION_KEY.eq(sessionKey))
                .and(LAPS.LAP_DURATION.gt(0.0))
                .and(LAPS.IS_PIT_OUT_LAP.eq(0))
                .orderBy(LAPS.LAP_DURATION.asc())
                .limit(1)
                .fetchInto(LapDto.class);
    }
}
