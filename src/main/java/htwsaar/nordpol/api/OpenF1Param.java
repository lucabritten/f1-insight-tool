package htwsaar.nordpol.api;

public enum OpenF1Param {

    SESSION_KEY("session_key"),
    SESSION_NAME("session_name"),
    DRIVER_NUMBER("driver_number"),
    YEAR("year"),
    LOCATION("location"),
    FIRST_NAME("first_name"),
    LAST_NAME("last_name"),
    MEETING_KEY("meeting_key");

    private final String apiName;

    OpenF1Param(String apiName) {
        this.apiName = apiName;
    }

    public String apiName() {
        return apiName;
    }
}
