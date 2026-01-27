package htwsaar.nordpol.api.lap;
import htwsaar.nordpol.api.BaseClient;
import htwsaar.nordpol.api.dto.LapDto;
import java.util.List;
import java.util.Map;


public class LapClient extends BaseClient implements ILapClient{

    public LapClient(String baseUrl) {
        super(baseUrl);
    }
    public LapClient(){
        super();
    }

    @Override
    public List<LapDto> getLapsBySessionKeyAndDriverNumber(int sessionKey, int driverNumber) {
        return fetchList(
                "/laps",
                Map.of(
                        "session_key", sessionKey,
                        "driver_number", driverNumber
                ),
                LapDto[].class
        );
    }

    @Override
    public List<LapDto> getLapsBySessionKey(int sessionKey) {
        return fetchList(
                "/laps",
                Map.of("session_key", sessionKey),
                LapDto[].class
        );
    }
}
