package util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class DateUtil {
    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private DateUtil() { }

    public static LocalDate fromDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static long nights(LocalDate in, LocalDate out) {
        return ChronoUnit.DAYS.between(in, out);
    }

    /** Two stays [aIn, aOut) and [bIn, bOut) overlap when each starts before the other ends. */
    public static boolean overlaps(LocalDate aIn, LocalDate aOut, LocalDate bIn, LocalDate bOut) {
        return aIn.isBefore(bOut) && bIn.isBefore(aOut);
    }
}
