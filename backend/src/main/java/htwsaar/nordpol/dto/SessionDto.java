package htwsaar.nordpol.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record SessionDto(int meeting_key,
                         int session_key,
                         String session_name,
                         String session_type) {
}
