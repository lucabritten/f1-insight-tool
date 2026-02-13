package htwsaar.nordpol.util.formatting;

import java.util.List;

/**
 * Formats gap/status values used in reports.
 */
public final class GapFormatter {

    public String gap(List<String> gaps, boolean dsq, boolean dns, boolean dnf) {
        if (dsq) return "DSQ";
        if (dns) return "DNS";
        if (dnf) return "DNF";
        if (gaps == null || gaps.isEmpty()) return "-";
        for (int i = gaps.size() - 1; i >= 0; i--) {
            String g = gaps.get(i);
            if (g != null && !g.isBlank()) {
                String t = g.trim();
                return (t.startsWith("+") || t.startsWith("-")) ? t : "+" + t;
            }
        }
        return "-";
    }
}
