package htwsaar.nordpol.service.weather;

import htwsaar.nordpol.api.dto.WeatherDto;
import htwsaar.nordpol.api.weather.IWeatherClient;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.exception.WeatherNotFoundException;
import htwsaar.nordpol.repository.weather.IWeatherRepo;
import htwsaar.nordpol.service.ICacheService;
import htwsaar.nordpol.service.meeting.MeetingService;
import htwsaar.nordpol.service.session.SessionService;
import htwsaar.nordpol.util.Mapper;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class WeatherService implements IWeatherService {

    private final IWeatherClient weatherClient;
    private final IWeatherRepo weatherRepo;
    private final SessionService sessionService;
    private final MeetingService meetingService;
    private final ICacheService cacheService;

    public WeatherService(IWeatherClient weatherClient, IWeatherRepo weatherRepo, SessionService sessionService, MeetingService meetingService, ICacheService cacheService){
        requireNonNull(weatherClient, "weatherClient must not be null");
        requireNonNull(weatherRepo, "weatherRepo must not be null.");
        requireNonNull(sessionService, "sessionService must not be null.");
        requireNonNull(meetingService, "meetingService must not be null");
        requireNonNull(cacheService, "cacheService must not be null");

        this.weatherClient = weatherClient;
        this.weatherRepo = weatherRepo;
        this.sessionService = sessionService;
        this.meetingService = meetingService;
        this.cacheService = cacheService;
    }

    @Override
    public WeatherWithContext getWeatherByLocationYearAndSessionName(String location, int year, SessionName sessionName) {
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        Weather weather = getWeatherByMeetingAndSessionKey(meetingKey, sessionKey);
        return new WeatherWithContext(meeting.meetingName(), meeting.countryName(), session.sessionName(), weather);
    }

    @Override
    public Weather getWeatherByMeetingAndSessionKey(int meetingKey, int sessionKey) {
        Optional<WeatherDto> dtoFromDB = weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(meetingKey, sessionKey);
        if(dtoFromDB.isPresent()) {
            return Mapper.toWeather(dtoFromDB.get());
        }

        List<WeatherDto> dtosFromApi =
                weatherClient.getWeatherDataByMeetingKeyAndSessionKey(meetingKey,sessionKey);

        if(!dtosFromApi.isEmpty()) {
            WeatherDto averagedWeather = calculateAverageData(dtosFromApi);
            weatherRepo.save(averagedWeather);
            return Mapper.toWeather(averagedWeather);
        }

        throw new WeatherNotFoundException(meetingKey, sessionKey);
    }

    private WeatherDto calculateAverageData(List<WeatherDto> dtoList){

        int sessionKey = dtoList.getFirst().session_key();

        int meetingKey = dtoList.getFirst().meeting_key();

        double avgAirTemp = dtoList.stream()
                .mapToDouble(WeatherDto::air_temperature)
                .average()
                .orElse(Double.NaN);

        double avgHumidity = dtoList.stream()
                .mapToDouble(WeatherDto::humidity)
                .average()
                .orElse(Double.NaN);

        double avgTrackTemp = dtoList.stream()
                .mapToDouble(WeatherDto::track_temperature)
                .average()
                .orElse(Double.NaN);

        double avgWindSpeed = dtoList.stream()
                .mapToDouble(WeatherDto::wind_speed)
                .average()
                .orElse(Double.NaN);

        double avgWindDirection = dtoList.stream()
                .mapToDouble(WeatherDto::wind_direction)
                .average()
                .orElse(Double.NaN);

        int isRainfall = dtoList.stream()
                .map(WeatherDto::rainfall)
                .map(i -> i == 1)
                .toList()
                .contains(true) ? 1 : 0;

        return new WeatherDto(sessionKey,
                meetingKey,
                avgAirTemp,
                avgHumidity,
                isRainfall,
                avgTrackTemp,
                avgWindDirection,
                avgWindSpeed);
    }
}
