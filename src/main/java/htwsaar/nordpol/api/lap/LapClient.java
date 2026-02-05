package htwsaar.nordpol.api.lap;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.OpenF1Endpoint;
import htwsaar.nordpol.dto.LapDto;
import java.util.List;
import java.util.Map;
import static htwsaar.nordpol.api.OpenF1Param.*;


public class LapClient extends BaseClient implements ILapClient{

    public LapClient(String baseUrl, ObjectMapper mapper) {
        super(baseUrl, mapper);
    }
    public LapClient(ObjectMapper mapper){
        super(mapper);
    }

    @Override
    public List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        return fetchList(
                OpenF1Endpoint.LAPS,
                Map.of(
                        SESSION_KEY, sessionKey,
                        DRIVER_NUMBER, driverNumber
                ),
                LapDto[].class
        );
    }

    @Override
    public List<LapDto> getLapsBySessionKey(int sessionKey) {
        return fetchList(
                OpenF1Endpoint.LAPS,
                Map.of(SESSION_KEY, sessionKey),
                LapDto[].class
        );
    }
}
