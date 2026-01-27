package htwsaar.nordpol.api.baseclient;
import htwsaar.nordpol.api.BaseClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;

class BaseClientTest {

    private MockWebServer mockWebServer;
    private TestBaseClient baseClient;

    static class TestBaseClient extends BaseClient {
        TestBaseClient(String baseUrl) {
            super(baseUrl);
        }
    }

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

}