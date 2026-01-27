package htwsaar.nordpol.domain;

import java.util.List;

public record SessionResult(int driverNumber,
                            int position,
                            List<String> gapToLeader,
                            List<Double> duration,
                            boolean dnf,
                            boolean dsq,
                            boolean dns) {
}
