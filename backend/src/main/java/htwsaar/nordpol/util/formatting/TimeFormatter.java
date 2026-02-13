package htwsaar.nordpol.util.formatting;

import java.util.List;

/**
 * Formats time-related values used in reports.
 */
public final class TimeFormatter {

    public String segment(List<Double> segments, int index) {
        if (segments != null && index < segments.size() && segments.get(index) != null) {
            return String.format("%.3f", segments.get(index));
        }
        return "-";
    }
}
