package htwsaar.nordpol.config.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

public class ApiClientConfig {

    public static OkHttpClient openF1HttpClient() {
        return new OkHttpClient().newBuilder()
                .callTimeout(Duration.ofSeconds(10))
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .writeTimeout(Duration.ofSeconds(10))
                .addInterceptor(new RateLimitInterceptor())
                .build();
    }

    static class RateLimitInterceptor implements Interceptor {

        private static final Logger log =
                LoggerFactory.getLogger(RateLimitInterceptor.class);

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            if (!response.isSuccessful() && response.code() == 429) {
                log.warn("Rate limit hit: {}", response.message());
                response.close();

                try {
                    log.info("Waiting 1 second before retry...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                response = chain.proceed(request);
            }
            return response;
        }
    }
}
