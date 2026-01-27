package htwsaar.nordpol.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.util.List;
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

    /*
    protected <T> Optional<T> fetchSingle() {

    }

    protected <T> List<T> fetchList() {

    }
     */

}
