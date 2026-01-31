package htwsaar.nordpol.util;

import htwsaar.nordpol.api.dto.*;
import htwsaar.nordpol.domain.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MapperTest {

    @Test
    void toDriverDto_mapsAllFields() {

        DriverDto dto = new DriverDto("Max", "Verstappen", 1, "Red Bull");

        Driver driver = Mapper.toDriver(dto);

        assertThat(driver.firstName()).isEqualTo("Max");
        assertThat(driver.lastName()).isEqualTo("Verstappen");
        assertThat(driver.driverNumber()).isEqualTo(1);
        assertThat(driver.teamName()).isEqualTo("Red Bull");
    }

    @Test
    void toMeetingDto_mapsAllFields() {

        MeetingDto dto = new MeetingDto("BEL", "Belgium", "Spa-Francorchamps", 1000, "Sprint Qualifying", 2025);

        Meeting meeting = Mapper.toMeeting(dto);

        assertThat(meeting.countryCode()).isEqualTo("BEL");
        assertThat(meeting.countryName()).isEqualTo("Belgium");
        assertThat(meeting.location()).isEqualTo("Spa-Francorchamps");
        assertThat(meeting.meetingKey()).isEqualTo(1000);
        assertThat(meeting.meetingName()).isEqualTo("Sprint Qualifying");
        assertThat(meeting.year()).isEqualTo(2025);
    }

    @Test
    void toSessionDto_mapsAllFields() {

        SessionDto dto = new SessionDto(1216, 9140, "Sprint", "Sprint");

        Session session = Mapper.toSession(dto);

        assertThat(session.meetingKey()).isEqualTo(1216);
        assertThat(session.sessionKey()).isEqualTo(9140);
        assertThat(session.sessionName().displayName()).isEqualTo("Sprint");
        assertThat(session.sessionType()).isEqualTo("Sprint");
    }

    @Test
    void toWeatherDto_mapsAllFields() {

        WeatherDto dto = new WeatherDto(1234, 1256, 21.0, 12.0, 1, 20.0, 44.0, 20.0);

        Weather weather = Mapper.toWeather(dto);

        assertThat(weather.sessionKey()).isEqualTo(1234);
        assertThat(weather.meetingKey()).isEqualTo(1256);
        assertThat(weather.avgAirTemperature()).isEqualTo(21.0);
        assertThat(weather.avgHumidity()).isEqualTo(12.0);
        assertThat(weather.isRainfall()).isEqualTo(true);
        assertThat(weather.avgTrackTemperature()).isEqualTo(20.0);
        assertThat(weather.avgWindDirection()).isEqualTo(44.0);
        assertThat(weather.avgWindSpeed()).isEqualTo(20.0);
    }

    @Test
    void toLapDto_mapsAllFields() {

        LapDto dto = new LapDto(33, 1011, 1, 30.1, 29.8, 31.2, 91.1, false);

        Lap lap = Mapper.toLap(dto);

        assertThat(lap.driverNumber()).isEqualTo(33);
        assertThat(lap.sessionKey()).isEqualTo(1011);
        assertThat(lap.lapNumber()).isEqualTo(1);
        assertThat(lap.durationSector1()).isEqualTo(30.1);
        assertThat(lap.durationSector2()).isEqualTo(29.8);
        assertThat(lap.durationSector3()).isEqualTo(31.2);
        assertThat(lap.lapDuration()).isEqualTo(91.1);
        assertThat(lap.isPitOutLap()).isEqualTo(false);
    }

    /*
    @Test
    void toSessionResult_mapsAllFields_whenNoNulls() {

    }
     */
}
