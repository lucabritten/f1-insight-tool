package htwsaar.nordpol.api.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record DriverDto(String first_name,
                        String last_name,
                        int driver_number,
                        String team_name) {
}
