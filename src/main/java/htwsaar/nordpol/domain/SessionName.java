package htwsaar.nordpol.domain;

import java.util.Arrays;

public enum SessionName {

    PRACTICE1("Practice 1", "Practice%201", "FP1"),
    PRACTICE2("Practice 2","Practice%202", "FP2"),
    PRACTICE3("Practice 3","Practice%20", "FP3"),
    QUALIFYING("Qualifying","Qualifying", "Quali"),
    SPRINT_SHOOTOUT("Sprint%20Shootout", "SprintQuali"),
    SPRINT("Sprint", "Sprint"),
    RACE("Race", "Race", "GP");

    private final String dbValue;        // what we store/send to DB/API
    private final String[] aliases;        // accepted inputs
    private final String apiValue;

    SessionName(String dbValue, String apiValue,  String... aliases) {
        this.dbValue = dbValue;
        this.apiValue = apiValue;
        this.aliases = aliases;
    }

    public String dbValue() {
        return dbValue;
    }

    public String apiValue(){
        return apiValue;
    }

    public static SessionName fromString(String value) {
        if (value == null) return null; // or throw IllegalArgumentException
        String v = value.trim();
        for (SessionName t : values()) {
            if (t.dbValue.equalsIgnoreCase(v)) return t;
            if (Arrays.stream(t.aliases).anyMatch(a -> a.equalsIgnoreCase(v))) return t;
        }
        throw new IllegalArgumentException("Unknown session name: " + value);
    }

}
