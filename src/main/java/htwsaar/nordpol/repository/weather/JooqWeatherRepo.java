package htwsaar.nordpol.repository.weather;

import htwsaar.nordpol.api.dto.WeatherDto;
import org.jooq.DSLContext;

import static com.nordpol.jooq.tables.Weather.*;
import static org.jooq.impl.DSL.asterisk;

import java.util.Optional;

public class JooqWeatherRepo implements IWeatherRepo{

    private final DSLContext create;

    public JooqWeatherRepo(DSLContext create){
        this.create = create;
    }

    @Override
    public void save(WeatherDto dto) {
        create
                .insertInto(WEATHER,
                        WEATHER.SESSION_KEY,
                        WEATHER.MEETING_KEY,
                        WEATHER.AVG_AIR_TEMPERATURE,
                        WEATHER.AVG_HUMIDITY,
                        WEATHER.IS_RAINFALL,
                        WEATHER.AVG_TRACK_TEMPERATURE,
                        WEATHER.AVG_WIND_DIRECTION,
                        WEATHER.AVG_WIND_SPEED
                        )
                .values(dto.session_key(),
                        dto.meeting_key(),
                        dto.air_temperature(),
                        dto.humidity(),
                        dto.rainfall(),
                        dto.track_temperature(),
                        dto.wind_direction(),
                        dto.wind_speed())
                .execute();
    }

    @Override
    public Optional<WeatherDto> getWeatherDataByMeetingKeyAndSessionKey(int meetingKey, int sessionKey) {
        var record = create.select(asterisk())
                .from(WEATHER)
                .where(WEATHER.MEETING_KEY.eq(meetingKey)
                        .and(WEATHER.SESSION_KEY.eq(sessionKey)))
                .fetchOneInto(WeatherDto.class);

        return Optional.ofNullable(record);
    }
}
