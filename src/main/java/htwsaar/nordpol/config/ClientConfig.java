package htwsaar.nordpol.config;


import htwsaar.nordpol.api.driver.DriverClient;
import htwsaar.nordpol.api.driver.IDriverClient;
import htwsaar.nordpol.api.lap.ILapClient;
import htwsaar.nordpol.api.lap.LapClient;
import htwsaar.nordpol.api.meeting.IMeetingClient;
import htwsaar.nordpol.api.meeting.MeetingClient;
import htwsaar.nordpol.api.session.ISessionClient;
import htwsaar.nordpol.api.session.SessionClient;
import htwsaar.nordpol.api.sessionresult.ISessionResultClient;
import htwsaar.nordpol.api.sessionresult.SessionResultClient;
import htwsaar.nordpol.api.weather.IWeatherClient;
import htwsaar.nordpol.api.weather.WeatherClient;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class ClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient().newBuilder()
                .callTimeout(Duration.ofSeconds(10))
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .writeTimeout(Duration.ofSeconds(10))
                .addInterceptor(this::intercept)
                .build();
    }

    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (!response.isSuccessful() && response.code() == 429) {
            response.close();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            response = chain.proceed(request);
        }
        return response;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public IDriverClient driverClient(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper
    ) {
            return new DriverClient(
                    "https://api.openf1.org/v1",
                    okHttpClient,
                    objectMapper
            );
    }

    @Bean
    public IMeetingClient meetingClient(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper
    ) {
        return new MeetingClient(
                "https://api.openf1.org/v1",
                okHttpClient,
                objectMapper
        );
    }

    @Bean
    public ILapClient lapClient(
        OkHttpClient okHttpClient,
        ObjectMapper objectMapper
    ){
        return new LapClient(
                "https://api.openf1.org/v1",
                okHttpClient,
                objectMapper
        );
    }

    @Bean
    public ISessionClient sessionClient(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper
    ){
        return new SessionClient(
                "https://api.openf1.org/v1",
                okHttpClient,
                objectMapper
        );
    }

    @Bean
    public IWeatherClient weatherClient(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper
    ) {
        return new WeatherClient(
                "https://api.openf1.org/v1",
                okHttpClient,
                objectMapper
        );
    }

    @Bean
    public ISessionResultClient sessionResultClient(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper
    ) {
        return new SessionResultClient(
                "https://api.openf1.org/v1",
                okHttpClient,
                objectMapper
        );
    }

}
