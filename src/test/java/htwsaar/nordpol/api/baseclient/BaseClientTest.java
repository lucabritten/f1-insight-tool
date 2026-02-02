package htwsaar.nordpol.api.baseclient;

import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.api.OpenF1Param;
import htwsaar.nordpol.exception.ExternalApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

        <T> List<T> fetchListTest(OpenF1Endpoint endpoint, Map<OpenF1Param, ?> queryParameter, Class<T[]> responseType) {
            return super.fetchList(endpoint, queryParameter, responseType);
        }

        <T> Optional<T> fetchSingleTest(OpenF1Endpoint endpoint, Map<OpenF1Param, ?> queryParameter, Class<T[]> responseType) {
            return super.fetchSingle(endpoint, queryParameter, responseType);
        }
    }

    record TestDto(String value) {}

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseClient = new TestBaseClient(mockWebServer.url("/v1").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("fetchList")
    class FetchList {

        @Test
        void returnsList() {
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
                    baseClient.fetchListTest(OpenF1Endpoint.TEST, Map.of(OpenF1Param.SESSION_KEY, "value"), TestDto[].class);

        assertThat(result)
                .isNotNull()
                .isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value()).isEqualTo("A");
        assertThat(result.get(1).value()).isEqualTo("B");
    }

        @Test
        void returnsEmptyList_whenHttpStatusIsNotSuccessful() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
            );

            List<TestDto> result =
                    baseClient.fetchListTest(OpenF1Endpoint.TEST, null, TestDto[].class);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchSingle")
    class FetchSingle {

        @Test
        void returnsElement() {
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
                    baseClient.fetchSingleTest(OpenF1Endpoint.TEST, null, TestDto[].class);

            assertThat(result).isPresent();
            assertThat(result.get().value()).isEqualTo("A");
        }

        @Test
        void returnsEmptyOptional_whenResponseIsEmpty() {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody("[]")
                    .setResponseCode(200)
            );

            Optional<TestDto> result =
                    baseClient.fetchSingleTest(OpenF1Endpoint.TEST, null, TestDto[].class);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        void throwsException_whenJsonIsInvalid() {
            mockWebServer.enqueue(new MockResponse()
                    .setBody("{invalid")
                    .setResponseCode(200)
            );

            assertThatThrownBy(() ->
                    baseClient.fetchListTest(OpenF1Endpoint.TEST, null, TestDto[].class)
            ).isInstanceOf(ExternalApiException.class);
        }

        @Test
        void throwsRuntimeException_whenConnectionFails() throws IOException {
            mockWebServer.shutdown();

            assertThatThrownBy(() ->
                    baseClient.fetchListTest(OpenF1Endpoint.TEST, null, TestDto[].class)
            )
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Failed to fetch data from OpenF1 API");
        }
    }

    @Nested
    @DisplayName("Rate Limiting")
    class RateLimiting {

        @Test
        void retriesOnce_whenRateLimitedWith429() throws InterruptedException {
            String json = """
                [
                    { "value": "A" }
                ]
                """;

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setBody("Rate limit")
            );

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(json)
            );

            List<TestDto> result =
                    baseClient.fetchListTest(OpenF1Endpoint.TEST, null, TestDto[].class);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().value()).isEqualTo("A");

            assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

            var firstRequest = mockWebServer.takeRequest();
            var secondRequest = mockWebServer.takeRequest();

            assertThat(firstRequest.getPath()).isEqualTo("/v1/test");
            assertThat(secondRequest.getPath()).isEqualTo("/v1/test");
        }
    }
}
