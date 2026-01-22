package htwsaar.nordpol.api.dto;

public record LapsDto(int driver_number,
                      int lap_number,
                      int session_key,
                      double duration_sector_1,
                      double duration_sector_2,
                      double duration_sector_3,
                      double lap_duration,
                      boolean is_pit_out_lap
) {
}
