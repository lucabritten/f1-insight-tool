package htwsaar.nordpol.cli;

import htwsaar.nordpol.cli.view.FastestLapWithContext;
import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.Meeting;
import htwsaar.nordpol.domain.Session;
import htwsaar.nordpol.domain.SessionName;
import htwsaar.nordpol.service.LapService;
import htwsaar.nordpol.service.MeetingService;
import htwsaar.nordpol.service.SessionService;
import htwsaar.nordpol.util.Formatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "fastest-lap",
        description = "Print the fastest lap for a given location, year, and session",
        mixinStandardHelpOptions = true
)
public class FastestLapCommand implements Runnable {

    @Option(
            names = {"--location", "-l"},
            description = "The location of the race (e.g. Austin)",
            required = true
    )
    private String location;

    @Option(
            names = {"--year", "-y"},
            description = "The season year",
            defaultValue = "2024"
    )
    private int year;

    @Option(
            names = {"--sessionName", "-sn"},
            description = "The session name (e.g. Race, Qualifying, Practice)",
            required = true
    )
    private String sessionName;

    @Option(
            names = {"--driverNumber", "-dn"},
            description = "Driver number to filter fastest lap within the session"
    )
    private Integer driverNumber;

    private final LapService lapService;
    private final MeetingService meetingService;
    private final SessionService sessionService;

    public FastestLapCommand() {
        this(ApplicationContext.lapService(),
                ApplicationContext.meetingService(),
                ApplicationContext.sessionService());
    }

    public FastestLapCommand(LapService lapService,
                             MeetingService meetingService,
                             SessionService sessionService) {
        this.lapService = lapService;
        this.meetingService = meetingService;
        this.sessionService = sessionService;
    }

    @Override
    public void run() {
        try {
            Meeting meeting = meetingService.getMeetingByYearAndLocation(year, location);
            Session session = sessionService.getSessionByMeetingKeyAndSessionName(
                    meeting.meetingKey(),
                    SessionName.fromString(sessionName)
            );

            Lap fastestLap = driverNumber == null
                    ? lapService.getFastestLapBySessionKey(session.sessionKey())
                    : lapService.getFastestLapBySessionKeyAndDriverNumber(session.sessionKey(), driverNumber);
            String output = Formatter.formatFastestLap(
                    new FastestLapWithContext(
                            meeting.location(),
                            session.sessionName().name(),
                            fastestLap
                    )
            );
            System.out.println(output);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
