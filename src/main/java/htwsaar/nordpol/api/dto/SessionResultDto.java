package htwsaar.nordpol.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)

public record SessionResultDto(int session_key,
                               @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                               List<String> gap_to_leader,
                               int driver_number,
                               boolean dnf,
                               boolean dns,
                               boolean dsq,
                               @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                               List<Double> duration,
                               Integer position) {
}
