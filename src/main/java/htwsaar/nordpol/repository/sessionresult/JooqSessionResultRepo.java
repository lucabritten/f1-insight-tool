package htwsaar.nordpol.repository.sessionresult;

import htwsaar.nordpol.api.dto.SessionResultDto;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Stream;

import static com.nordpol.jooq.tables.SessionResults.*;

public class JooqSessionResultRepo implements ISessionResultRepo {

    private DSLContext create;

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
            .fetch(record -> new SessionResultDto(
                record.get(SESSION_RESULTS.SESSION_KEY),
                Stream.of(
                    record.get(SESSION_RESULTS.GAP_TO_LEADER_Q1),
                    record.get(SESSION_RESULTS.GAP_TO_LEADER_Q2),
                    record.get(SESSION_RESULTS.GAP_TO_LEADER_Q3)
                ).toList(),
                record.get(SESSION_RESULTS.DRIVER_NUMBER),
                record.get(SESSION_RESULTS.DNF) == 1,
                record.get(SESSION_RESULTS.DNS) == 1,
                record.get(SESSION_RESULTS.DSQ) == 1,
                Stream.of(
                    record.get(SESSION_RESULTS.DURATION_Q1),
                    record.get(SESSION_RESULTS.DURATION_Q2),
                    record.get(SESSION_RESULTS.DURATION_Q3)
                ).toList(),
                record.get(SESSION_RESULTS.POSITION)
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
                        dto.dnf() ? 1 : 0,
                        dto.dns() ? 1 : 0,
                        dto.dsq() ? 1 : 0,
                        (dto.gap_to_leader() != null && !dto.gap_to_leader().isEmpty()) ? dto.gap_to_leader().get(0) : null,
                        (dto.gap_to_leader() != null && dto.gap_to_leader().size() > 1) ? dto.gap_to_leader().get(1) : null,
                        (dto.gap_to_leader() != null && dto.gap_to_leader().size() > 2) ? dto.gap_to_leader().get(2) : null,
                        (dto.duration() != null && !dto.duration().isEmpty()) ? dto.duration().get(0) : null,
                        (dto.duration() != null && dto.duration().size() > 1) ? dto.duration().get(1) : null,
                        (dto.duration() != null && dto.duration().size() > 2) ? dto.duration().get(2) : null)
                .execute()
        );
    }
}
