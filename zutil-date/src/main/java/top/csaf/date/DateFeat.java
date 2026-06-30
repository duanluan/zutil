package top.csaf.date;

import lombok.extern.slf4j.Slf4j;
import top.csaf.date.constant.DateConst;

import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * 时间特性，决定着 {@link DateUtil} 的处理方式，“_ALWAYS”结尾的总是生效
 */
@Slf4j
public class DateFeat {

  /**
   * 解析器模式
   */
  private static final ThreadLocal<ResolverStyle> RESOLVER_STYLE = new ThreadLocal<>();
  /**
   * 持久的解析器模式
   */
  private static volatile ResolverStyle RESOLVER_STYLE_ALWAYS = null;

  /**
   * 设置解析器模式
   *
   * @param resolverStyle 解析器模式
   */
  public static void set(final ResolverStyle resolverStyle) {
    RESOLVER_STYLE.set(resolverStyle);
  }

  /**
   * 设置持久的解析器模式
   *
   * @param resolverStyle 持久的解析器模式
   */
  public static void setAlways(final ResolverStyle resolverStyle) {
    RESOLVER_STYLE_ALWAYS = resolverStyle;
  }

  /**
   * 获取解析器模式
   *
   * @param resolverStyle 解析器模式
   * @return 解析器模式，默认为 null
   */
  public static ResolverStyle get(final ResolverStyle resolverStyle) {
    if (resolverStyle != null) {
      return resolverStyle;
    } else if (RESOLVER_STYLE_ALWAYS != null) {
      return RESOLVER_STYLE_ALWAYS;
    } else if (RESOLVER_STYLE.get() != null) {
      ResolverStyle resolverStyle1 = RESOLVER_STYLE.get();
      RESOLVER_STYLE.remove();
      return resolverStyle1;
    }
    return null;
  }

  /**
   * 获取解析器模式
   *
   * @param resolverStyle 解析器模式
   * @return 解析器模式，默认为形参
   */
  public static ResolverStyle getLazy(final ResolverStyle resolverStyle) {
    if (RESOLVER_STYLE_ALWAYS != null) {
      return RESOLVER_STYLE_ALWAYS;
    } else if (RESOLVER_STYLE.get() != null) {
      ResolverStyle resolverStyle1 = RESOLVER_STYLE.get();
      RESOLVER_STYLE.remove();
      return resolverStyle1;
    }
    return resolverStyle;
  }

  /**
   * 获取解析器模式
   *
   * @return 解析器模式，默认为 {@link DateConst#DEFAULT_RESOLVER_STYLE}
   */
  public static ResolverStyle getResolverStyle() {
    if (RESOLVER_STYLE_ALWAYS != null) {
      return RESOLVER_STYLE_ALWAYS;
    } else if (RESOLVER_STYLE.get() != null) {
      ResolverStyle resolverStyle1 = RESOLVER_STYLE.get();
      RESOLVER_STYLE.remove();
      return resolverStyle1;
    }
    return DateConst.DEFAULT_RESOLVER_STYLE;
  }

  /**
   * 严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   */
  private static final ThreadLocal<Boolean> STRICT_YY_TO_UU = new ThreadLocal<>();
  /**
   * 持久的严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   */
  private static volatile Boolean STRICT_YY_TO_UU_ALWAYS = null;

  /**
   * 设置严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   *
   * @param strictYyToUu 严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   */
  public static void set(final Boolean strictYyToUu) {
    STRICT_YY_TO_UU.set(strictYyToUu);
  }

  /**
   * 设置持久的严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   *
   * @param strictYyToUu 持久的严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   */
  public static void setAlways(final Boolean strictYyToUu) {
    STRICT_YY_TO_UU_ALWAYS = strictYyToUu;
  }

  /**
   * 获取严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   *
   * @param strictYyToUu 严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   * @return 严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者，默认为 null
   */
  public static Boolean get(final Boolean strictYyToUu) {
    if (strictYyToUu != null) {
      return strictYyToUu;
    } else if (STRICT_YY_TO_UU_ALWAYS != null) {
      return STRICT_YY_TO_UU_ALWAYS;
    } else if (STRICT_YY_TO_UU.get() != null) {
      Boolean strictYyToUu1 = STRICT_YY_TO_UU.get();
      STRICT_YY_TO_UU.remove();
      return strictYyToUu1;
    }
    return null;
  }

  /**
   * 获取严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   *
   * @param strictYyToUu 严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   * @return 严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者，默认为形参
   */
  public static Boolean getLazy(final Boolean strictYyToUu) {
    if (STRICT_YY_TO_UU_ALWAYS != null) {
      return STRICT_YY_TO_UU_ALWAYS;
    } else if (STRICT_YY_TO_UU.get() != null) {
      Boolean strictYyToUu1 = STRICT_YY_TO_UU.get();
      STRICT_YY_TO_UU.remove();
      return strictYyToUu1;
    }
    return strictYyToUu;
  }

  /**
   * 获取严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者
   *
   * @return 严格模式时，如果有 yyyy/yy 没有 uuuu/uu，前者转换为后者，默认为 true
   */
  public static Boolean getstrictYyToUu() {
    if (STRICT_YY_TO_UU_ALWAYS != null) {
      return STRICT_YY_TO_UU_ALWAYS;
    } else if (STRICT_YY_TO_UU.get() != null) {
      Boolean strictYyToUu1 = STRICT_YY_TO_UU.get();
      STRICT_YY_TO_UU.remove();
      return strictYyToUu1;
    }
    return true;
  }

  /**
   * 区域，比如月份是中文还是英文
   */
  private static final ThreadLocal<Locale> LOCALE = new ThreadLocal<>();
  /**
   * 持久的区域，比如月份是中文还是英文
   */
  private static volatile Locale LOCALE_ALWAYS = null;

  /**
   * 设置区域
   *
   * @param locale 区域
   */
  public static void set(final Locale locale) {
    LOCALE.set(locale);
  }

  /**
   * 设置持久的区域
   *
   * @param locale 持久的区域
   */
  public static void setAlways(final Locale locale) {
    LOCALE_ALWAYS = locale;
  }

  /**
   * 获取区域
   *
   * @param locale 区域
   * @return 区域，默认为 null
   */
  public static Locale get(final Locale locale) {
    if (locale != null) {
      return locale;
    } else if (LOCALE_ALWAYS != null) {
      return LOCALE_ALWAYS;
    } else if (LOCALE.get() != null) {
      Locale locale1 = LOCALE.get();
      LOCALE.remove();
      return locale1;
    }
    return null;
  }

  /**
   * 获取区域
   *
   * @param locale 区域
   * @return 区域，默认为形参
   */
  public static Locale getLazy(final Locale locale) {
    if (LOCALE_ALWAYS != null) {
      return LOCALE_ALWAYS;
    } else if (LOCALE.get() != null) {
      Locale locale1 = LOCALE.get();
      LOCALE.remove();
      return locale1;
    }
    return locale;
  }

  /**
   * 获取区域
   *
   * @return 区域，默认为 {@link DateConst#DEFAULT_LOCALE}
   */
  public static Locale getLocale() {
    if (LOCALE_ALWAYS != null) {
      return LOCALE_ALWAYS;
    } else if (LOCALE.get() != null) {
      Locale locale1 = LOCALE.get();
      LOCALE.remove();
      return locale1;
    }
    return DateConst.DEFAULT_LOCALE;
  }

  /**
   * 时区
   */
  private static final ThreadLocal<ZoneId> ZONE_ID = new ThreadLocal<>();
  /**
   * 持久的时区
   */
  private static volatile ZoneId ZONE_ID_ALWAYS = null;

  /**
   * 设置时区
   *
   * @param zoneId 时区
   */
  public static void set(final ZoneId zoneId) {
    ZONE_ID.set(zoneId);
  }

  /**
   * 设置持久的时区
   *
   * @param zoneId 持久的时区
   */
  public static void setAlways(final ZoneId zoneId) {
    ZONE_ID_ALWAYS = zoneId;
  }

  /**
   * 获取时区
   *
   * @param zoneId 时区
   * @return 时区，默认为 null
   */
  public static ZoneId get(final ZoneId zoneId) {
    if (zoneId != null) {
      return zoneId;
    } else if (ZONE_ID_ALWAYS != null) {
      return ZONE_ID_ALWAYS;
    } else if (ZONE_ID.get() != null) {
      ZoneId zoneId1 = ZONE_ID.get();
      ZONE_ID.remove();
      return zoneId1;
    }
    return null;
  }

  /**
   * 获取时区
   *
   * @param zoneId 时区
   * @return 时区，默认为形参
   */
  public static ZoneId getLazy(final ZoneId zoneId) {
    if (ZONE_ID_ALWAYS != null) {
      return ZONE_ID_ALWAYS;
    } else if (ZONE_ID.get() != null) {
      ZoneId zoneId1 = ZONE_ID.get();
      ZONE_ID.remove();
      return zoneId1;
    }
    return zoneId;
  }

  /**
   * 获取时区
   *
   * @return 时区，默认为 {@link DateConst#SYSTEM_ZONE_ID}
   */
  public static ZoneId getZoneId() {
    if (ZONE_ID_ALWAYS != null) {
      return ZONE_ID_ALWAYS;
    } else if (ZONE_ID.get() != null) {
      ZoneId zoneId1 = ZONE_ID.get();
      ZONE_ID.remove();
      return zoneId1;
    }
    return DateConst.SYSTEM_ZONE_ID;
  }

  /**
   * 最小 Date 年
   */
  private static final ThreadLocal<Long> MIN_DATE_YEAR = new ThreadLocal<>();
  /**
   * 持久的最小 Date 年
   */
  private static volatile Long MIN_DATE_YEAR_ALWAYS = null;

  /**
   * 设置最小 Date 年
   *
   * @param minDateYear 最小 Date 年
   */
  public static void setMinDateYear(final Long minDateYear) {
    MIN_DATE_YEAR.set(minDateYear);
  }

  /**
   * 设置持久的最小 Date 年
   *
   * @param minDateYear 持久的最小 Date 年
   */
  public static void setMinDateYearAlways(final Long minDateYear) {
    MIN_DATE_YEAR_ALWAYS = minDateYear;
  }

  /**
   * 获取最小 Date 年
   *
   * @param minDateYear 最小 Date 年
   * @return 最小 Date 年，默认为 null
   */
  public static Long getMinDateYear(final Long minDateYear) {
    if (minDateYear != null && minDateYear > 0) {
      return minDateYear;
    } else if (MIN_DATE_YEAR_ALWAYS != null && MIN_DATE_YEAR_ALWAYS > 0) {
      return MIN_DATE_YEAR_ALWAYS;
    } else if (MIN_DATE_YEAR.get() != null && MIN_DATE_YEAR.get() > 0) {
      Long minDateYear1 = MIN_DATE_YEAR.get();
      MIN_DATE_YEAR.remove();
      return minDateYear1;
    }
    return null;
  }

  /**
   * 获取最小 Date 年
   *
   * @param minDateYear 最小 Date 年
   * @return 最小 Date 年，默认为形参
   */
  public static Long getLazyMinDateYear(final Long minDateYear) {
    if (MIN_DATE_YEAR_ALWAYS != null && MIN_DATE_YEAR_ALWAYS > 0) {
      return MIN_DATE_YEAR_ALWAYS;
    } else if (MIN_DATE_YEAR.get() != null && MIN_DATE_YEAR.get() > 0) {
      Long minDateYear1 = MIN_DATE_YEAR.get();
      MIN_DATE_YEAR.remove();
      return minDateYear1;
    } else if (minDateYear != null && minDateYear > 0) {
      return minDateYear;
    }
    return null;
  }

  /**
   * 获取最小 Date 年
   *
   * @return 最小 Date 年，默认为 {@link DateConst#DEFAULT_MIN_DATE_YEAR}
   */
  public static Long getMinDateYear() {
    if (MIN_DATE_YEAR_ALWAYS != null && MIN_DATE_YEAR_ALWAYS > 0) {
      return MIN_DATE_YEAR_ALWAYS;
    } else if (MIN_DATE_YEAR.get() != null && MIN_DATE_YEAR.get() > 0) {
      Long minDateYear1 = MIN_DATE_YEAR.get();
      MIN_DATE_YEAR.remove();
      return minDateYear1;
    }
    return DateConst.DEFAULT_MIN_DATE_YEAR;
  }
}
