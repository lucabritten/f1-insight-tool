package htwsaar.nordpol.service;

import htwsaar.nordpol.api.dto.WeatherDto;
import htwsaar.nordpol.api.weather.IWeatherClient;
import htwsaar.nordpol.cli.view.WeatherWithContext;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.domain.Weather;
import htwsaar.nordpol.exception.WeatherNotFoundException;
import htwsaar.nordpol.repository.weather.IWeatherRepo;
import htwsaar.nordpol.util.Mapper;

import java.util.List;
import java.util.Optional;

public class WeatherService {

    private final IWeatherClient weatherClient;
    private final IWeatherRepo weatherRepo;
    private final SessionService sessionService;
    private final MeetingService meetingService;

    public WeatherService(IWeatherClient client, IWeatherRepo repo, SessionService sessionService, MeetingService meetingService){
        if(client == null)
            throw new IllegalArgumentException("WeatherClient must not be null.");
        if(repo == null)
            throw new IllegalArgumentException("WeatherRepo must not be null.");
        if(sessionService == null)
            throw new IllegalArgumentException("SessionService must not be null.");
        if(meetingService == null)
            throw new IllegalArgumentException("MeetingService must not be null,");

        this.weatherClient = client;
        this.weatherRepo = repo;
        this.sessionService = sessionService;
        this.meetingService = meetingService;
    }

    public Weather getWeatherByLocationYearAndSessionName(String location, int year, SessionName sessionName) {
        Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
        int meetingKey = meeting.meetingKey();

        Session session = sessionService.getSessionByMeetingKeyAndSessionName(meetingKey, sessionName);
        int sessionKey = session.sessionKey();

        Weather weather = getWeatherByMeetingAndSessionKey(meetingKey, sessionKey);
        return new WeatherWithContext(meeting.location(), meeting.countryName(), session.sessionName(), weather);
    }

    public Weather getWeatherByMeetingAndSessionKey(int meetingKey, int sessionKey) {
        Optional<WeatherDto> dtoFromDB = weatherRepo.getWeatherDataByMeetingKeyAndSessionKey(meetingKey, sessionKey);
        if(dtoFromDB.isPresent()) {
            return Mapper.toWeather(dtoFromDB.get());
        }

        List<WeatherDto> dtosFromApi =
                weatherClient.getWeatherDataByMeetingKeyAndSessionKey(meetingKey,sessionKey)
                        .orElseThrow(() ->
                                new WeatherNotFoundException(meetingKey, sessionKey)
                        );

        WeatherDto averagedWeather = calculateAverageData(dtosFromApi);

        weatherRepo.save(averagedWeather);

        return Mapper.toWeather(averagedWeather);
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
