package htwsaar.nordpol.API.DTO;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record DriverApiDto(String first_name,
                           String last_name,
                           int driver_number,
                           String country_code) {
}
