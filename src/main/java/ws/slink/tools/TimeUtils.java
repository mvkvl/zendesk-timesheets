package ws.slink.tools;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

    public enum OffsetUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    /**
     *
     * offsetStr format: [-]N{smhdwMy}
     *
     * s - seconds
     * m - minutes
     * h - hours
     * d - days
     * w - weeks
     * M - months
     * y - years
     *
     *
     * @param offsetStr
     * @return
     */
    public static Instant offset(String offsetStr) {
        return offset(Instant.now(), offsetStr);
    }
    public static Instant offset(String offsetStr, TimeZone timeZone) {
        return offset(Instant.now(), offsetStr, timeZone);
    }
    public static Instant offset(Instant referenceDate, String offsetStr) {
        return offset(referenceDate, offsetStr, null);
    }
    public static Instant offset(Instant referenceDate, String offsetStr, TimeZone timeZone) {
        if (StringUtils.isBlank(offsetStr) || offsetStr.equals("*") || offsetStr.equals("all"))
            return new Date(0).toInstant();
        String unit = offsetStr.substring(offsetStr.length() - 1);
        int offset = Integer.parseInt(offsetStr.substring(0, offsetStr.length() - 1));
        String sign = offset > 0 ? "+" : "-";
        offset = (offset > 0) ? offset : -offset;
        LocalDateTime ldt;
        if (null == timeZone)
            ldt = LocalDateTime.from(referenceDate.atZone(ZoneId.systemDefault()));
        else
            ldt = LocalDateTime.from(referenceDate.atZone(timeZone.toZoneId()));
        switch(unit) {
            case "s":
                return sign.equals("+") ? ldt.plusSeconds(offset).toInstant(ZoneOffset.UTC) : ldt.minusSeconds(offset).toInstant(ZoneOffset.UTC);
            case "m":
                return sign.equals("+") ? ldt.plusMinutes(offset).toInstant(ZoneOffset.UTC) : ldt.minusMinutes(offset).toInstant(ZoneOffset.UTC);
            case "h":
                return sign.equals("+") ? ldt.plusHours(offset).toInstant(ZoneOffset.UTC) : ldt.minusHours(offset).toInstant(ZoneOffset.UTC);
            case "d":
                return sign.equals("+") ? ldt.plusDays(offset).toInstant(ZoneOffset.UTC) : ldt.minusDays(offset).toInstant(ZoneOffset.UTC);
            case "w":
                return sign.equals("+") ? ldt.plusWeeks(offset).toInstant(ZoneOffset.UTC) : ldt.minusWeeks(offset).toInstant(ZoneOffset.UTC);
            case "M":
                return sign.equals("+") ? ldt.plusMonths(offset).toInstant(ZoneOffset.UTC) : ldt.minusMonths(offset).toInstant(ZoneOffset.UTC);
            case "y":
                return sign.equals("+") ? ldt.plusYears(offset).toInstant(ZoneOffset.UTC) : ldt.minusYears(offset).toInstant(ZoneOffset.UTC);
            default:
                throw new IllegalArgumentException("incorrect offset string: " + offsetStr);
        }
    }

    public static Instant roundTo(Instant date, OffsetUnit unit) {
        return roundTo(date, unit, null);
    }
    public static Instant roundTo(Instant date, OffsetUnit unit, TimeZone atZone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date.toEpochMilli()));
        if (null != atZone)
            calendar.setTimeZone(atZone);
        switch(unit) {
            case YEAR:
                calendar.set(Calendar.MONTH, 0);
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            case DAY:
                calendar.set(Calendar.HOUR_OF_DAY, 0);
            case HOUR:
                calendar.set(Calendar.MINUTE, 0);
            case MINUTE:
                calendar.set(Calendar.SECOND, 0);
            case SECOND:
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case WEEK:
                int w = (calendar.get(Calendar.DAY_OF_WEEK) == 1) ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1;
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, d - w + 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            default:
                throw new IllegalArgumentException("incorrect offset unit passed: " + unit);
        }
        return calendar.getTime().toInstant();
    }

    public static String timeString(Instant instant) {
        return timeString(instant, TimeZone.getDefault());
    }
    public static String timeString(Instant instant, TimeZone timeZone) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        if (null != timeZone)
            sf.setTimeZone(timeZone);
        return sf.format(new Date(instant.toEpochMilli()));
    }

    public static void main(String[] args) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        sdf.setTimeZone(tz);

        Instant i = Instant.now();
        System.out.println("SE: " + timeString(roundTo(i, OffsetUnit.SECOND, tz)));
        System.out.println("MI: " + timeString(roundTo(i, OffsetUnit.MINUTE, tz)));
        System.out.println("HR: " + timeString(roundTo(i, OffsetUnit.HOUR, tz)));
        System.out.println("DY: " + timeString(roundTo(i, OffsetUnit.DAY, tz)));
        System.out.println("WK: " + timeString(roundTo(i, OffsetUnit.WEEK, tz)));
        System.out.println("MO: " + timeString(roundTo(i, OffsetUnit.MONTH, tz)));
        System.out.println("YR: " + timeString(roundTo(i, OffsetUnit.YEAR, tz)));

        System.out.println();
        System.out.println(Instant.now() + " -> " + TimeUtils.offset("-1m"));
        System.out.println(Instant.now() + " -> " + TimeUtils.offset( "1m"));
        System.out.println();
        System.out.println(Instant.now() + " -> " + TimeUtils.offset("-1h"));
        System.out.println(Instant.now() + " -> " + TimeUtils.offset( "1h" ));
        System.out.println();
        System.out.println(Instant.now() + " -> " + TimeUtils.offset("-1d"));
        System.out.println(Instant.now() + " -> " + TimeUtils.offset( "1d"));
        System.out.println();
        System.out.println(Instant.now() + " -> " + TimeUtils.offset("-1w"));
        System.out.println(Instant.now() + " -> " + TimeUtils.offset( "1w"));
        System.out.println();
        System.out.println(Instant.now() + " -> " + TimeUtils.offset("-1M"));
        System.out.println(Instant.now() + " -> " + TimeUtils.offset( "1M"));
        System.out.println();
        System.out.println(Instant.now() + " -> " + TimeUtils.offset("-1y"));
        System.out.println(Instant.now() + " -> " + TimeUtils.offset( "1y"));
    }

}
