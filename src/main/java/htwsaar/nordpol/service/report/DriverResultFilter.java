package htwsaar.nordpol.service.report;

import htwsaar.nordpol.domain.SessionResult;

import java.util.List;


public class DriverResultFilter {

    public List<SessionResult> filterTopDrivers(List<SessionResult> results, Integer topDrivers) {
        if (topDrivers == null || results.isEmpty()) {
            return results;
        }
        int limit = Math.min(topDrivers, results.size());
        return List.copyOf(results.subList(0, limit));
    }

    public List<Integer> extractDriverNumbers(List<SessionResult> results) {
        return results.stream()
                .map(SessionResult::driverNumber)
                .distinct()
                .toList();
    }
}
