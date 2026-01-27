package htwsaar.nordpol.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseClient {

    protected final OkHttpClient okHttpClient = new OkHttpClient();
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final String baseUrl;

    protected BaseClient(String baseUrl){
        this.baseUrl = baseUrl;
    }

    protected BaseClient(){
        this("https://api.openf1.org/v1");
    }

    protected <T> Optional<T> fetchSingle(String path, Map<String, ?> queries, Class<T[]> responseType) {
        return fetchList(path, queries, responseType).stream()
                        .findFirst();
    }

    protected <T> List<T> fetchList(String path, Map<String, ?> queries, Class<T[]> responseType) {
        String url = buildUrl(path, queries);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if(!response.isSuccessful()) {
                return List.of();
            }

            return Arrays.asList(objectMapper.readValue(response.body().string(), responseType));
        }
        catch(IOException e) {
            throw new RuntimeException("Failed to fetch data from OpenF1 API");
        }
    }

    private String buildUrl(String path, Map<String, ?> queries) {
        HttpUrl.Builder builder = HttpUrl.parse(baseUrl + path).newBuilder();

        if(queries != null) {
            queries.forEach((k, v) -> {
                if (v != null) {
                    builder.addQueryParameter(k, String.valueOf(v));
                }
            });
        }

        return builder.build().toString();
    }

}
