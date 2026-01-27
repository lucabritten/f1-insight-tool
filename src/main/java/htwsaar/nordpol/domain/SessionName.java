package htwsaar.nordpol.domain;

import java.util.Arrays;

public enum SessionName {

    PRACTICE1("Practice 1", "FP1"),
    PRACTICE2("Practice 2", "FP2"),
    PRACTICE3("Practice 3", "FP3"),
    QUALIFYING("Qualifying", "Quali"),
    SPRINT_SHOOTOUT("Sprint Shootout", "SprintQuali"),
    SPRINT("Sprint", "Sprint"),
    RACE("Race", "GP");

    private final String dbValue;
    private final String[] aliases;

    SessionName(String dbValue,  String... aliases) {
        this.dbValue = dbValue;
        this.aliases = aliases;
    }

    public String dbValue() {
        return dbValue;
    }

    public static SessionName fromString(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Session name must not be null or empty.");

        String v = value.trim();
        for (SessionName t : values()) {
            if (t.dbValue.equalsIgnoreCase(v)) return t;
            if (Arrays.stream(t.aliases).anyMatch(a -> a.equalsIgnoreCase(v))) return t;
        }
        throw new IllegalArgumentException("Unknown session name: " + value);
    }

    public String displayName() {
        return dbValue;
    }
}