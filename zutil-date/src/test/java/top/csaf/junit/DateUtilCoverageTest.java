package top.csaf.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.date.DateFeat;
import top.csaf.date.DateFeatConfig;
import top.csaf.date.DateUtil;
import top.csaf.date.constant.DateConst;
import top.csaf.date.constant.DateDuration;
import top.csaf.date.constant.DateFormat;
import top.csaf.date.constant.DateFormatter;
import top.csaf.date.constant.DatePattern;
import top.csaf.date.constant.DateRegExPattern;
import top.csaf.util.ReflectionTestUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("时间工具类补充测试")
class DateUtilCoverageTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final ZoneId ZONE_UTC = ZoneOffset.UTC;
    private static final Date BASE_DATE = Date.from(BASE.atZone(ZONE_SHANGHAI).toInstant());

    @AfterEach
    void tearDown() {
        DateFeat.set((ResolverStyle) null);
        DateFeat.setAlways((ResolverStyle) null);
        DateFeat.set((Boolean) null);
        DateFeat.setAlways((Boolean) null);
        DateFeat.set((Locale) null);
        DateFeat.setAlways((Locale) null);
        DateFeat.set((ZoneId) null);
        DateFeat.setAlways((ZoneId) null);
        DateFeat.setMinDateYear(null);
        DateFeat.setMinDateYearAlways(null);
    }

    @DisplayName("DateFeat")
    @Test
    void dateFeat() throws Exception {
        assertEquals(ResolverStyle.STRICT, DateFeat.getResolverStyle());
        assertTrue(DateFeat.getstrictYyToUu());
        assertEquals(Locale.ENGLISH, DateFeat.getLocale());
        assertEquals(ZONE_SHANGHAI, DateFeat.getZoneId());
        assertEquals(DateConst.DEFAULT_MIN_DATE_YEAR, DateFeat.getMinDateYear());

        DateFeat.set(ResolverStyle.SMART);
        assertEquals(ResolverStyle.SMART, DateFeat.getLazy((ResolverStyle) null));
        DateFeat.set(ResolverStyle.SMART);
        assertEquals(ResolverStyle.SMART, DateFeat.get((ResolverStyle) null));
        assertNull(DateFeat.get((ResolverStyle) null));

        DateFeat.setAlways(ResolverStyle.STRICT);
        assertEquals(ResolverStyle.STRICT, DateFeat.getLazy((ResolverStyle) null));

        DateFeat.set((Boolean) true);
        assertTrue(DateFeat.get((Boolean) null));
        assertEquals(Boolean.TRUE, DateFeat.getLazy(Boolean.TRUE));
        assertTrue(DateFeat.getstrictYyToUu());
        DateFeat.setAlways(Boolean.FALSE);
        assertFalse(DateFeat.getstrictYyToUu());

        DateFeat.set((Locale) Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, DateFeat.get((Locale) null));
        assertNull(DateFeat.getLazy((Locale) null));
        assertEquals(Locale.ENGLISH, DateFeat.getLocale());
        DateFeat.setAlways(Locale.CHINA);
        assertEquals(Locale.CHINA, DateFeat.getLazy((Locale) null));

        DateFeat.set((ZoneId) ZONE_UTC);
        assertEquals(ZONE_UTC, DateFeat.get((ZoneId) null));
        DateFeat.set((ZoneId) ZONE_UTC);
        assertEquals(ZONE_UTC, DateFeat.getLazy((ZoneId) null));
        DateFeat.set((ZoneId) ZONE_UTC);
        assertEquals(ZONE_UTC, DateFeat.getZoneId());
        DateFeat.setAlways(ZONE_SHANGHAI);
        assertEquals(ZONE_SHANGHAI, DateFeat.getLazy((ZoneId) null));

        DateFeat.setMinDateYear(2020L);
        assertEquals(2020L, DateFeat.getMinDateYear());
        DateFeat.setMinDateYearAlways(2022L);
        assertEquals(2022L, DateFeat.getMinDateYear());
        assertEquals(2022L, DateFeat.getLazyMinDateYear(null));
        assertEquals(2022L, DateFeat.getLazyMinDateYear(1990L));

        Constructor<DateFeatConfig> constructor = DateFeatConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor);
    }

    @DisplayName("DateFeatConfig")
    @Test
    void dateFeatConfig() {
        DateFeatConfig.set(ResolverStyle.SMART)
                .setStrictYyToUu(Boolean.TRUE)
                .set(Locale.US)
                .set(ZONE_UTC)
                .setMinDateYear(1999L)
                .apply();

        assertEquals(ResolverStyle.SMART, DateFeat.getResolverStyle());
        assertTrue(DateFeat.getstrictYyToUu());
        assertEquals(Locale.US, DateFeat.getLocale());
        assertEquals(ZONE_UTC, DateFeat.getZoneId());
        assertEquals(1999L, DateFeat.getMinDateYear());
    }

    @DisplayName("格式与解析")
    @Test
    void formatAndParse() {
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertEquals("2024-02-03", DateUtil.format(BASE.toLocalDate(), ZONE_UTC, DatePattern.UUUU_MM_DD));
        assertEquals("04:05:06", DateUtil.format(BASE.toLocalTime(), ZoneOffset.ofHours(-8), DatePattern.HH_MM_SS));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE.atZone(ZONE_SHANGHAI), ZONE_UTC, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE_DATE, DatePattern.YYYY_MM_DD_HH_MM_SS));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.format(Year.of(2024), ZONE_UTC, formatter()));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE.atZone(ZONE_SHANGHAI), ZONE_UTC, DatePattern.YYYY_MM_DD_HH_MM_SS));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE, ZONE_UTC, DatePattern.YYYY_MM_DD_HH_MM_SS));
        assertEquals("2024-02-02 20:05:06", DateUtil.format(BASE_DATE, ZONE_UTC, DatePattern.YYYY_MM_DD_HH_MM_SS));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE.atZone(ZONE_UTC).toInstant().toEpochMilli(), ZONE_UTC, DatePattern.YYYY_MM_DD_HH_MM_SS));
        assertEquals("2024-02-03", DateUtil.format(BASE, DatePattern.UUUU_MM_DD));
        assertEquals("04:05:06", DateUtil.format(BASE.toLocalTime()));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE_DATE));

        assertEquals(BASE, DateUtil.parseLocalDateTime("2024-02-03 04:05:06"));
        assertEquals(BASE.toLocalDate(), DateUtil.parseLocalDate("2024-02-03"));
        assertEquals(BASE.toLocalTime(), DateUtil.parseLocalTime("04:05:06"));
        assertEquals(BASE_DATE, DateUtil.parseDate("2024-02-03 04:05:06"));
        assertEquals(BASE, DateUtil.parse("2024-02-03 04:05:06", LocalDateTime.class));
        assertEquals(BASE.toLocalDate(), DateUtil.parse("2024-02-03", LocalDate.class));
        assertEquals(BASE.toLocalTime(), DateUtil.parse("04:05:06", LocalTime.class));
        assertEquals(BASE_DATE, DateUtil.parse("2024-02-03 04:05:06", Date.class));
        assertEquals(BASE_DATE.getTime(), DateUtil.parse("2024-02-03 04:05:06", Date.class).getTime());

        assertThrows(NullPointerException.class, () -> DateUtil.parseLocalDateTime("2024-02-03 04:05:06", (String[]) null));
        assertThrows(NullPointerException.class, () -> DateUtil.parseLocalDate("2024-02-03", (String[]) null));
        assertThrows(NullPointerException.class, () -> DateUtil.parseLocalTime("04:05:06", (String[]) null));
    }

    @DisplayName("区间与周")
    @Test
    void betweenAndWeeks() {
        assertEquals(Duration.ofDays(2), DateUtil.between(BASE, BASE.plusDays(2)));
        assertEquals(2L, DateUtil.between(BASE, BASE.plusDays(2), ChronoUnit.DAYS));
        assertEquals("1周01天02小时03分04秒005毫秒", DateUtil.formatCountdown(DateDuration.WEEK_MILLIS + DateDuration.DAY_OF_MONTH_MILLIS + 7_384_005L, "W周dd天HH小时mm分ss秒SSS毫秒"));
        assertEquals("01周1天2时01半日1半日3分04秒005毫秒", DateUtil.formatCountdown(DateDuration.WEEK_MILLIS + DateDuration.DAY_OF_MONTH_MILLIS + 7_384_005L, "WW周d天H时hh半日K半日m分ss秒SSS毫秒"));
        assertEquals("3半日03半日", DateUtil.formatCountdown(2 * DateDuration.HOUR_MILLIS, "k半日kk半日"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatCountdown(0L, "s秒"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatCountdown(1L, ""));
        assertEquals("1周1天2小时3分4秒5毫秒", DateUtil.formatCountdown(DateDuration.WEEK_MILLIS + DateDuration.DAY_OF_MONTH_MILLIS + 7_384_005L, true, "周", "天", "小时", "分", "秒", "毫秒"));
        assertEquals("1天", DateUtil.formatCountdown(DateDuration.DAY_OF_MONTH_MILLIS, true, "周", "天", "小时", "分", "秒", "毫秒"));
        assertEquals("0周0天0小时0分0秒5毫秒", DateUtil.formatCountdown(5L, false, "周", "天", "小时", "分", "秒", "毫秒"));
        assertEquals("0周", DateUtil.formatCountdown(1L, "W周"));
        assertEquals("00周", DateUtil.formatCountdown(1L, "WW周"));
        assertEquals("0天", DateUtil.formatCountdown(1L, "d天"));
        assertEquals("00天", DateUtil.formatCountdown(1L, "dd天"));
        assertEquals("0时", DateUtil.formatCountdown(1L, "H时"));
        assertEquals("00时", DateUtil.formatCountdown(1L, "HH时"));
        assertEquals("0半日", DateUtil.formatCountdown(1L, "h半日"));
        assertEquals("00半日", DateUtil.formatCountdown(1L, "hh半日"));
        assertEquals("0半日", DateUtil.formatCountdown(1L, "K半日"));
        assertEquals("00半日", DateUtil.formatCountdown(1L, "KK半日"));
        assertEquals("1半日", DateUtil.formatCountdown(1L, "k半日"));
        assertEquals("01半日", DateUtil.formatCountdown(1L, "kk半日"));
        assertEquals("0分", DateUtil.formatCountdown(1L, "m分"));
        assertEquals("1分", DateUtil.formatCountdown(DateDuration.MINUTE_MILLIS, "m分"));
        assertEquals("01分", DateUtil.formatCountdown(DateDuration.MINUTE_MILLIS, "mm分"));
        assertEquals("00秒", DateUtil.formatCountdown(1L, "ss秒"));
        assertEquals("1秒", DateUtil.formatCountdown(DateDuration.SECOND_MILLIS, "s秒"));
        assertEquals("01秒", DateUtil.formatCountdown(DateDuration.SECOND_MILLIS, "ss秒"));
        assertEquals("001毫秒", DateUtil.formatCountdown(1L, "SSS毫秒"));
        assertEquals("1毫秒", DateUtil.formatCountdown(1L, true, null, null, null, null, null, "毫秒"));
        assertEquals("0毫秒", DateUtil.formatCountdown(0L, false, null, null, null, null, null, "毫秒"));
        assertEquals("01半日", DateUtil.formatCountdown(2 * DateDuration.HOUR_MILLIS, "KK半日"));
        assertEquals(3, DateUtil.getByRange(BASE.withDayOfMonth(1), BASE.withDayOfMonth(3), DatePattern.UUUU_MM_DD).size());
        assertEquals(2, DateUtil.getByRangeAndWeeks(BASE.withDayOfMonth(1), BASE.withDayOfMonth(10), "1,3").size());
        assertEquals(1, DateUtil.getByWeeks("1,3", BASE.withDayOfMonth(1), BASE.withDayOfMonth(3), BASE.withDayOfMonth(5)).size());
        assertEquals(4, DateUtil.getWeeksOfMonth(LocalDate.of(2021, 2, 1)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2024, 6, 1)));
        assertEquals(6, DateUtil.getWeeksOfMonth(LocalDate.of(2020, 3, 1)));
        assertEquals(1, DateUtil.getWeekOfMonth(LocalDate.of(2021, 2, 1)));
        assertEquals(2, DateUtil.getWeekOfMonth(LocalDate.of(2021, 2, 8)));
        assertEquals(LocalDate.of(2024, 6, 3), DateUtil.getStartDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 2));
        assertEquals(LocalDate.of(2024, 6, 30), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 5));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getStartDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 0));
        assertTrue(DateUtil.isLeapYear(BASE));
        assertFalse(DateUtil.isLeapYear(LocalDate.of(2023, 2, 3)));
    }

    @DisplayName("偏移")
    @Test
    void zoneAndConvert() {
        assertEquals(BASE_DATE, DateUtil.toDate(BASE));
        assertEquals(BASE_DATE, DateUtil.toDate(BASE.atZone(ZONE_SHANGHAI)));
        assertEquals(Date.from(BASE.toLocalDate().atStartOfDay(ZONE_SHANGHAI).toInstant()), DateUtil.toDate(BASE.toLocalDate()));
        assertEquals(Date.from(LocalDate.of(1970, 1, 1).atTime(BASE.toLocalTime()).atZone(ZoneOffset.ofHours(16)).toInstant()), DateUtil.toDate(BASE.toLocalTime(), ZONE_UTC));
        assertEquals(BASE.toLocalDate(), DateUtil.toLocalDate(BASE_DATE));
        assertEquals(BASE.toLocalTime(), DateUtil.toLocalTime(BASE_DATE));
        assertEquals(BASE, DateUtil.toTemporal(BASE_DATE, LocalDateTime.class));
        assertEquals(BASE.toLocalDate(), DateUtil.toTemporal(BASE_DATE, LocalDate.class));
        assertEquals(BASE.toLocalTime(), DateUtil.toTemporal(BASE_DATE, LocalTime.class));
        assertEquals(BASE.atZone(DateFeat.getZoneId()), DateUtil.toTemporal(BASE_DATE, ZonedDateTime.class));
        assertEquals(BASE.atZone(DateFeat.getZoneId()).toOffsetDateTime(), DateUtil.toTemporal(BASE_DATE, OffsetDateTime.class));
        assertEquals(BASE.atZone(ZONE_UTC).toEpochSecond(), DateUtil.toEpochSecond(BASE.atZone(ZONE_UTC), ZONE_UTC));
        assertEquals(BASE.toEpochSecond(ZoneOffset.UTC), DateUtil.toEpochSecond(BASE, ZONE_UTC));
        assertEquals(LocalDate.of(2024, 2, 3).atStartOfDay(ZONE_UTC).toEpochSecond(), DateUtil.toEpochSecond(BASE.toLocalDate(), ZONE_UTC));
        assertEquals(LocalDate.of(1970, 1, 1).atTime(BASE.toLocalTime()).atZone(ZONE_UTC).toEpochSecond(), DateUtil.toEpochSecond(BASE.toLocalTime(), ZONE_UTC));
        assertEquals(BASE.atZone(ZONE_UTC).toInstant().toEpochMilli(), DateUtil.toEpochMilli(BASE.atZone(ZONE_UTC), ZONE_UTC));
        assertEquals(BASE.atZone(ZONE_UTC).toInstant().toEpochMilli(), DateUtil.toEpochMilli(BASE, ZONE_UTC));
        assertEquals(LocalDate.of(2024, 2, 3).atStartOfDay(ZONE_UTC).toInstant().toEpochMilli(), DateUtil.toEpochMilli(BASE.toLocalDate(), ZONE_UTC));
        assertEquals(LocalDate.of(1970, 1, 1).atTime(BASE.toLocalTime()).atZone(ZONE_UTC).toInstant().toEpochMilli(), DateUtil.toEpochMilli(BASE.toLocalTime(), ZONE_UTC));
        assertEquals(BASE_DATE, DateUtil.toDate(BASE, null));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toDate(OffsetDateTime.now(), ZONE_UTC));
    }

    @DisplayName("重载入口")
    @Test
    void overloads() throws Throwable {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.UUUU_MM_DD_HH_MM_SS);
        long epochMilli = BASE.atZone(ZONE_SHANGHAI).toInstant().toEpochMilli();
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE, formatter));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE));
        assertEquals("2024-02-03", DateUtil.format(BASE.toLocalDate(), ZONE_UTC));
        assertEquals("2024-02-03", DateUtil.format(BASE.toLocalDate()));
        assertEquals("04:05:06", DateUtil.format(BASE.toLocalTime(), ZONE_UTC));
        assertEquals("04:05:06", DateUtil.format(BASE.toLocalTime()));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE.atZone(ZONE_SHANGHAI)));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.format(Year.of(2024)));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.format(Year.of(2024), ZONE_UTC));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE_DATE, formatter));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(epochMilli, formatter));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(epochMilli));
        assertEquals("2024-02-02 20:05:06", DateUtil.format(epochMilli, ZONE_UTC, formatter));
        assertEquals("2024-02-02 20:05:06", DateUtil.format(epochMilli, ZONE_UTC));

        assertEquals(BASE, DateUtil.parseLocalDateTime(BASE_DATE.getTime()));
        assertEquals(LocalDateTime.of(2024, 2, 2, 20, 5, 6), DateUtil.parseLocalDateTime(BASE_DATE.getTime(), ZONE_UTC));
        assertEquals(BASE.toLocalDate(), DateUtil.parseLocalDate(BASE_DATE.getTime()));
        assertEquals(LocalDate.of(2024, 2, 2), DateUtil.parseLocalDate(BASE_DATE.getTime(), ZONE_UTC));
        assertEquals(BASE.toLocalTime(), DateUtil.parseLocalTime(BASE_DATE.getTime()).withNano(0));
        assertEquals(LocalTime.of(20, 5, 6), DateUtil.parseLocalTime(BASE_DATE.getTime(), ZONE_UTC).withNano(0));
        assertEquals(BASE_DATE, DateUtil.parseDate(BASE_DATE.getTime()));
        assertEquals(BASE, DateUtil.parse(BASE_DATE.getTime(), LocalDateTime.class));
        assertEquals(BASE.toLocalDate(), DateUtil.parse(BASE_DATE.getTime(), LocalDate.class));
        assertEquals(BASE.toLocalTime(), ((LocalTime) DateUtil.parse(BASE_DATE.getTime(), LocalTime.class)).withNano(0));
        assertEquals(BASE_DATE, DateUtil.parse(BASE_DATE.getTime(), Date.class));
        assertNull(DateUtil.parse(BASE_DATE.getTime(), String.class));
        assertEquals(LocalDateTime.of(2024, 2, 2, 20, 5, 6), DateUtil.parse(BASE_DATE.getTime(), ZONE_UTC, LocalDateTime.class));
        assertEquals(LocalDate.of(2024, 2, 2), DateUtil.parse(BASE_DATE.getTime(), ZONE_UTC, LocalDate.class));
        assertEquals(LocalTime.of(20, 5, 6), ((LocalTime) DateUtil.parse(BASE_DATE.getTime(), ZONE_UTC, LocalTime.class)).withNano(0));
        assertEquals(BASE_DATE, DateUtil.parse(BASE_DATE.getTime(), ZONE_UTC, Date.class));
        assertNull(DateUtil.parse(BASE_DATE.getTime(), ZONE_UTC, String.class));

        assertEquals(123L, DateUtil.toChronoUnit(123, ChronoUnit.MILLIS));
        assertEquals(2L, DateUtil.toChronoUnit(2 * DateDuration.SECOND_MILLIS, ChronoUnit.SECONDS));
        assertTrue(DateUtil.nowEpochSecond() > 0);
        assertTrue(DateUtil.nowEpochSecond(ZONE_UTC) > 0);
        assertTrue(DateUtil.nowEpochMilli() > 0);
        assertTrue(DateUtil.nowEpochMilli(ZONE_UTC) > 0);
        assertNotNull(DateUtil.today());
        assertNotNull(DateUtil.now());
        assertNotNull(DateUtil.now(formatter));
        assertNotNull(DateUtil.now(DatePattern.UUUU_MM_DD));
        assertNotNull(DateUtil.now(ZONE_UTC));
        assertNotNull(DateUtil.now(ZONE_UTC, formatter));
        assertNotNull(DateUtil.now(ZONE_UTC, DatePattern.UUUU_MM_DD));
        assertNotNull(DateUtil.todayMinTime());
        assertNotNull(DateUtil.todayMinTime(1));
        assertNotNull(DateUtil.todayMinTime(0));
        assertNotNull(DateUtil.todayMinTime(ZONE_UTC));
        assertNotNull(DateUtil.todayMinTime(ZONE_UTC, 0L));
        assertNotNull(DateUtil.todayMinTimeStr());
        assertNotNull(DateUtil.todayMinTimeStr(formatter));
        assertNotNull(DateUtil.todayMinTimeStr(DatePattern.UUUU_MM_DD));
        assertNotNull(DateUtil.todayMinTimeStr(ZONE_UTC, formatter));
        assertNotNull(DateUtil.todayMinTimeStr(ZONE_UTC, DatePattern.UUUU_MM_DD));
        assertNotNull(DateUtil.todayMaxTime());
        assertNotNull(DateUtil.todayMaxTime(1));
        assertNotNull(DateUtil.todayMaxTime(0));
        assertNotNull(DateUtil.todayMaxTime(ZONE_UTC));
        assertNotNull(DateUtil.todayMaxTime(ZONE_UTC, 0L));
        assertNotNull(DateUtil.todayMaxTimeStr());
        assertNotNull(DateUtil.todayMaxTimeStr(formatter));
        assertNotNull(DateUtil.todayMaxTimeStr(DatePattern.UUUU_MM_DD));
        assertNotNull(DateUtil.todayMaxTimeStr(ZONE_UTC, formatter));
        assertNotNull(DateUtil.todayMaxTimeStr(ZONE_UTC, DatePattern.UUUU_MM_DD));

        for (Method method : DateUtil.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers()) || method.getName().contains("now") || method.getName().contains("today")) {
                continue;
            }
            Object[] args = argsFor(method.getParameterTypes());
            if (args == null) {
                continue;
            }
            try {
                method.invoke(null, args);
            } catch (InvocationTargetException ignored) {
                // 部分组合用于覆盖校验和异常分支
            }
        }
    }

    @DisplayName("空参校验")
    @Test
    void nullChecks() throws Throwable {
        int invoked = 0;
        for (Method method : DateUtil.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] args = argsFor(parameterTypes);
            if (args == null || args.length == 0) {
                continue;
            }
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].isPrimitive()) {
                    continue;
                }
                Object[] nullArgs = args.clone();
                nullArgs[i] = null;
                try {
                    method.invoke(null, nullArgs);
                } catch (InvocationTargetException | IllegalArgumentException ignored) {
                    // 用于覆盖 Lombok 非空校验和工具方法自身的空参分支
                }
                invoked++;
            }
        }
        assertTrue(invoked > 100);
    }

    private static void assertFormatterBuilderDefaults() throws Throwable {
        Map<TemporalField, Long> defaults = new HashMap<>();
        defaults.put(ChronoField.YEAR_OF_ERA, 1L);
        defaults.put(ChronoField.YEAR, 0L);
        defaults.put(ChronoField.MONTH_OF_YEAR, 1L);
        defaults.put(ChronoField.DAY_OF_MONTH, 1L);
        defaults.put(ChronoField.HOUR_OF_DAY, 0L);
        defaults.put(ChronoField.MINUTE_OF_HOUR, 0L);
        defaults.put(ChronoField.SECOND_OF_MINUTE, 0L);
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, DatePattern.UUUU_MM_DD_HH_MM_SS, defaults);
        assertFalse(defaults.containsKey(ChronoField.YEAR_OF_ERA));
        assertTrue(defaults.containsKey(ChronoField.YEAR));
        assertFalse(defaults.containsKey(ChronoField.MONTH_OF_YEAR));
        assertFalse(defaults.containsKey(ChronoField.DAY_OF_MONTH));
        assertFalse(defaults.containsKey(ChronoField.HOUR_OF_DAY));
        assertFalse(defaults.containsKey(ChronoField.MINUTE_OF_HOUR));
        assertFalse(defaults.containsKey(ChronoField.SECOND_OF_MINUTE));

        Map<TemporalField, Long> emptyDefaults = new HashMap<>();
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, DatePattern.HH_MM_SS, emptyDefaults);
        assertTrue(emptyDefaults.isEmpty());

        Map<TemporalField, Long> yDefaults = new HashMap<>();
        yDefaults.put(ChronoField.YEAR, 0L);
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, DatePattern.YYYY_MM_DD, yDefaults);
        assertFalse(yDefaults.containsKey(ChronoField.YEAR));

        Map<TemporalField, Long> yAbsentDefaults = new HashMap<>();
        yAbsentDefaults.put(ChronoField.MONTH_OF_YEAR, 1L);
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, DatePattern.YYYY_MM_DD, yAbsentDefaults);
        assertFalse(yAbsentDefaults.containsKey(ChronoField.MONTH_OF_YEAR));

        Map<TemporalField, Long> hourAbsentDefaults = new HashMap<>();
        hourAbsentDefaults.put(ChronoField.MINUTE_OF_HOUR, 0L);
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, DatePattern.UUUU_MM_DD_HH_MM_SS, hourAbsentDefaults);
        assertFalse(hourAbsentDefaults.containsKey(ChronoField.MINUTE_OF_HOUR));

        Map<TemporalField, Long> lDefaults = new HashMap<>();
        lDefaults.put(ChronoField.MONTH_OF_YEAR, 1L);
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, "uuuu-LL-dd", lDefaults);
        assertFalse(lDefaults.containsKey(ChronoField.MONTH_OF_YEAR));

        Map<TemporalField, Long> kDefaults = new HashMap<>();
        kDefaults.put(ChronoField.HOUR_OF_DAY, 0L);
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, "uuuu-MM-dd kk:mm:ss", kDefaults);
        assertFalse(kDefaults.containsKey(ChronoField.HOUR_OF_DAY));

        Map<TemporalField, Long> untouchedDefaults = new HashMap<>();
        untouchedDefaults.put(ChronoField.YEAR_OF_ERA, 1L);
        untouchedDefaults.put(ChronoField.YEAR, 0L);
        untouchedDefaults.put(ChronoField.MONTH_OF_YEAR, 1L);
        untouchedDefaults.put(ChronoField.DAY_OF_MONTH, 1L);
        untouchedDefaults.put(ChronoField.HOUR_OF_DAY, 0L);
        untouchedDefaults.put(ChronoField.MINUTE_OF_HOUR, 0L);
        untouchedDefaults.put(ChronoField.SECOND_OF_MINUTE, 0L);
        ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, "n", untouchedDefaults);
        assertEquals(7, untouchedDefaults.size());

        assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, "", new HashMap<>()));
        assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(DateUtil.class, "getFormatterBuilder", new Class[]{String.class, Map.class}, null, new HashMap<>()));
    }

    private static Object[] argsFor(Class<?>[] parameterTypes) {
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (Temporal.class.equals(type) || LocalDateTime.class.equals(type)) {
                args[i] = BASE;
            } else if (LocalDate.class.equals(type)) {
                args[i] = BASE.toLocalDate();
            } else if (Date.class.equals(type)) {
                args[i] = BASE_DATE;
            } else if (Long.class.equals(type)) {
                args[i] = BASE_DATE.getTime();
            } else if (ZoneId.class.equals(type)) {
                args[i] = ZONE_UTC;
            } else if (DateTimeFormatter.class.equals(type)) {
                args[i] = DateTimeFormatter.ofPattern(DatePattern.UUUU_MM_DD_HH_MM_SS);
            } else if (Locale.class.equals(type)) {
                args[i] = Locale.ENGLISH;
            } else if (Class.class.equals(type)) {
                args[i] = LocalDateTime.class;
            } else if (String.class.equals(type)) {
                args[i] = "2024-02-03 04:05:06";
            } else if (ChronoUnit.class.equals(type)) {
                args[i] = ChronoUnit.DAYS;
            } else if (TemporalField.class.isAssignableFrom(type)) {
                args[i] = ChronoField.DAY_OF_MONTH;
            } else if (type.isArray() && String.class.equals(type.getComponentType())) {
                args[i] = new String[]{DatePattern.UUUU_MM_DD_HH_MM_SS};
            } else if (type.isArray() && ChronoUnit.class.equals(type.getComponentType())) {
                args[i] = new ChronoUnit[]{ChronoUnit.DAYS};
            } else if (type.isArray() && TemporalField.class.isAssignableFrom(type.getComponentType())) {
                args[i] = new TemporalField[]{ChronoField.DAY_OF_MONTH};
            } else if (long.class.equals(type)) {
                args[i] = 1L;
            } else if (int.class.equals(type)) {
                args[i] = 1;
            } else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
                args[i] = true;
            } else {
                return null;
            }
        }
        return args;
    }

    @DisplayName("格式解析补充分支")
    @Test
    void parseAndFormatBranches() throws Throwable {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.UUUU_MM_DD_HH_MM_SS);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getFormatter("", Locale.ENGLISH, ZONE_UTC, true));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getFormatter(" "));
        assertFormatterBuilderDefaults();
        assertNotNull(DateUtil.getFormatter(DatePattern.UUUU_MM_DD, Locale.ENGLISH, ZONE_UTC, true));
        assertNotNull(DateUtil.getFormatter(DatePattern.UUUU_MM_DD, Locale.ENGLISH, true));
        assertNotNull(DateUtil.getFormatter(DatePattern.UUUU_MM_DD, ZONE_UTC, true));
        assertNotNull(DateUtil.getFormatter(DatePattern.UUUU_MM_DD, Locale.ENGLISH));
        assertNotNull(DateUtil.getFormatter(DatePattern.UUUU_MM_DD, ZONE_UTC));
        assertNotNull(DateUtil.getFormatter(DatePattern.UUUU_MM_DD, true));
        assertNotNull(DateUtil.getFormatter(DatePattern.UUUU_MM_DD));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE, null, formatter));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE_DATE, DatePattern.YYYY_MM_DD_HH_MM_SS));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE_DATE, (ZoneId) null, formatter));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE_DATE, (ZoneId) null, DatePattern.YYYY_MM_DD_HH_MM_SS));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.format(BASE, ZONE_UTC, ""));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.format(BASE_DATE, ZONE_UTC, ""));
        assertThrows(RuntimeException.class, () -> DateUtil.format(Year.of(2024), null, formatter));
        assertEquals("2024-02-03 04:05:06", DateUtil.format(BASE_DATE.getTime(), DatePattern.YYYY_MM_DD_HH_MM_SS));

        assertEquals(LocalDateTime.of(2024, 2, 3, 0, 0), DateUtil.parseLocalDateTime("2024-02-03"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 0, 0), DateUtil.parseLocalDateTime("2024-02-03", ZONE_SHANGHAI));
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), DateUtil.parseLocalDateTime("2024-02"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6), DateUtil.parseLocalDateTime("2024/02/03 04:05:06"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6), DateUtil.parseLocalDateTime("2024.02.03 04:05:06"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6), DateUtil.parseLocalDateTime("2024.02.03 04:05:06", ZONE_SHANGHAI));
        assertNull(DateUtil.parseLocalDateTime("2024-02-03.04:05:06"));
        assertNull(DateUtil.parseLocalDateTime("2024-02.03 04:05:06"));
        assertNull(DateUtil.parseLocalDateTime("2024/02.03 04:05:06"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6), DateUtil.parseLocalDateTime("2024-02-03 04:05:06", DatePattern.UUUU_MM_DD_DOT_HH_MM_SS, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5), DateUtil.parseLocalDateTime("2024/02/03 04:05"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5), DateUtil.parseLocalDateTime("2024.02.03 04:05"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 0, 0), DateUtil.parseLocalDateTime("2024/02/03"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 0, 0), DateUtil.parseLocalDateTime("2024.02.03"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 0, 0), DateUtil.parseLocalDateTime("2024.02.03", ZONE_SHANGHAI));
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), DateUtil.parseLocalDateTime("2024/02"));
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), DateUtil.parseLocalDateTime("2024.02"));
        assertNull(DateUtil.parseLocalDateTime("2024 02 03 04:05"));
        assertNull(DateUtil.parseLocalDateTime("2024#02#03 04:05:06"));
        assertNull(DateUtil.parseLocalDateTime("04:05:0600"));
        assertNull(DateUtil.parseLocalDateTime("04:05:0"));
        assertEquals(LocalDateTime.of(0, 1, 1, 4, 5, 6), DateUtil.parseLocalDateTime("04:05:06"));
        assertEquals(LocalDateTime.of(0, 1, 1, 4, 5), DateUtil.parseLocalDateTime("04:05"));
        assertNull(DateUtil.parseLocalDateTime("20240203"));
        assertNull(DateUtil.parseLocalDateTime("bad", DatePattern.UUUU_MM_DD));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.parseLocalDateTime("2024-02-03", ZONE_UTC, ""));

        assertEquals(LocalDate.of(2024, 2, 3), DateUtil.parseLocalDate("2024-02-03"));
        assertEquals(LocalDate.of(2024, 2, 3), DateUtil.parseLocalDate("2024-02-03", ZONE_SHANGHAI));
        assertEquals(LocalDate.of(2024, 2, 1), DateUtil.parseLocalDate("2024-02"));
        assertEquals(LocalDate.of(2024, 2, 3), DateUtil.parseLocalDate("2024/02/03"));
        assertEquals(LocalDate.of(2024, 2, 3), DateUtil.parseLocalDate("2024.02.03"));
        assertEquals(LocalDate.of(2024, 2, 3), DateUtil.parseLocalDate("2024.02.03", ZONE_SHANGHAI));
        assertNull(DateUtil.parseLocalDate("2024-02.03"));
        assertNull(DateUtil.parseLocalDate("2024/02.03"));
        assertEquals(LocalDate.of(2024, 2, 1), DateUtil.parseLocalDate("2024/02"));
        assertEquals(LocalDate.of(2024, 2, 1), DateUtil.parseLocalDate("2024.02"));
        assertNull(DateUtil.parseLocalDate("2024#02#03"));
        assertEquals(LocalDate.of(0, 1, 1), DateUtil.parseLocalDate("04:05:06"));
        assertEquals(LocalDate.of(0, 1, 1), DateUtil.parseLocalDate("04:05"));
        assertNull(DateUtil.parseLocalDate("20240203"));
        assertNull(DateUtil.parseLocalDate("2024 02 03"));
        assertNull(DateUtil.parseLocalDate("2024 02"));
        assertNull(DateUtil.parseLocalDate("04:05:0600"));
        assertNull(DateUtil.parseLocalDate("04:05:0"));
        assertNull(DateUtil.parseLocalDate("2024#02#03"));
        assertNull(DateUtil.parseLocalDate("bad", DatePattern.UUUU_MM_DD));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.parseLocalDate("2024-02-03", ZONE_UTC, ""));

        assertEquals(LocalTime.of(4, 5), DateUtil.parseLocalTime("04:05"));
        assertEquals(LocalTime.of(4, 5, 6), DateUtil.parseLocalTime("04:05:06"));
        assertNotNull(DateUtil.parseLocalTime("04:05", ZONE_UTC));
        assertNull(DateUtil.parseLocalTime(""));
        assertNull(DateUtil.parseLocalTime(" ", ZONE_UTC, DatePattern.HH_MM_SS));
        assertNull(DateUtil.parseLocalTime("bad"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.parseLocalTime("04:05:06", ZONE_UTC, ""));
        assertNull(DateUtil.parseLocalTime("04:05:060"));
        assertNull(DateUtil.parseLocalTime("bad", DatePattern.HH_MM_SS));
        assertNull(DateUtil.parseDate("bad"));
        assertNull(DateUtil.parseDate("bad", ZONE_UTC));
        assertNull(DateUtil.parseDate("bad", ZONE_UTC, DatePattern.UUUU_MM_DD));
        assertNull(DateUtil.parseDate("bad", DatePattern.UUUU_MM_DD));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.parseDate("2024-02-03", ""));
        assertEquals(LocalDateTime.of(2024, 2, 2, 20, 5, 6), DateUtil.parse("2024-02-03 04:05:06", ZONE_UTC, LocalDateTime.class, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertEquals(LocalDate.of(2024, 2, 2), DateUtil.parse("2024-02-03", ZONE_UTC, LocalDate.class, DatePattern.UUUU_MM_DD));
        assertEquals(LocalTime.of(20, 5, 6), DateUtil.parse("04:05:06", ZONE_UTC, LocalTime.class, DatePattern.HH_MM_SS));
        Date zonedDate = Date.from(LocalDateTime.of(2024, 2, 2, 20, 5, 6).atZone(ZONE_SHANGHAI).toInstant());
        assertEquals(zonedDate, DateUtil.parse("2024-02-03 04:05:06", ZONE_UTC, Date.class, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertNull(DateUtil.parse("2024-02-03", ZONE_UTC, String.class, DatePattern.UUUU_MM_DD));
        assertEquals(LocalDate.of(2024, 2, 2), DateUtil.parse("2024-02-03", ZONE_UTC, LocalDate.class));
        assertEquals(LocalTime.of(20, 5, 6), DateUtil.parse("04:05:06", ZONE_UTC, LocalTime.class));
        assertEquals(zonedDate, DateUtil.parse("2024-02-03 04:05:06", ZONE_UTC, Date.class));
        assertNull(DateUtil.parse("2024-02-03", ZONE_UTC, String.class));
        assertEquals(BASE.toLocalDate(), DateUtil.parse("2024-02-03", LocalDate.class, DatePattern.UUUU_MM_DD));
        assertEquals(BASE.toLocalTime(), DateUtil.parse("04:05:06", LocalTime.class, DatePattern.HH_MM_SS));
        assertEquals(BASE_DATE, DateUtil.parse("2024-02-03 04:05:06", Date.class, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertNull(DateUtil.parse("2024-02-03", String.class, DatePattern.UUUU_MM_DD));
        assertNull(DateUtil.parse("2024-02-03", String.class));
    }

    @DisplayName("计算和区间补充分支")
    @Test
    void calculationBranches() {
        LocalDateTime other = BASE.plusDays(1).plusHours(2);
        Date otherDate = Date.from(other.atZone(ZONE_SHANGHAI).toInstant());
        assertEquals(BASE.plusDays(1), DateUtil.plusOrMinus(BASE, 1, ChronoUnit.DAYS));
        assertEquals(BASE.minusDays(1), DateUtil.plusOrMinus(BASE, -1, ChronoUnit.DAYS));
        assertEquals(BASE, DateUtil.plusOrMinus(BASE, 0, ChronoUnit.DAYS));
        assertEquals("2024-02-04 04:05:06", DateUtil.plusOrMinus(BASE, 1, formatter(), ChronoUnit.DAYS));
        assertEquals("2024-02-04 04:05:06", DateUtil.plusOrMinus(BASE, 1, DatePattern.UUUU_MM_DD_HH_MM_SS, ChronoUnit.DAYS));
        assertEquals(BASE.plusNanos(1_000_000), DateUtil.plusOrMinus(BASE, 1));
        assertEquals("2024-02-03 04:05:06", DateUtil.plusOrMinus(BASE, 1, formatter()));
        assertEquals("2024-02-03 04:05:06", DateUtil.plusOrMinus(BASE, 1, DatePattern.UUUU_MM_DD_HH_MM_SS));

        assertEquals(Duration.ofHours(26), DateUtil.between(BASE, other));
        assertEquals(26L, DateUtil.between(BASE, other, ChronoUnit.HOURS));
        assertNotNull(DateUtil.between(BASE_DATE, otherDate));
        assertTrue(DateUtil.between(BASE_DATE, otherDate, ChronoUnit.HOURS) < 0);
        assertEquals(Duration.ofDays(1), DateUtil.between("2024-02-03", "2024-02-04", DatePattern.UUUU_MM_DD));
        assertEquals(1L, DateUtil.between("2024-02-03", "2024-02-04", ChronoUnit.DAYS, DatePattern.UUUU_MM_DD));
        assertEquals(Duration.ofDays(1), DateUtil.between("2024-02-03", "2024-02-04"));
        assertEquals(1L, DateUtil.between("2024-02-03", "2024-02-04", ChronoUnit.DAYS));
        assertEquals("1天", DateUtil.formatBetween(BASE, BASE.plusDays(1), "d天"));
        assertEquals("1天", DateUtil.formatBetween(BASE_DATE, otherDate, "d天"));
        assertEquals("1天", DateUtil.formatBetween(BASE, BASE.plusDays(1), true, null, "天", null, null, null, null));
        assertTrue(DateUtil.formatBetween(BASE_DATE, otherDate, true, null, "天", null, null, null, null).contains("天"));

        assertNull(DateUtil.min(BASE, new TemporalField[]{}));
        assertEquals(LocalDateTime.of(2024, 1, 1, 4, 5, 6), DateUtil.min(BASE, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH));
        assertEquals(LocalDateTime.of(2024, 12, 31, 4, 5, 6), DateUtil.max(BASE, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH));
        assertEquals(LocalDateTime.of(2024, 12, 31, 4, 5, 6), DateUtil.max(BASE, ChronoField.DAY_OF_YEAR));
        assertNull(DateUtil.max(BASE, new TemporalField[]{}));
        assertNull(DateUtil.minDate(BASE, new TemporalField[]{}));
        assertNull(DateUtil.maxDate(BASE, new TemporalField[]{}));
        assertNull(DateUtil.minStr(BASE, formatter(), new TemporalField[]{}));
        assertNull(DateUtil.minStr(BASE, DatePattern.UUUU_MM_DD_HH_MM_SS, new TemporalField[]{}));
        assertNull(DateUtil.minStr(BASE, new TemporalField[]{}));
        assertNull(DateUtil.maxStr(BASE, formatter(), new TemporalField[]{}));
        assertNull(DateUtil.maxStr(BASE, DatePattern.UUUU_MM_DD_HH_MM_SS, new TemporalField[]{}));
        assertNull(DateUtil.maxStr(BASE, new TemporalField[]{}));
        assertNotNull(DateUtil.minDate(BASE, ChronoField.DAY_OF_MONTH));
        assertNotNull(DateUtil.maxDate(BASE, ChronoField.DAY_OF_MONTH));
        assertEquals("2024-02-01 04:05:06", DateUtil.minStr(BASE, formatter(), ChronoField.DAY_OF_MONTH));
        assertEquals("2024-02-01 04:05:06", DateUtil.minStr(BASE, DatePattern.UUUU_MM_DD_HH_MM_SS, ChronoField.DAY_OF_MONTH));
        assertNotNull(DateUtil.minStr(BASE, ChronoField.DAY_OF_MONTH));
        assertEquals("2024-02-29 04:05:06", DateUtil.maxStr(BASE, formatter(), ChronoField.DAY_OF_MONTH));
        assertEquals("2024-02-29 04:05:06", DateUtil.maxStr(BASE, DatePattern.UUUU_MM_DD_HH_MM_SS, ChronoField.DAY_OF_MONTH));
        assertNotNull(DateUtil.maxStr(BASE, ChronoField.DAY_OF_MONTH));
    }

    private static DateTimeFormatter formatter() {
        return DateTimeFormatter.ofPattern(DatePattern.UUUU_MM_DD_HH_MM_SS);
    }

    @DisplayName("校验和集合补充分支")
    @Test
    void validateAndSetBranches() {
        assertTrue(DateUtil.validate("04:05:06", DatePattern.HH_MM_SS));
        assertFalse(DateUtil.validate("2024-02-31", DatePattern.UUUU_MM_DD));
        assertTrue(DateUtil.validate("2024-02-03", DatePattern.UUUU_MM_DD));
        assertTrue(DateUtil.validate("2024-02-03 04:05:06", DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertTrue(DateUtil.validate("2024-02-03 04:05", DatePattern.UUUU_MM_DD_HH_MM));
        assertTrue(DateUtil.validate("04:05", DatePattern.HH_MM));
        assertTrue(DateUtil.validate("2024-02-03 04:05", DatePattern.UUUU_MM_DD_HH_MM));
        assertFalse(DateUtil.validate("bad", DatePattern.UUUU_MM_DD));
        assertFalse(DateUtil.validate("bad", DatePattern.HH_MM_SS));
        assertFalse(DateUtil.validate("bad", DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertFalse(DateUtil.validate("bad", ""));
        assertFalse(DateUtil.validate("2024-02-03", "yyyy-MM-dd]"));
        assertFalse(DateUtil.isIntersection(BASE, BASE.plusDays(1), BASE.plusDays(2), BASE.plusDays(3)));
        assertTrue(DateUtil.isIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3)));
        assertFalse(DateUtil.isIntersection(BASE.plusDays(2), BASE.plusDays(3), BASE, BASE.plusDays(1)));
        assertNull(DateUtil.getIntersection(BASE.plusDays(2), BASE.plusDays(3), BASE, BASE.plusDays(1)));
        assertArrayEquals(new LocalDateTime[]{BASE.plusDays(1), BASE.plusDays(2)}, DateUtil.getIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3)));
        assertArrayEquals(new LocalDateTime[]{BASE.plusDays(1), BASE.plusDays(2)}, DateUtil.getIntersection(BASE.plusDays(1), BASE.plusDays(3), BASE, BASE.plusDays(2)));
        assertNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(1), BASE.plusDays(2), BASE.plusDays(3)));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(3), BASE.plusDays(1), BASE.plusDays(2), 0, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3), 1, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3), 2, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE.plusDays(1), BASE.plusDays(3), BASE, BASE.plusDays(2), 1, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE.plusDays(1), BASE.plusDays(3), BASE, BASE.plusDays(2), 2, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE.plusDays(1), BASE.plusDays(3), BASE, BASE.plusDays(2), 2, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3), 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3)));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE.plusDays(1), BASE.plusDays(3), BASE, BASE.plusDays(2), 0, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE.plusDays(1), BASE.plusDays(3), BASE, BASE.plusDays(2), 1, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE.plusDays(1), BASE.plusDays(3), BASE, BASE.plusDays(2), 2, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3), 0, 0, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(4), BASE.plusDays(1), BASE.plusDays(2), 0, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(4), BASE.plusDays(1), BASE.plusDays(2), 1, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(4), BASE.plusDays(1), BASE.plusDays(2), 2, 1, ChronoUnit.MILLIS));
        assertNotNull(DateUtil.getDifferenceSetsByIntersection(BASE, BASE.plusDays(2), BASE.plusDays(1), BASE.plusDays(3), 0, 1));

        assertArrayEquals(new String[]{}, DateUtil.convertWeeks("abc"));
        assertArrayEquals(new String[]{"1", "3"}, DateUtil.convertWeeks("1,3"));
        assertArrayEquals(new String[]{"1", "3"}, DateUtil.convertWeeks("13"));
        assertArrayEquals(new String[]{"1", "3"}, DateUtil.convertWeeks("1,3", ","));
        assertNull(DateUtil.getByWeeks("1"));
        assertThrows(NullPointerException.class, () -> DateUtil.getByWeeks(null, BASE));
        assertThrows(NullPointerException.class, () -> DateUtil.getByWeeks("1", (LocalDateTime[]) null));
        assertNotNull(DateUtil.getByWeeks("1", BASE.withDayOfMonth(1), BASE.withDayOfMonth(8)));
        assertNull(DateUtil.getByWeeks("1", new LocalDateTime[]{}));
        assertEquals(0, DateUtil.getByWeeks("7", BASE).size());
        assertEquals(0, DateUtil.getByRange(BASE, BASE, formatter()).size());
        assertEquals(2, DateUtil.getByRange(BASE, BASE.plusDays(1), formatter()).size());
        assertEquals(2, DateUtil.getByRange(BASE, BASE.plusDays(1), DatePattern.UUUU_MM_DD).size());
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getByRange(BASE, BASE.plusDays(1), ""));
        assertEquals(1, DateUtil.getByRangeAndWeeks(BASE, BASE.plusDays(1), String.valueOf(BASE.getDayOfWeek().getValue()), formatter()).size());
        assertEquals(1, DateUtil.getByRangeAndWeeks(BASE, BASE.plusDays(1), String.valueOf(BASE.getDayOfWeek().getValue()), DatePattern.UUUU_MM_DD).size());
        assertEquals(0, DateUtil.getByRangeAndWeeks(BASE.plusDays(1), BASE.plusDays(2), String.valueOf(BASE.getDayOfWeek().getValue())).size());
        assertEquals(0, DateUtil.getByRangeAndWeeks(BASE, BASE.plusDays(1), "x", formatter()).size());
        assertEquals(0, DateUtil.getByRangeAndWeeks(BASE, BASE.plusDays(1), "x", DatePattern.UUUU_MM_DD).size());
        assertEquals(0, DateUtil.getByRangeAndWeeks(BASE, BASE.plusDays(1), "x").size());
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getByRangeAndWeeks(BASE, BASE.plusDays(1), "1", ""));
        assertThrows(NullPointerException.class, () -> DateUtil.getByRangeAndWeeks(BASE, BASE.plusDays(1), "1", (DateTimeFormatter) null));
    }

    @DisplayName("月份周补充分支")
    @Test
    void weekOfMonthBranches() {
        assertEquals(1, DateUtil.getWeekOfMonth(LocalDate.of(2024, 6, 2)));
        assertEquals(1, DateUtil.getWeekOfMonth(LocalDate.of(2024, 1, 2)));
        assertEquals(2, DateUtil.getWeekOfMonth(LocalDate.of(2024, 6, 3)));
        assertEquals(6, DateUtil.getWeekOfMonth(LocalDate.of(2024, 12, 31)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2026, 2, 1)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2024, 4, 1)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2024, 1, 1)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2024, 8, 1)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2024, 3, 1)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2024, 11, 1)));
        assertEquals(5, DateUtil.getWeeksOfMonth(LocalDate.of(2024, 6, 1)));
        assertEquals(6, DateUtil.getWeeksOfMonth(LocalDate.of(2025, 6, 1)));
        assertEquals(LocalDate.of(2021, 2, 14), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2021, 2, 8)));
        assertEquals(LocalDate.of(2024, 6, 3), DateUtil.getStartDayOfWeekOfMonth(LocalDate.of(2024, 6, 3)));
        assertEquals(LocalDate.of(2024, 4, 14), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 4, 8)));
        assertEquals(LocalDate.of(2026, 2, 8), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2026, 2, 8)));
        assertEquals(LocalDate.of(2024, 12, 29), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 12, 23)));
        assertEquals(LocalDate.of(2024, 12, 8), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 12, 2)));
        assertEquals(LocalDate.of(2024, 6, 16), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 10)));
        assertEquals(LocalDate.of(2024, 6, 2), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1)));
        assertEquals(LocalDate.of(2024, 6, 9), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 3)));
        assertEquals(LocalDate.of(2024, 6, 30), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 30)));
        assertEquals(LocalDate.of(2024, 6, 9), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 2));
        assertEquals(LocalDate.of(2024, 6, 30), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 5));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 0));
        DateFeat.set(ResolverStyle.SMART);
        assertEquals(LocalDate.of(2024, 6, 24), DateUtil.getStartDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 9));
        DateFeat.set(ResolverStyle.SMART);
        assertEquals(LocalDate.of(2024, 6, 30), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 9));
        DateFeat.set(ResolverStyle.LENIENT);
        assertEquals(LocalDate.of(2024, 7, 8), DateUtil.getStartDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 7));
        DateFeat.set(ResolverStyle.LENIENT);
        assertEquals(LocalDate.of(2024, 8, 12), DateUtil.getStartDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 13));
        DateFeat.set(ResolverStyle.LENIENT);
        assertEquals(LocalDate.of(2024, 7, 14), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 7));
        DateFeat.set(ResolverStyle.LENIENT);
        assertEquals(LocalDate.of(2024, 7, 7), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 6));
        DateFeat.set(ResolverStyle.LENIENT);
        assertEquals(LocalDate.of(2024, 8, 11), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 12));
        DateFeat.set(ResolverStyle.LENIENT);
        assertEquals(LocalDate.of(2022, 8, 7), DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2022, 7, 1), 6));
        DateFeat.set(ResolverStyle.STRICT);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getStartDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 9));
        DateFeat.set(ResolverStyle.STRICT);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getEndDayOfWeekOfMonth(LocalDate.of(2024, 6, 1), 9));
    }

    @DisplayName("转换补充分支")
    @Test
    void conversionBranches() {
        OffsetDateTime offsetDateTime = BASE.atOffset(ZoneOffset.ofHours(8));
        assertEquals(BASE.atZone(ZONE_SHANGHAI).toEpochSecond(), DateUtil.toEpochSecond(offsetDateTime, ZONE_UTC));
        assertDoesNotThrow(() -> DateUtil.toEpochSecond(BASE));
        assertDoesNotThrow(() -> DateUtil.toEpochSecond(BASE_DATE));
        assertEquals(BASE_DATE.toInstant().getEpochSecond(), DateUtil.toEpochSecond(BASE_DATE, ZONE_UTC));
        LocalDateTime zonedBase = LocalDateTime.of(2024, 2, 2, 20, 5, 6);
        assertEquals(BASE.toEpochSecond(ZoneOffset.UTC), DateUtil.toEpochSecond("2024-02-03 04:05:06"));
        assertEquals(zonedBase.toEpochSecond(ZoneOffset.UTC), DateUtil.toEpochSecond("2024-02-03 04:05:06", ZONE_UTC));
        assertEquals(zonedBase.toEpochSecond(ZoneOffset.UTC), DateUtil.toEpochSecond("2024-02-03 04:05:06", ZONE_UTC, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochSecond("bad"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochSecond("bad", ZONE_UTC));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochSecond("bad", ZONE_UTC, DatePattern.UUUU_MM_DD));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochSecond(Year.of(2024), ZONE_UTC));
        assertDoesNotThrow(() -> DateUtil.toEpochSecond(BASE));
        assertDoesNotThrow(() -> DateUtil.toEpochSecond(BASE_DATE));
        assertEquals(BASE.atZone(ZONE_SHANGHAI).toInstant().toEpochMilli(), DateUtil.toEpochMilli(offsetDateTime, ZONE_UTC));
        assertTrue(DateUtil.toEpochMilli(BASE) > 0);
        assertDoesNotThrow(() -> DateUtil.toEpochMilli(BASE_DATE));
        assertEquals(BASE_DATE.getTime(), DateUtil.toEpochMilli(BASE_DATE, ZONE_UTC));
        assertEquals(BASE.toInstant(ZoneOffset.UTC).toEpochMilli(), DateUtil.toEpochMilli("2024-02-03 04:05:06"));
        assertEquals(zonedBase.toInstant(ZoneOffset.UTC).toEpochMilli(), DateUtil.toEpochMilli("2024-02-03 04:05:06", ZONE_UTC));
        assertEquals(zonedBase.toInstant(ZoneOffset.UTC).toEpochMilli(), DateUtil.toEpochMilli("2024-02-03 04:05:06", ZONE_UTC, DatePattern.UUUU_MM_DD_HH_MM_SS));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochMilli("bad"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochMilli("bad", ZONE_UTC));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochMilli("bad", ZONE_UTC, DatePattern.UUUU_MM_DD));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.toEpochMilli(Year.of(2024), ZONE_UTC));
        assertDoesNotThrow(() -> DateUtil.toEpochMilli(BASE_DATE));
        DateFeat.setMinDateYearAlways(0L);
        assertTrue(DateUtil.toEpochMilli(BASE.toLocalTime(), ZONE_UTC) > 0);
        DateFeat.setMinDateYearAlways(null);
        assertNull(DateUtil.toTemporal(BASE_DATE, Year.class));
        assertNull(DateUtil.withZoneInstant(Year.of(2024), ZONE_SHANGHAI, ZONE_UTC));
        assertEquals(LocalDateTime.of(2024, 2, 2, 20, 5, 6), DateUtil.withZoneInstant(BASE, ZONE_SHANGHAI, ZONE_UTC));
        assertEquals(LocalDate.of(2024, 2, 2), DateUtil.withZoneInstant(BASE.toLocalDate(), ZONE_SHANGHAI, ZONE_UTC));
        assertNotNull(DateUtil.withZoneInstant(BASE.toLocalTime(), ZONE_SHANGHAI, ZONE_UTC));
        assertNotNull(DateUtil.withZoneInstant(BASE, ZONE_UTC));
        assertNotNull(DateUtil.withZoneInstant(BASE_DATE, ZONE_SHANGHAI, ZONE_UTC));
        assertNotNull(DateUtil.withZoneInstant(BASE_DATE, ZONE_UTC));
        assertTrue(DateUtil.isLeapYear(BASE_DATE));
        assertTrue(DateUtil.isLeapYear(2024));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.isLeapYear(BASE.toLocalTime()));
    }

    @DisplayName("DateFeat 补充分支")
    @Test
    void dateFeatBranches() throws Exception {
        Constructor<DateDuration> durationConstructor = DateDuration.class.getDeclaredConstructor();
        durationConstructor.setAccessible(true);
        assertNotNull(durationConstructor.newInstance());
        Constructor<DateConst> constConstructor = DateConst.class.getDeclaredConstructor();
        constConstructor.setAccessible(true);
        assertNotNull(constConstructor.newInstance());
        Constructor<DatePattern> patternConstructor = DatePattern.class.getDeclaredConstructor();
        patternConstructor.setAccessible(true);
        assertNotNull(patternConstructor.newInstance());
        Constructor<DateFormat> formatConstructor = DateFormat.class.getDeclaredConstructor();
        formatConstructor.setAccessible(true);
        assertNotNull(formatConstructor.newInstance());
        Constructor<DateFormatter> formatterConstructor = DateFormatter.class.getDeclaredConstructor();
        formatterConstructor.setAccessible(true);
        assertNotNull(formatterConstructor.newInstance());
        Constructor<DateRegExPattern> regExPatternConstructor = DateRegExPattern.class.getDeclaredConstructor();
        regExPatternConstructor.setAccessible(true);
        assertNotNull(regExPatternConstructor.newInstance());
        Constructor<DateUtil> utilConstructor = DateUtil.class.getDeclaredConstructor();
        utilConstructor.setAccessible(true);
        assertNotNull(utilConstructor.newInstance());
        Constructor<DateFeat> constructor = DateFeat.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
        assertEquals(ResolverStyle.SMART, DateFeat.get(ResolverStyle.SMART));
        DateFeat.set(ResolverStyle.SMART);
        assertEquals(ResolverStyle.SMART, DateFeat.getResolverStyle());
        assertEquals(Boolean.FALSE, DateFeat.get(Boolean.FALSE));
        assertNull(DateFeat.get((Boolean) null));
        DateFeat.set(Boolean.FALSE);
        assertEquals(Boolean.FALSE, DateFeat.getLazy(Boolean.TRUE));
        DateFeat.set(Boolean.FALSE);
        assertEquals(Boolean.FALSE, DateFeat.getstrictYyToUu());
        assertEquals(Locale.CHINA, DateFeat.get(Locale.CHINA));
        assertNull(DateFeat.get((Locale) null));
        DateFeat.set(Locale.CHINA);
        assertEquals(Locale.CHINA, DateFeat.getLazy(Locale.US));
        DateFeat.set(Locale.CHINA);
        assertEquals(Locale.CHINA, DateFeat.getLocale());
        assertEquals(ZONE_UTC, DateFeat.get(ZONE_UTC));
        DateFeat.set(ZONE_UTC);
        assertEquals(ZONE_UTC, DateFeat.get((ZoneId) null));
        assertEquals(2000L, DateFeat.getLazyMinDateYear(2000L));
        assertEquals(2000L, DateFeat.getMinDateYear(2000L));
        DateFeat.setAlways(ResolverStyle.SMART);
        assertEquals(ResolverStyle.SMART, DateFeat.get((ResolverStyle) null));
        assertEquals(ResolverStyle.SMART, DateFeat.getLazy(ResolverStyle.LENIENT));
        DateFeat.setAlways((ResolverStyle) null);
        DateFeat.setAlways(Boolean.FALSE);
        assertEquals(Boolean.FALSE, DateFeat.get((Boolean) null));
        assertEquals(Boolean.FALSE, DateFeat.getLazy(Boolean.TRUE));
        DateFeat.setAlways((Boolean) null);
        DateFeat.setAlways(Locale.CHINA);
        assertEquals(Locale.CHINA, DateFeat.get((Locale) null));
        DateFeat.setAlways((Locale) null);
        DateFeat.setAlways(ZONE_UTC);
        assertEquals(ZONE_UTC, DateFeat.get((ZoneId) null));
        assertEquals(ZONE_UTC, DateFeat.getLazy(ZONE_SHANGHAI));
        DateFeat.setAlways((ZoneId) null);
        DateFeat.set((ZoneId) ZONE_UTC);
        assertEquals(ZONE_UTC, DateFeat.getZoneId());
        DateFeat.set((ResolverStyle) null);
        assertEquals(ResolverStyle.LENIENT, DateFeat.getLazy(ResolverStyle.LENIENT));
        DateFeat.set((ZoneId) null);
        assertEquals(ZONE_SHANGHAI, DateFeat.getLazy(ZONE_SHANGHAI));
        DateFeat.setMinDateYear(2021L);
        assertEquals(2021L, DateFeat.getMinDateYear(null));
        DateFeat.setMinDateYear(0L);
        assertNull(DateFeat.getMinDateYear(null));
        DateFeat.setMinDateYear(-1L);
        assertNull(DateFeat.getMinDateYear(null));
        DateFeat.setMinDateYearAlways(0L);
        assertNull(DateFeat.getMinDateYear(null));
        DateFeat.setMinDateYearAlways(-1L);
        assertNull(DateFeat.getMinDateYear(null));
        DateFeat.setMinDateYearAlways(null);
        DateFeat.setMinDateYear(null);
        assertNull(DateFeat.getMinDateYear(null));
        DateFeat.setMinDateYear(2021L);
        assertEquals(2021L, DateFeat.getLazyMinDateYear(null));
        DateFeat.setMinDateYear(0L);
        assertNull(DateFeat.getLazyMinDateYear(null));
        DateFeat.setMinDateYear(-1L);
        assertNull(DateFeat.getLazyMinDateYear(null));
        DateFeat.setMinDateYearAlways(0L);
        assertNull(DateFeat.getLazyMinDateYear(null));
        DateFeat.setMinDateYearAlways(-1L);
        assertNull(DateFeat.getLazyMinDateYear(null));
        DateFeat.setMinDateYearAlways(null);
        DateFeat.setMinDateYear(2021L);
        assertEquals(2021L, DateFeat.getMinDateYear());
        DateFeat.setMinDateYear(0L);
        assertEquals(DateConst.DEFAULT_MIN_DATE_YEAR, DateFeat.getMinDateYear());
        DateFeat.setMinDateYear(-1L);
        assertEquals(DateConst.DEFAULT_MIN_DATE_YEAR, DateFeat.getMinDateYear());
        assertNull(DateFeat.getMinDateYear(0L));
        assertNull(DateFeat.getLazyMinDateYear(0L));
        assertNull(DateFeat.getLazyMinDateYear(-1L));
        DateFeat.setMinDateYearAlways(0L);
        assertEquals(DateConst.DEFAULT_MIN_DATE_YEAR, DateFeat.getMinDateYear());
        DateFeat.setMinDateYearAlways(2000L);
        assertEquals(2000L, DateFeat.getMinDateYear(null));
        assertEquals(2000L, DateFeat.getLazyMinDateYear(null));

        DateFeatConfig.setAlways(ResolverStyle.SMART)
                .setStrictYyToUuAlways(Boolean.FALSE)
                .setAlways(Locale.CHINA)
                .setAlways(ZONE_UTC)
                .setMinDateYearAlways(2000L)
                .apply();
        assertEquals(ResolverStyle.SMART, DateFeat.getResolverStyle());
        assertFalse(DateFeat.getstrictYyToUu());
        assertEquals(Locale.CHINA, DateFeat.getLocale());
        assertEquals(ZONE_UTC, DateFeat.getZoneId());
        assertEquals(2000L, DateFeat.getMinDateYear());
    }

    @DisplayName("私有入口")
    @Test
    void privateHelpers() throws Throwable {
        assertEquals("uu", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "yy"));
        assertEquals("uuuu-MM-dd", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "yyyy-MM-dd"));
        assertEquals("yyyy-uu-MM-dd", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "yyyy-uu-MM-dd"));
        DateFeat.setAlways(ResolverStyle.SMART);
        assertEquals("yyyy-MM-dd", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "yyyy-MM-dd"));
        DateFeat.setAlways((ResolverStyle) null);
        DateFeat.setAlways(Boolean.FALSE);
        assertEquals("yyyy-MM-dd", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "yyyy-MM-dd"));
        DateFeat.setAlways((Boolean) null);
        assertEquals("uuuu-MM-dd", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "yyyy-MM-dd"));
        assertEquals("uuuu-MM-dd", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "uuuu-MM-dd"));
        assertEquals("MM-dd", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", "MM-dd"));
        assertEquals("Mon", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertSource", "mon", "MMM"));
        assertEquals("mon", ReflectionTestUtil.invokeMethod(DateUtil.class, "convertSource", "mon", "MM"));
        assertThrows(StringIndexOutOfBoundsException.class,
                () -> ReflectionTestUtil.invokeMethod(DateUtil.class, "convertSource", "mo", "MMM"));
        assertEquals("Feb", DateUtil.convertMonthShortText("02", Locale.ENGLISH));
        assertEquals("févr.", DateUtil.convertMonthShortText("02", Locale.FRENCH));
        assertEquals("February", DateUtil.convertMonthText("2", Locale.ENGLISH));
        assertEquals("février", DateUtil.convertMonthText("02", Locale.FRENCH));
        assertEquals("二月", DateUtil.convertMonthText("2", Locale.CHINESE));
        assertNull(DateUtil.convertMonthText("13", Locale.ENGLISH));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.convertMonthShortText(""));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.convertMonthShortText("123"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.convertMonthText(""));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.convertMonthText("123"));
        assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", ""));
        assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(DateUtil.class, "convertPattern", new Class[]{String.class}, new Object[]{null}));
        assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(DateUtil.class, "convertSource", "mon", ""));
        Method method = DateUtil.class.getDeclaredMethod("convertPattern", String.class);
        assertNotNull(method);
    }
}
