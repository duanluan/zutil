package top.csaf.date.constant;

import java.time.temporal.ChronoUnit;

public class DateDuration {

  public static final long MILLIS_1000 = 1000;
  public static final long WEEK_MILLIS = ChronoUnit.WEEKS.getDuration().getSeconds() * MILLIS_1000;
  public static final long DAY_OF_MONTH_MILLIS = ChronoUnit.DAYS.getDuration().getSeconds() * MILLIS_1000;
  public static final long HOUR_MILLIS = ChronoUnit.HOURS.getDuration().getSeconds() * MILLIS_1000;
  public static final long MINUTE_MILLIS = ChronoUnit.MINUTES.getDuration().getSeconds() * MILLIS_1000;
  public static final long SECOND_MILLIS = ChronoUnit.SECONDS.getDuration().getSeconds() * MILLIS_1000;
}
