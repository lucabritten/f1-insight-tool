package htwsaar.nordpol.API;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DriverClientTest {

    private MockWebServer mockWebServer;
    private DriverClient driverClient;

    @BeforeEach
    void setUp() throws IOException{
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        driverClient = new DriverClient(mockWebServer.url("/v1").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    void getDriverByFullName_returnsDriver(){
        String json = """
                [
                    {
                        "driver_number": 44,
                        "first_name": "Lewis",
                        "last_name": "Hamilton",
                        "country_code": "GBR"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );


        Optional<DriverApiDto> optionalDriverDto = driverClient.getDriverByName("Lewis", "Hamilton", 2025);

        assertThat(optionalDriverDto).isPresent();

        DriverApiDto driverDto = optionalDriverDto.get();

        assertThat(driverDto.driver_number()).isEqualTo(44);
        assertThat(driverDto.first_name()).isEqualTo("Lewis");
        assertThat(driverDto.last_name()).isEqualTo("Hamilton");
    }

    @Test
    void getDriverByFullName_returnsEmptyOptional_whenApiReturnsEmptyArray(){
        String json = "[]";

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );

        Optional<DriverApiDto> driverApiDto = driverClient.getDriverByName("Unknown", "Driver", 2025);

        assertThat(driverApiDto).isEmpty();
    }

    @Test
    void getDriverByFullName_returnsEmptyOptional_whenApiReturns404(){
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
        );

        Optional<DriverApiDto> optionalDriverDto = driverClient.getDriverByName("Lando", "NORRIS", 2025);

        assertThat(optionalDriverDto).isEmpty();
    }

    @Test
    void getDriverByFullName_throwsException_whenJsonIsInvalid() {
        String invalidJson = "{invalid";

        mockWebServer.enqueue(new MockResponse()
                .setBody(invalidJson)
                .setResponseCode(200)
        );

        assertThatThrownBy(() ->
                driverClient.getDriverByName("Lewis", "Hamilton", 2025)
        ).isInstanceOf(RuntimeException.class);
    }
}


