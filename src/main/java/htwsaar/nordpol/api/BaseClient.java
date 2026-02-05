package htwsaar.nordpol.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.config.api.ApiClientConfig;
import htwsaar.nordpol.exception.ExternalApiException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for all OpenF1 API clients.
 *
 * <p>This class encapsulates HTTP communication, URL construction,
 * and JSON deserialization for the OpenF1 API.</p>
 *
 * <p><strong>Design decision:</strong><br>
 * API calls that return an unsuccessful HTTP status code do NOT throw
 * an exception by default. Instead, an empty result is returned.</p>
 *
 * <p>This allows the service layer to decide whether missing or
 * unavailable data is a valid state or should be treated as a
 * business error (e.g. DriverNotFoundException).</p>
 *
 * <p>Only unexpected technical failures (e.g. I/O errors) are wrapped
 * in an {@link htwsaar.nordpol.exception.ExternalApiException}.</p>
 */
public abstract class BaseClient {

    protected final OkHttpClient okHttpClient;
    protected final ObjectMapper objectMapper;
    protected final String baseUrl;

    private static final Logger log = LoggerFactory.getLogger(BaseClient.class);

    protected BaseClient(String baseUrl, ObjectMapper objectMapper){
        this.baseUrl = baseUrl;
        this.okHttpClient = ApiClientConfig.openF1HttpClient();
        this.objectMapper = objectMapper;
    }

    protected BaseClient(ObjectMapper objectMapper){
        this("https://api.openf1.org/v1", objectMapper);
    }

    protected <T> Optional<T> fetchSingle(OpenF1Endpoint endpoint, Map<OpenF1Param, ?> queryParameter, Class<T[]> responseType) {
        List<T> results = fetchList(endpoint, queryParameter, responseType);
        return results.stream().findFirst();
    }

    protected <T> List<T> fetchList(OpenF1Endpoint endpoint, Map<OpenF1Param, ?> queryParameter, Class<T[]> responseType) {

        String url = buildUrl(endpoint.path(), queryParameter);

        log.debug("URL: {}", url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String bodyString = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("OpenF1 request failed: {} {} body={}",response.code(), response.message(), bodyString);
                return List.of();
            }

            return Arrays.asList(objectMapper.readValue(bodyString, responseType));
        } catch (IOException e) {
            throw new ExternalApiException("Failed to fetch data from OpenF1 API (url=" + url + ")", e);
        }
    }

    private String buildUrl(String path, Map<OpenF1Param, ?> queryParameter) {
        HttpUrl.Builder builder = HttpUrl.parse(baseUrl + path).newBuilder();

        if(queryParameter != null) {
            queryParameter.forEach((k, v) -> {
                if (v != null) {
                    builder.addQueryParameter(k.apiName(), String.valueOf(v));
                }
            });
        }

        return builder.build().toString();
    }

}
