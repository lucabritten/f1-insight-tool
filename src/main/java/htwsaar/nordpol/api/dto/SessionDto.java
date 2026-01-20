package htwsaar.nordpol.api.dto;

public record SessionDto(int meeting_key,
                         int session_key,
                         String session_name,
                         String session_type) {
}
