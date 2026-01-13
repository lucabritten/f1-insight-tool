package htwsaar.nordpol.API;

import htwsaar.nordpol.API.DTO.DriverApiDto;
import htwsaar.nordpol.Domain.Driver;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

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
                        "country_code": "GBR",
                        "full_name": "Lewis HAMILTON"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setResponseCode(200)
        );


        DriverApiDto driverDto = driverClient.getDriverByName("Lewis", "Hamilton");

        assertThat(driverDto.driver_number()).isEqualTo(44);
        assertThat(driverDto.full_name()).isEqualTo("Lewis HAMILTON");
    }
}


