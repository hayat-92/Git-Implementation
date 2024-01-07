package git.domain;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public record AuthorSignature(String login, String email, ZonedDateTime when) {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("XX");

    public String format(){
        var timestamp = when.toEpochSecond();
        var timezone = when.format(DATE_FORMATTER);
        return String.format("%s <%s> %d %s", login, email, timestamp, timezone);

    }
}
