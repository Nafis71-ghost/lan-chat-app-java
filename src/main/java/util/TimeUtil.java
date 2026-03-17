package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

    private TimeUtil() {}

    public static String now() {
        return LocalDateTime.now().format(FORMAT);
    }
}