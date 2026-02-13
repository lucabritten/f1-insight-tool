package htwsaar.nordpol.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record MeetingDto(String country_code,
                         String country_name,
                         String location,
                         int meeting_key,
                         String meeting_name,
                         int year,
                         String country_flag) {
}
