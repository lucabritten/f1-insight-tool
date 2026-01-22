package htwsaar.nordpol.domain;

public record Session(int sessionKey,
                      int meetingKey,
                      SessionName sessionName,
                      String sessionType) {
}

