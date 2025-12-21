package top.csaf.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.date.DateFeat;
import top.csaf.date.DateUtil;
import top.csaf.date.constant.DateConst;
import top.csaf.date.constant.DatePattern;
import top.csaf.util.ReflectionTestUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("时间工具类测试")
class DateUtilTest {

  private final ZonedDateTime nowZonedDateTime = ZonedDateTime.now();
  private final LocalDateTime nowLocalDateTime = LocalDateTime.now();
  private final LocalDate nowLocalDate = LocalDate.now();
  private final LocalTime nowLocalTime = LocalTime.now();
  private final Date nowDate = new Date();
  private final long currentTimeMillis = System.currentTimeMillis();
  private final String customSource = "21/08/08 08.08.08";
  private final String customPattern = "uu/MM/dd HH.mm.ss";

  private void println(Object source) {
    System.out.println(source);
  }

  @DisplayName("探寻 ChronoField")
  @Test
  void testChronoField() {
    String indent = "\t\t\t\t\t\t\t\t\t\t\t\t\t";
    LocalDateTime now = nowLocalDateTime;
    // 时代：公元前，相当于当前时间的负数
    println(now.with(ChronoField.ERA, 0) + indent.replaceFirst("\t", "") + "时代：公元前");
    // 时代：公元，即当前时间
    println(now.with(ChronoField.ERA, 1) + indent + "时代：公元");
    // 公元前所属年：以当前时间为基础，年修改为公元前 2 年，结果 -0001-10-01T02:30:32.723 加上当前时间的月份往后 10-01T02:30:32.723 为 2 年
    println(now.with(ChronoField.ERA, 0).with(ChronoField.YEAR_OF_ERA, 2) + indent.replaceFirst("\t", "") + "公元前所属年");
    // 公元后所属年：以当前年月为基础，年修改为 2 年
    println(now.with(ChronoField.ERA, 1).with(ChronoField.YEAR_OF_ERA, 2) + indent + "公元后所属年");
    // 年
    println(now.with(ChronoField.YEAR, 2020) + indent + "年");
    // 预期月，从 0 年开始计算月（从 0 开始），2021 年 10 月的值为 2021 * 12 + 10 - 1
    println(now.with(ChronoField.PROLEPTIC_MONTH, 0) + indent + "预期月");
    // 年的月
    println(now.with(ChronoField.MONTH_OF_YEAR, 9) + indent + "年的月");
    // 年的对齐周：年的第一天为第一周的第一天
    println(now.with(ChronoField.ALIGNED_WEEK_OF_YEAR, 2) + indent + "年的对齐周");
    // 月的对齐周：月的第一天为此月第一周的第一天
    println(now.with(ChronoField.ALIGNED_WEEK_OF_MONTH, 2) + indent + "月的对齐周");
    // 年的对齐周的天
    println(now.with(ChronoField.ALIGNED_WEEK_OF_YEAR, 2).with(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR, 5) + indent + "年的对齐周的天");
    // 月的对齐周的天
    println(now.with(ChronoField.ALIGNED_WEEK_OF_MONTH, 2).with(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH, 5) + indent + "月的对齐周的天");
    // 年的天
    println(now.with(ChronoField.DAY_OF_YEAR, 1) + indent + "年的天");
    // 月的天
    println(now.with(ChronoField.DAY_OF_MONTH, 1) + indent + "月的天");
    // 周的天
    println(now.with(ChronoField.DAY_OF_WEEK, 1) + indent + "周的天");
    // 以 1970-01-01 为 0 开始的天（忽略偏移量和时区）
    println(now.with(ChronoField.EPOCH_DAY, 1) + indent + "以 1970-01-01 为 0 开始的天（忽略偏移量和时区）");
    // 上午（0-12）
    println(now.with(ChronoField.AMPM_OF_DAY, 0) + indent + "上午（0-12）");
    // 下午（13-23）
    println(now.with(ChronoField.AMPM_OF_DAY, 1) + indent + "下午（13-23）");
    // 上午或下午的小时，以当前 AMPM 为准，从 0 开始
    println(now.with(ChronoField.HOUR_OF_AMPM, 0) + indent + "上午或下午的小时");
    // 上午的小时
    println(now.with(ChronoField.AMPM_OF_DAY, 0).with(ChronoField.HOUR_OF_AMPM, 0) + indent + "上午的小时");
    // 下午的小时
    println(now.with(ChronoField.AMPM_OF_DAY, 1).with(ChronoField.HOUR_OF_AMPM, 0) + indent + "下午的小时");
    // 12 小时制，以当前 AMPM 为准，从 1 开始
    println(now.with(ChronoField.CLOCK_HOUR_OF_AMPM, 1) + indent + "12 小时制");
    // 上午的 12 小时制小时
    println(now.with(ChronoField.AMPM_OF_DAY, 0).with(ChronoField.CLOCK_HOUR_OF_AMPM, 1) + indent + "上午的 12 小时制小时");
    // 下午的 12 小时制小时
    println(now.with(ChronoField.AMPM_OF_DAY, 1).with(ChronoField.CLOCK_HOUR_OF_AMPM, 1) + indent + "下午的 12 小时制小时");
    // 24 小时制，从 1 开始，24 就是 00:00（非第二天）
    println(now.with(ChronoField.CLOCK_HOUR_OF_DAY, 24) + indent + "24 小时制");
    // 天的小时
    println(now.with(ChronoField.HOUR_OF_DAY, 0) + indent + "天的小时");
    // 天的分钟
    println(now.with(ChronoField.MINUTE_OF_DAY, 1) + indent + "天的分钟");
    // 小时的分钟
    println(now.with(ChronoField.MINUTE_OF_HOUR, 1) + indent + "小时的分钟");
    // 天的秒
    println(now.with(ChronoField.SECOND_OF_DAY, 1) + indent + "天的秒");
    // 分钟的秒
    println(now.with(ChronoField.SECOND_OF_MINUTE, 1) + indent + "分钟的秒");
    // 以 1970-01-01T00:00Z (ISO) 为 0 开始的秒，必须和时区结合使用（+时区小时）
    println(now.atZone(ZoneId.systemDefault()).with(ChronoField.INSTANT_SECONDS, 1) + "\t\t以 1970-01-01T00:00Z (ISO) 为 0 开始的秒，必须和时区结合使用（+时区小时）");
    // 天的毫秒
    println(now.with(ChronoField.MILLI_OF_DAY, 1) + indent + "天的毫秒");
    // 秒的毫秒
    println(now.with(ChronoField.MILLI_OF_SECOND, 1) + indent + "秒的毫秒");
    // 天的微秒
    println(now.with(ChronoField.MICRO_OF_DAY, 1) + indent.replaceFirst("\t\t", "") + "天的微秒");
    // 秒的微秒
    println(now.with(ChronoField.MICRO_OF_SECOND, 1) + indent.replaceFirst("\t\t", "") + "秒的微秒");
    // 天的纳秒
    println(now.with(ChronoField.NANO_OF_DAY, 1) + indent.replaceFirst("\t\t\t", "") + "天的纳秒");
    // 秒的纳秒
    println(now.with(ChronoField.NANO_OF_SECOND, 1) + indent.replaceFirst("\t\t\t", "") + "秒的纳秒");

  }

  @DisplayName("探寻时区")
  @Test
  void atZone() {
    LocalDateTime now = nowLocalDateTime;
    // 无时区信息
    println(now);

    // 系统区域，atZone 只是给对象赋时区，并不会修改小时
    println(now.atZone(DateConst.SYSTEM_ZONE_ID));
    // 设置区域
    println(now.atZone(ZoneId.of("Asia/Shanghai")));
    // 设置偏移量
    println(now.atZone(ZoneOffset.ofHours(1)));

    now = now.withMonth(8);
    // 指定时区修改小时，本地和 UTC 相差多少就会加上多少，比如 Asia/Shanghai 和 UTC 相差 -8 小时
    println(now.atZone(DateConst.SYSTEM_ZONE_ID).withZoneSameInstant(ZoneOffset.UTC));
    // 指定时区，其他保持不变，TODO 但会转换为此区域的有效时间（夏令时）？
    println(now.atZone(DateConst.SYSTEM_ZONE_ID).withZoneSameLocal(ZoneOffset.ofHours(-7)));
    // 指定区域，其他保持不变
    println(now.atZone(DateConst.SYSTEM_ZONE_ID).withZoneSameLocal(ZoneId.of("America/Los_Angeles")));
    // 将区域设置为偏移量（ISO-8601），方便网络传输
    println(now.atZone(DateConst.SYSTEM_ZONE_ID).withZoneSameLocal(ZoneId.of("America/Los_Angeles")).withFixedOffsetZone());

    Instant instant = now.atZone(DateConst.SYSTEM_ZONE_ID).toInstant();
    // 夏令时在 ZoneOffset 当中表现，同一时区（使用夏令时），不同月份，甚至不同年份（tz database），夏季和冬季的 ZoneOffset 不同：https://www.itranslater.com/qa/details/2326279995771061248、https://en.wikipedia.org/wiki/Tz_database
    println(ZoneId.of("America/Los_Angeles").getRules().getOffset(instant));
    instant = now.withMonth(1).atZone(DateConst.SYSTEM_ZONE_ID).toInstant();
    println(ZoneId.of("America/Los_Angeles").getRules().getOffset(instant));
  }


  @DisplayName("getFormatter：获取 DateTimeFormatter 对象")
  @Test
  void getFormatter() {
    // 格式中不含年
    DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern(DatePattern.MM_DD).toFormatter();
    // 获取年报错：Unsupported field: Year
    try {
      dateTimeFormatter.parse("08-08").get(ChronoField.YEAR);
    } catch (Exception e) {
      log.warn(e.getMessage());
    }

    assertThrows(IllegalArgumentException.class, () -> DateUtil.getFormatter("", null, null,null));
    // 使用 getFormatter() 时会赋默认值
    assertEquals(DateUtil.getFormatter(DatePattern.MM_DD, Locale.ENGLISH, ZoneId.systemDefault()).parse("08-08").get(ChronoField.YEAR), 0);
    DateFeat.set((Locale) null);
    assertEquals(DateUtil.getFormatter(DatePattern.MM_DD, null, ZoneId.systemDefault()).parse("08-08").get(ChronoField.YEAR), 0);

    // 地区为简体中文，可以解析星期几
    assertEquals(DateUtil.getFormatter("MM-dd EEEE", Locale.SIMPLIFIED_CHINESE).parse("08-15 星期二").get(ChronoField.DAY_OF_WEEK), 2);
    // ZonedDateTime.parse 后时区为 +8，但本地时区就是 +8，所以此时 getHour() 为 0，再 withZoneSameInstant 转换为 UTC 时区，少 8 小时，所以 getHour() 会 -8，就是 16
    assertEquals(ZonedDateTime.parse("00", DateUtil.getFormatter("HH", ZoneOffset.ofHours(8))).withZoneSameInstant(ZoneOffset.UTC).getHour(), 16);

    assertEquals(DateUtil.getFormatter(DatePattern.MM_DD).parse("08-26").get(ChronoField.YEAR), 0);
  }

  @DisplayName("convertByPattern：转换需要格式化的字符串，比如英文月份转换为首字母大写")
  @Test
  void convertSource() {
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(DateUtil.class, "convertSource", "mon", ""));
    assertEquals(ReflectionTestUtil.invokeMethod(DateUtil.class, "convertSource", "mon", "MMM"), "Mon");
  }

  @DisplayName("convertMonth[Short]Text：转换数字月到[短]文本月")
  @Test
  void convertMonthText() {
    assertThrows(IllegalArgumentException.class, () -> DateUtil.convertMonthShortText("", Locale.SIMPLIFIED_CHINESE));
    assertThrows(IllegalArgumentException.class, () -> DateUtil.convertMonthShortText("000", Locale.SIMPLIFIED_CHINESE));

    assertEquals(DateUtil.convertMonthShortText("9"), "Sep");
    assertEquals(DateUtil.convertMonthShortText("09", Locale.CHINESE), "9月");
    assertEquals(DateUtil.convertMonthShortText("9", Locale.FRENCH), "sept.");
    assertNull(DateUtil.convertMonthShortText("13", Locale.US));

    assertEquals(DateUtil.convertMonthText("9"), "September");
    assertEquals(DateUtil.convertMonthText("09", Locale.CHINESE), "九月");
    assertEquals(DateUtil.convertMonthText("9", Locale.FRENCH), "septembre");
    assertNull(DateUtil.convertMonthText("13", Locale.US));
  }

  @DisplayName("format：格式化为字符串")
  @Test
  void format() {
    ZoneOffset zoneOffset = ZoneOffset.ofHours(-18);

    println("ZonedDateTime 指定时区和格式" + DateUtil.format(nowZonedDateTime, zoneOffset, DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("ZonedDateTime 指定格式：" + DateUtil.format(nowZonedDateTime, DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("ZonedDateTime 指定时区：" + DateUtil.format(nowZonedDateTime, zoneOffset));
    println("ZonedDateTime：" + DateUtil.format(nowZonedDateTime));

    println("LocalDateTime 指定时区和格式：" + DateUtil.format(nowLocalDateTime, zoneOffset, DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("LocalDateTime 指定格式：" + DateUtil.format(nowLocalDateTime, DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("LocalDateTime 指定时区：" + DateUtil.format(nowLocalDateTime, zoneOffset));
    println("LocalDateTime：" + DateUtil.format(nowLocalDateTime));

    println("LocalDate 指定时区和格式：" + DateUtil.format(nowLocalDate, zoneOffset, DatePattern.UUUU_MM_DD));
    println("LocalDate 指定格式：" + DateUtil.format(nowLocalDate, DatePattern.UUUU_MM_DD));
    println("LocalDate 指定时区：" + DateUtil.format(nowLocalDate, zoneOffset));
    println("LocalDate：" + DateUtil.format(nowLocalDate));

    println("LocalTime 指定时区和格式：" + DateUtil.format(nowLocalTime, zoneOffset, DatePattern.HH_MM_SS));
    println("LocalTime 指定格式：" + DateUtil.format(nowLocalTime, DatePattern.HH_MM_SS));
    println("LocalTime 指定时区：" + DateUtil.format(nowLocalTime, zoneOffset));
    println("LocalTime：" + DateUtil.format(nowLocalTime));

    println("Date 指定时区和格式：" + DateUtil.format(nowDate, zoneOffset, DatePattern.YYYY_MM_DD_HH_MM_SS));
    println("Date 指定格式：" + DateUtil.format(nowDate, DatePattern.YYYY_MM_DD_HH_MM_SS));
    println("Date 指定时区：" + DateUtil.format(nowDate, zoneOffset));
    println("Date：" + DateUtil.format(nowDate));

    println("Long 指定时区和格式：" + DateUtil.format(currentTimeMillis, zoneOffset, DatePattern.YYYY_MM_DD_HH_MM_SS));
    println("Long 指定格式：" + DateUtil.format(currentTimeMillis, DatePattern.YYYY_MM_DD_HH_MM_SS));
    println("Long 指定时区：" + DateUtil.format(currentTimeMillis, zoneOffset));
    println("Long：" + DateUtil.format(currentTimeMillis));
  }

  @DisplayName("formatCountdown：格式化为倒计时字符串")
  @Test
  void formatCountdown() {
    println(DateUtil.formatCountdown(Math.abs(DateUtil.between(nowLocalDateTime, DateUtil.parseLocalDateTime(nowLocalDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli() - 691200000), ChronoUnit.MILLIS)), "W周dd天HH时mm分ss秒SSS毫秒"));
    println(DateUtil.formatCountdown(Math.abs(DateUtil.between(nowLocalDateTime, DateUtil.parseLocalDateTime(nowLocalDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli() - 691200000), ChronoUnit.MILLIS)), false, null, "天", "小时", "分", "秒", "毫秒"));
  }

  @DisplayName("toDate：转换为 Date 对象")
  @Test
  void toDate() {
    // 当前时区偏移量为 8，指定时区偏移量为 18，则小时数会 +(18-8)
    ZoneOffset zoneOffset = ZoneOffset.ofHours(18);

    println("ZonedDateTime 指定时区：" + DateUtil.toDate(nowZonedDateTime, zoneOffset));
    println("LocalDateTime 指定时区：" + DateUtil.toDate(nowLocalDateTime, zoneOffset));
    // LocalDate 的时分秒此处默认为 0，所以时区偏移量 >=0 都没有什么意义
    println("LocalDate 指定时区：" + DateUtil.toDate(nowLocalDate, zoneOffset));
    println("LocalTime 指定时区：" + DateUtil.toDate(nowLocalTime, zoneOffset));

    println("ZonedDateTime：" + DateUtil.toDate(nowZonedDateTime));
    println("LocalDateTime：" + DateUtil.toDate(nowLocalDateTime));
    println("LocalDate：" + DateUtil.toDate(nowLocalDate));
    println("LocalTime：" + DateUtil.toDate(nowLocalTime));
  }

  @DisplayName("parse：解析为时间对象")
  @Test
  void parse() {
    // 当前时区偏移量为 8，指定时区偏移量为 18，则小时数会 +(18-8)
    ZoneOffset zoneOffset = ZoneOffset.ofHours(18);
    String yyyyMMddHHmmss = "2021-08-08 08:08:08";
    String yyyyMMddHHmm = "2021-08-08 08:08";
    String yyyyMMdd = "2021-08-08";
    String yyyyMM = "2021-08";
    String HHmmss = "08:08:08";
    String HHmm = "08:08";

    println("LocalDateTime 解析时间戳指定时区：" + DateUtil.parseLocalDateTime(currentTimeMillis, zoneOffset));
    println("LocalDateTime 指定时区和格式" + DateUtil.parseLocalDateTime(customSource, zoneOffset, customPattern));
    println("LocalDateTime 指定时区预定格式（uuuu-MM-dd HH:mm:ss）：" + DateUtil.parseLocalDateTime(yyyyMMddHHmmss, zoneOffset));
    println("LocalDateTime 指定时区预定格式（uuuu-MM-dd HH:mm）：" + DateUtil.parseLocalDateTime(yyyyMMddHHmm, zoneOffset));
    println("LocalDateTime 指定时区预定格式（uuuu-MM-dd）：" + DateUtil.parseLocalDateTime(yyyyMMdd, zoneOffset));
    println("LocalDateTime 指定时区预定格式（uuuu-MM）：" + DateUtil.parseLocalDateTime(yyyyMM, zoneOffset));
    println("LocalDateTime 指定时区预定格式（HH:mm:ss）：" + DateUtil.parseLocalDateTime(HHmmss, zoneOffset));
    println("LocalDateTime 指定时区预定格式（HH:mm）：" + DateUtil.parseLocalDateTime(HHmm, zoneOffset));
    println("——————————————————");
    println("LocalDateTime 解析时间戳：" + DateUtil.parseLocalDateTime(currentTimeMillis));
    println("LocalDateTime 指定格式" + DateUtil.parseLocalDateTime(customSource, customPattern));
    println("LocalDateTime 预定格式（uuuu-MM-dd HH:mm:ss）：" + DateUtil.parseLocalDateTime(yyyyMMddHHmmss));
    println("LocalDateTime 预定格式（uuuu-MM-dd HH:mm）：" + DateUtil.parseLocalDateTime(yyyyMMddHHmm));
    println("LocalDateTime 预定格式（uuuu-MM-dd）：" + DateUtil.parseLocalDateTime(yyyyMMdd));
    println("LocalDateTime 预定格式（uuuu-MM）：" + DateUtil.parseLocalDateTime(yyyyMM));
    println("LocalDateTime 预定格式（HH:mm:ss）：" + DateUtil.parseLocalDateTime(HHmmss));
    println("LocalDateTime 预定格式（HH:mm）：" + DateUtil.parseLocalDateTime(HHmm));
    println("——————————————————");
    println("LocalDate 解析时间戳指定时区：" + DateUtil.parseLocalDate(currentTimeMillis, zoneOffset));
    println("LocalDate 指定时区和格式" + DateUtil.parseLocalDate(customSource, zoneOffset, customPattern));
    println("LocalDate 指定时区预定格式（uuuu-MM-dd）：" + DateUtil.parseLocalDate(yyyyMMdd, zoneOffset));
    println("LocalDate 指定时区预定格式（uuuu-MM）：" + DateUtil.parseLocalDate(yyyyMM, zoneOffset));
    println("LocalDate 指定时区预定格式（HH:mm:ss）：" + DateUtil.parseLocalDateTime(HHmmss, zoneOffset));
    println("LocalDate 指定时区预定格式（HH:mm）：" + DateUtil.parseLocalDateTime(HHmm, zoneOffset));
    println("——————————————————");
    println("LocalDate 解析时间戳：" + DateUtil.parseLocalDate(currentTimeMillis));
    println("LocalDate 指定格式" + DateUtil.parseLocalDate(customSource, customPattern));
    println("LocalDate 预定格式（uuuu-MM-dd）：" + DateUtil.parseLocalDate(yyyyMMdd));
    println("LocalDate 预定格式（uuuu-MM）：" + DateUtil.parseLocalDate(yyyyMM));
    println("LocalDate 预定格式（HH:mm:ss）：" + DateUtil.parseLocalDateTime(HHmmss));
    println("LocalDate 预定格式（HH:mm）：" + DateUtil.parseLocalDateTime(HHmm));
    println("——————————————————");
    println("LocalTime 解析时间戳指定时区：" + DateUtil.parseLocalTime(currentTimeMillis, zoneOffset));
    println("LocalTime 指定时区和格式" + DateUtil.parseLocalTime(customSource, zoneOffset, customPattern));
    println("LocalTime 指定时区预定格式（HH:mm:ss）：" + DateUtil.parseLocalTime(HHmmss, zoneOffset));
    println("LocalTime 指定时区预定格式（HH:mm）：" + DateUtil.parseLocalTime(HHmm, zoneOffset));
    println("——————————————————");
    println("LocalTime 解析时间戳：" + DateUtil.parseLocalTime(currentTimeMillis));
    println("LocalTime 指定格式" + DateUtil.parseLocalTime(customSource, customPattern));
    println("LocalTime 预定格式（HH:mm:ss）：" + DateUtil.parseLocalTime(HHmmss));
    println("LocalTime 预定格式（HH:mm）：" + DateUtil.parseLocalTime(HHmm));
    println("——————————————————");
    println("Date 解析时间戳指定时区：" + DateUtil.parseDate(currentTimeMillis, zoneOffset));
    println("Date 指定时区和格式" + DateUtil.parseDate(customSource, zoneOffset, customPattern));
    println("Date 指定时区预定格式（uuuu-MM-dd HH:mm:ss）：" + DateUtil.parseDate(yyyyMMddHHmmss, zoneOffset));
    println("Date 指定时区预定格式（uuuu-MM-dd HH:mm）：" + DateUtil.parseDate(yyyyMMddHHmm, zoneOffset));
    println("Date 指定时区预定格式（uuuu-MM-dd）：" + DateUtil.parseDate(yyyyMMdd, zoneOffset));
    println("Date 指定时区预定格式（uuuu-MM）：" + DateUtil.parseDate(yyyyMM, zoneOffset));
    println("Date 指定时区预定格式（HH:mm:ss）：" + DateUtil.parseDate(HHmmss, zoneOffset));
    println("Date 指定时区预定格式（HH:mm）：" + DateUtil.parseDate(HHmm, zoneOffset));
    println("——————————————————");
    println("Date 解析时间戳：" + DateUtil.parseDate(currentTimeMillis));
    println("Date 指定格式" + DateUtil.parseDate(customSource, customPattern));
    println("Date 预定格式（uuuu-MM-dd HH:mm:ss）：" + DateUtil.parseDate(yyyyMMddHHmmss));
    println("Date 预定格式（uuuu-MM-dd HH:mm）：" + DateUtil.parseDate(yyyyMMddHHmm));
    println("Date 预定格式（uuuu-MM-dd）：" + DateUtil.parseDate(yyyyMMdd));
    println("Date 预定格式（uuuu-MM）：" + DateUtil.parseDate(yyyyMM));
    println("Date 预定格式（HH:mm:ss）：" + DateUtil.parseDate(HHmmss));
    println("Date 预定格式（HH:mm）：" + DateUtil.parseDate(HHmm));

    assertEquals(LocalDateTime.of(2024,1,1,12,0),DateUtil.parseLocalDateTime("2024/01/01 12:00:00","yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"));
  }

  @DisplayName("withZoneInstant：转换时区")
  @Test
  void withZoneInstant() {
    // 旧时区和新时区之间差 10 小时，所以小时数会 +10
    ZoneOffset oldZoneOffset = ZoneOffset.UTC;
    // 默认时区（此处为 8）和新时区之间差 2 小时，所以小时数会 +2
    ZoneOffset newZoneOffset = ZoneOffset.ofHours(10);

    println("LocalDateTime 旧时区转新时区：" + DateUtil.withZoneInstant(nowLocalDateTime, oldZoneOffset, newZoneOffset));
    println("LocalDateTime 默认时区转新时区：" + DateUtil.withZoneInstant(nowLocalDateTime, newZoneOffset));
    println("LocalDate 旧时区转新时区：" + DateUtil.withZoneInstant(nowLocalDate, oldZoneOffset, newZoneOffset));
    println("LocalDate 默认时区转新时区：" + DateUtil.withZoneInstant(nowLocalDate, newZoneOffset));
    println("LocalTime 旧时区转新时区：" + DateUtil.withZoneInstant(nowLocalTime, oldZoneOffset, newZoneOffset));
    println("LocalTime 默认时区转新时区：" + DateUtil.withZoneInstant(nowLocalTime, newZoneOffset));
    println("Date 旧时区转新时区：" + DateUtil.withZoneInstant(nowDate, oldZoneOffset, newZoneOffset));
    println("Date 默认时区转新时区：" + DateUtil.withZoneInstant(nowDate, newZoneOffset));
  }

  @DisplayName("now：当前时间字符串")
  @Test
  void today() {
    println("当天时间字符串：" + DateUtil.today());
  }

  @DisplayName("now：当前时间字符串")
  @Test
  void now() {
    println("当前时间字符串：" + DateUtil.now());
    println("指定格式的当前时间字符串：" + DateUtil.now(customPattern));
    println("指定时区的当前时间字符串：" + DateUtil.now(ZoneOffset.ofHours(10)));
    println("指定时区和格式的当前时间字符串：" + DateUtil.now(ZoneOffset.ofHours(10), customPattern));
  }

  @DisplayName("minMax：指定级别的最小/最大时间")
  @Test
  void minMax() {
    println("今天开始时间：" + DateUtil.min(nowLocalDateTime, ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE, ChronoField.NANO_OF_SECOND));
    println("今天开始时间 1：" + DateUtil.min(nowLocalDateTime, ChronoField.NANO_OF_DAY));
    println("——————————————————");
    LocalDateTime localDateTime = nowLocalDateTime.withYear(2020).withMonth(2);
    println("2020 年 2 月最后一天：" + DateUtil.max(localDateTime, ChronoField.DAY_OF_MONTH));
    println("当天对应月的最后一天结束时间：" + DateUtil.max(LocalDateTime.now(), ChronoField.DAY_OF_MONTH, ChronoField.NANO_OF_DAY));
  }

  @DisplayName("todayTime：获取今天开始或结束时间")
  @Test
  void todayTime() {
    ZoneOffset zoneOffset = ZoneOffset.ofHours(-15);

    println("指定时区的昨天开始时间：" + DateUtil.todayMinTime(zoneOffset, -1L));
    println("指定时区今天开始时间：" + DateUtil.todayMinTime(zoneOffset));
    println("昨天开始时间：" + DateUtil.todayMinTime(-1L));
    println("今天开始时间：" + DateUtil.todayMinTime());

    println("指定时区和格式的昨天开始时间字符串：" + DateUtil.todayMinTimeStr(zoneOffset, DatePattern.UUUU_MM_DD_HH_MM, -1L));
    println("指定时区和格式的今天开始时间字符串：" + DateUtil.todayMinTimeStr(zoneOffset, DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("指定格式的今天开始时间字符串：" + DateUtil.todayMinTimeStr(DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("今天开始时间字符串：" + DateUtil.todayMinTimeStr());

    println("——————————————————");

    println("指定时区的昨天结束时间：" + DateUtil.todayMaxTime(zoneOffset, -1L));
    println("指定时区今天结束时间：" + DateUtil.todayMaxTime(zoneOffset));
    println("昨天结束时间：" + DateUtil.todayMaxTime(-1L));
    println("今天结束时间：" + DateUtil.todayMaxTime());

    println("指定时区和格式的昨天结束时间字符串：" + DateUtil.todayMaxTimeStr(zoneOffset, DatePattern.UUUU_MM_DD_HH_MM, -1L));
    println("指定时区和格式的今天结束时间字符串：" + DateUtil.todayMaxTimeStr(zoneOffset, DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("指定格式的今天结束时间字符串：" + DateUtil.todayMaxTimeStr(DatePattern.UUUU_MM_DD_HH_MM_SS));
    println("今天结束时间字符串：" + DateUtil.todayMaxTimeStr());
  }

  @DisplayName("plusOrMinus：加减时间")
  @Test
  void plusOrMinus() {
    println("加一天 1 小时：" + DateUtil.plusOrMinus(nowLocalDateTime, 1, ChronoUnit.DAYS, ChronoUnit.HOURS));
    println("减一天：" + DateUtil.plusOrMinus(nowLocalDateTime, -(1000 * 60 * 60 * 24)));
  }

  @DisplayName("交集差集并集")
  @Test
  void IntersectionAndDifferenceSetsAndConcatenation() {
    println("两个时间段（y1 == x2）是否交集：" + DateUtil.isIntersection(DateUtil.todayMinTime(), nowLocalDateTime, nowLocalDateTime, DateUtil.todayMaxTime()));
    println("两个时间段（y1 < x2）是否交集：" + DateUtil.isIntersection(DateUtil.todayMinTime(), nowLocalDateTime.minusNanos(1), nowLocalDateTime, DateUtil.todayMaxTime()));
    LocalDateTime[][] differenceSetsByIntersection = DateUtil.getDifferenceSetsByIntersection(DateUtil.todayMinTime(), nowLocalDateTime, nowLocalDateTime, DateUtil.todayMaxTime(), 1, ChronoUnit.SECONDS);
    println("交集的差集（y1 - 1 秒，避免有交集）：[" + differenceSetsByIntersection[0][0] + ", " + differenceSetsByIntersection[0][1] + "], [" + differenceSetsByIntersection[1][0] + ", " + differenceSetsByIntersection[1][1] + "]");
    differenceSetsByIntersection = DateUtil.getDifferenceSetsByIntersection(DateUtil.todayMinTime(), nowLocalDateTime, nowLocalDateTime, DateUtil.todayMaxTime(), 1, 1, ChronoUnit.SECONDS);
    println("交集的差集（y1 - 1 秒，避免有交集）：[" + differenceSetsByIntersection[0][0] + ", " + differenceSetsByIntersection[0][1] + "], [" + differenceSetsByIntersection[1][0] + ", " + differenceSetsByIntersection[1][1] + "]");
    differenceSetsByIntersection = DateUtil.getDifferenceSetsByIntersection(DateUtil.todayMinTime(), nowLocalDateTime, nowLocalDateTime, DateUtil.todayMaxTime(), 2, 1, ChronoUnit.SECONDS);
    println("交集的差集（x2 + 1 秒，避免有交集）：[" + differenceSetsByIntersection[0][0] + ", " + differenceSetsByIntersection[0][1] + "], [" + differenceSetsByIntersection[1][0] + ", " + differenceSetsByIntersection[1][1] + "]");
  }

  @DisplayName("是否为闰年")
  @Test
  void isLeapYear() {
    println("LocalDateTime：" + DateUtil.isLeapYear(LocalDateTime.now().withYear(2020)));
    println("LocalDate：" + DateUtil.isLeapYear(LocalDate.now().withYear(2004)));
    println("ZonedDateTime：" + DateUtil.isLeapYear(ZonedDateTime.now().withYear(2000)));
    println("Date：" + DateUtil.isLeapYear(new Date(1577808000000L)));
    println("Year：" + DateUtil.isLeapYear(1900));
  }
}
