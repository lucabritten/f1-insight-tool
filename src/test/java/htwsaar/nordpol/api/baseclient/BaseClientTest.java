package htwsaar.nordpol.api.baseclient;
import htwsaar.nordpol.api.BaseClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseClientTest {

    private MockWebServer mockWebServer;
    private TestBaseClient baseClient;

    static class TestBaseClient extends BaseClient {
        TestBaseClient(String baseUrl) {
            super(baseUrl);
        }

        <T> List<T> fetchListTest(String path, Map<String, ?> queries, Class<T[]> responseType) {
            return super.fetchList(path, queries, responseType);
        }

        <T> Optional<T> fetchSingleTest(String path, Map<String, ?> queries, Class<T[]> responseType) {
            return super.fetchSingle(path, queries, responseType);
        }
    }

    record TestDto(String value) {}

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseClient = new TestBaseClient(mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    void fetchList_returnsList() {
        String json = """
                [
                    { "value": "A" },
                    { "value": "B" }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        List<TestDto> result =
                baseClient.fetchListTest("/test", Map.of("key", "value"), TestDto[].class);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).value()).isEqualTo("A");
        assertThat(result.get(1).value()).isEqualTo("B");
    }

    @Test
    void fetchSingle_returnsElement() {
        String json = """
                [
                    { "value": "A" }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<TestDto> result =
                baseClient.fetchSingleTest("/test", null, TestDto[].class);

        assertThat(result).isPresent();
        assertThat(result.get().value()).isEqualTo("A");
    }

    @Test
    void fetchSingle_returnsEmptyOptional_whenResponseIsEmpty() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("[]")
                .setResponseCode(200)
        );

        Optional<TestDto> result =
                baseClient.fetchSingleTest("/test", null, TestDto[].class);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchList_returnsEmptyList_whenHttpStatusIsNotSuccessful() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );

        List<TestDto> result =
                baseClient.fetchListTest("/test", null, TestDto[].class);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchList_throwsException_whenJsonIsInvalid() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{invalid")
                .setResponseCode(200)
        );

        assertThatThrownBy(() ->
                baseClient.fetchListTest("/test", null, TestDto[].class)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void fetchList_throwsRuntimeException_whenConnectionFails() throws IOException {
        mockWebServer.shutdown();

        assertThatThrownBy(() ->
                baseClient.fetchListTest("/test", null, TestDto[].class)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch data from OpenF1 API");
    }

}