package htwsaar.nordpol.api;

public enum OpenF1Endpoint {
    DRIVERS("/drivers"),
    LAPS("/laps"),
    MEETINGS("/meetings"),
    SESSIONS("/sessions"),
    SESSION_RESULTS("/session_result"),
    WEATHER("/weather"),
    TEST("/test");

    private final String path;

    OpenF1Endpoint(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
