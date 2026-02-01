package htwsaar.nordpol.repository.weather;


import htwsaar.nordpol.api.dto.WeatherDto;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class WeatherRepoTest {

    private Connection connection;
    private DSLContext create;
    private IWeatherRepo weatherRepo;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        create = DSL.using(connection, SQLDialect.SQLITE);
        create.execute("""
                CREATE TABLE Weather (
                    session_key integer not null,
                    meeting_key integer not null,
                    avg_air_temperature real not null,
                    avg_humidity real not null ,
                    is_rainfall integer not null ,
                    avg_track_temperature real not null,
                    avg_wind_direction real not null ,
                    avg_wind_speed real not null,
                    PRIMARY KEY (session_key, meeting_key)
                                     );
        """);

        weatherRepo = new JooqWeatherRepo(create);
    }

    @AfterEach
    void tearDown() throws Exception {
        if(connection != null){
            connection.close();
        }
    }

    @Test
    void saveWeather_persistsMeeting() {
        WeatherDto weatherData = new WeatherDto(1256, 1234, 21.00,12.47,30, 20.24, 44.00, 20.00);

        weatherRepo.save(weatherData);

        Optional<WeatherDto> stored =
                weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(1234, 1256);

        assertThat(stored).isPresent();

        WeatherDto dto = stored.get();

        assertThat(dto.meeting_key()).isEqualTo(1234);
        assertThat(dto.session_key()).isEqualTo(1256);
        assertThat(dto.air_temperature()).isEqualTo(21.00);
        assertThat(dto.humidity()).isEqualTo(12.47);
        assertThat(dto.rainfall()).isEqualTo(30);
        assertThat(dto.track_temperature()).isEqualTo(20.24);
        assertThat(dto.wind_direction()).isEqualTo(44.00);
        assertThat(dto.wind_speed()).isEqualTo(20.00);
    }
    @Test
    void getWeatherDataByMeetingKeyAndSessionKey_returnsEmptyOptional_whenMissing() {
        Optional<WeatherDto> stored =
                weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(1234, 1256);
        assertThat(stored).isEmpty();
    }
    @Test
    void saveWeather_throwsException_onDuplicatePrimaryKey() {
        WeatherDto dto =
                new WeatherDto(1256, 1234, 21.0, 12.0, 1, 20.0, 44.0, 20.0);

        weatherRepo.save(dto);

        assertThatThrownBy(() -> weatherRepo.save(dto))
                .isInstanceOf(Exception.class);
    }
    @Test
    void getWeather_returnsCorrectRow_whenMultipleExist() {
        weatherRepo.save(new WeatherDto(1, 100, 10, 10, 0, 10, 10, 10));
        weatherRepo.save(new WeatherDto(2, 200, 20, 20, 1, 20, 20, 20));

        Optional<WeatherDto> stored =
                weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(200, 2);

        assertThat(stored).isPresent();
        assertThat(stored.get().meeting_key()).isEqualTo(200);
    }




}
