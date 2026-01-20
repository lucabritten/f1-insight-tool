package htwsaar.nordpol.api.dto;

public record MeetingDto(String country_code,
                         String country_name,
                         String location,
                         int meeting_key,
                         String meeting_name,
                         int year) {
}
