package htwsaar.nordpol.presentation.cli.converter;

import htwsaar.nordpol.domain.SessionName;
import picocli.CommandLine;

public class SessionNameConverter implements CommandLine.ITypeConverter<SessionName> {

    @Override
    public SessionName convert(String value) {
        return SessionName.fromString(value);
    }
}
