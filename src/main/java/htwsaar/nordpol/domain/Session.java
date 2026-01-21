package htwsaar.nordpol.domain;

public record Session(int sessionKey,
                      int meetingKey,
                      String sessionName,
                      String sessionType) {
}

