package htwsaar.nordpol.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseClient {

    protected final OkHttpClient okHttpClient;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final String baseUrl;

    protected BaseClient(String baseUrl){
        this.baseUrl = baseUrl;
        this.okHttpClient = new OkHttpClient().newBuilder()
                .callTimeout(Duration.ofSeconds(10))
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .writeTimeout(Duration.ofSeconds(10))
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);

                    // 429 is how the api indicates a rate limit error
                    if (!response.isSuccessful() && response.code() == 429) {
                        System.err.println("Rate limit hit: " + response.message());
                        response.close(); // Close the failed response

                        try {
                            System.out.println("Waiting 1 second before retry...");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        response = chain.proceed(request);
                    }
                    return response;
                })
                .build();
    }

    protected BaseClient(){
        this("https://api.openf1.org/v1");
    }

    protected <T> Optional<T> fetchSingle(OpenF1Endpoint endpoint, Map<String, ?> queryParameter, Class<T[]> responseType) {
        List<T> results = fetchList(endpoint, queryParameter, responseType);
        return results.stream().findFirst();
    }

    protected <T> List<T> fetchList(OpenF1Endpoint endpoint, Map<String, ?> queryParameter, Class<T[]> responseType) {

        String url = buildUrl(endpoint.path(), queryParameter);

        System.out.println(url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String bodyString = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                System.out.println("OpenF1 request failed: " + response.code() + " " + response.message()
                        + " body=" + bodyString);
                return List.of();
            }

            return Arrays.asList(objectMapper.readValue(bodyString, responseType));
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch data from OpenF1 API", e);
        }
    }

    private String buildUrl(String path, Map<String, ?> queryParameter) {
        HttpUrl.Builder builder = HttpUrl.parse(baseUrl + path).newBuilder();

        if(queryParameter != null) {
            queryParameter.forEach((k, v) -> {
                if (v != null) {
                    builder.addQueryParameter(k, String.valueOf(v));
                }
            });
        }

        return builder.build().toString();
    }

}
