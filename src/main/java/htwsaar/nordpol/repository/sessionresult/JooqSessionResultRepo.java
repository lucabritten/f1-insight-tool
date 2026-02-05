package htwsaar.nordpol.repository.sessionresult;

import htwsaar.nordpol.dto.SessionResultDto;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Stream;

import static com.nordpol.jooq.tables.SessionResults.*;

public class JooqSessionResultRepo implements ISessionResultRepo {

    private final DSLContext create;

    private static final int Q1 = 0;
    private static final int Q2 = 1;
    private static final int Q3 = 2;

    public JooqSessionResultRepo(DSLContext create) {
        this.create = create;
    }

    @Override
    public List<SessionResultDto> getSessionResultBySessionKey(int sessionKey) {
        return create.select(
                SESSION_RESULTS.SESSION_KEY,
                SESSION_RESULTS.DRIVER_NUMBER,
                SESSION_RESULTS.POSITION,
                SESSION_RESULTS.DNF,
                SESSION_RESULTS.DNS,
                SESSION_RESULTS.DSQ,
                SESSION_RESULTS.GAP_TO_LEADER_Q1,
                SESSION_RESULTS.GAP_TO_LEADER_Q2,
                SESSION_RESULTS.GAP_TO_LEADER_Q3,
                SESSION_RESULTS.DURATION_Q1,
                SESSION_RESULTS.DURATION_Q2,
                SESSION_RESULTS.DURATION_Q3
            )
            .from(SESSION_RESULTS)
            .where(SESSION_RESULTS.SESSION_KEY.eq(sessionKey))
            .fetch(result -> new SessionResultDto(
                result.get(SESSION_RESULTS.SESSION_KEY),
                Stream.of(
                    result.get(SESSION_RESULTS.GAP_TO_LEADER_Q1),
                    result.get(SESSION_RESULTS.GAP_TO_LEADER_Q2),
                    result.get(SESSION_RESULTS.GAP_TO_LEADER_Q3)
                ).toList(),
                result.get(SESSION_RESULTS.DRIVER_NUMBER),
                result.get(SESSION_RESULTS.DNF) == 1,
                result.get(SESSION_RESULTS.DNS) == 1,
                result.get(SESSION_RESULTS.DSQ) == 1,
                Stream.of(
                    result.get(SESSION_RESULTS.DURATION_Q1),
                    result.get(SESSION_RESULTS.DURATION_Q2),
                    result.get(SESSION_RESULTS.DURATION_Q3)
                ).toList(),
                result.get(SESSION_RESULTS.POSITION)
            ));
    }

    @Override
    public void saveAll(List<SessionResultDto> dtoList) {
        dtoList.forEach(dto ->
            create.insertInto(SESSION_RESULTS,
                SESSION_RESULTS.SESSION_KEY,
                SESSION_RESULTS.DRIVER_NUMBER,
                SESSION_RESULTS.POSITION,
                SESSION_RESULTS.DNF,
                SESSION_RESULTS.DNS,
                SESSION_RESULTS.DSQ,
                SESSION_RESULTS.GAP_TO_LEADER_Q1,
                SESSION_RESULTS.GAP_TO_LEADER_Q2,
                SESSION_RESULTS.GAP_TO_LEADER_Q3,
                SESSION_RESULTS.DURATION_Q1,
                SESSION_RESULTS.DURATION_Q2,
                SESSION_RESULTS.DURATION_Q3)
                .values(dto.session_key(),
                        dto.driver_number(),
                        dto.position(),
                        status(dto.dnf()),
                        status(dto.dns()),
                        status(dto.dsq()),
                        gap(dto.gap_to_leader(), Q1),
                        gap(dto.gap_to_leader(), Q2),
                        gap(dto.gap_to_leader(), Q3),
                        duration(dto.duration(), Q1),
                        duration(dto.duration(), Q2),
                        duration(dto.duration(), Q3))
                .execute()
        );
    }

    private int status(boolean status) {
        return status ? 1 : 0;
    }

    private String gap(List<String> gaps, int qualifyingNumber) {
        return mapListToDbValue(gaps, qualifyingNumber);
    }

    private Double duration(List<Double> durations, int qualifyingNumber) {
        return mapListToDbValue(durations, qualifyingNumber);
    }

    private <T> T mapListToDbValue(List<T> list, int index) {
        return (list != null && list.size() > index)
                ? list.get(index)
                : null;
    }
}
