package top.csaf.idcard;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import top.csaf.date.DateUtil;

import java.time.*;
import java.util.Date;

/**
 * 身份证工具类
 * <p>
 * 参考：
 * <ul>
 *   <li><a href="https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=080D6FBF2BB468F9007657F26D60013E">国家标准 | GB 11643-1999</a></li>
 *   <li><a href="https://zh.wikipedia.org/wiki/%E6%A0%A1%E9%AA%8C%E7%A0%81">校验码 - 维基百科，自由的百科全书</a></li>
 * </ul>
 */
@Slf4j
public class IdCardUtil {

  private static final int NUMBER_LEN_CHINA1 = 15;
  private static final int NUMBER_LEN_CHINA2 = 18;

  /**
   * 使用 ISO 7064:1983, MOD 11-2 校验身份证号码最后一位校验码
   * <p>
   * 前 17 位分别乘以不同的系数：7-9-10-5-8-4-2-1-6-3-7-9-10-5-8-4-2
   * 再将这 17 个乘积相加，然后除以 11，得到余数
   * 余数只能为：  0-1-2-3-4-5-6-7-8-9-10
   * 对应的效验码：1-0-X-9-8-7-6-5-4-3-2
   *
   * @param number 身份证号码
   * @return 校验结果
   */
  public static boolean validateCheckCode(@NonNull final String number) {
    if (number.length() != NUMBER_LEN_CHINA2) {
      throw new IllegalArgumentException("Number: ID Card number must be 18 digits");
    }
    final int[] coefficient = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    final char[] checkCode = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    int sum = 0;
    for (int i = 0; i < coefficient.length; i++) {
      sum += (number.charAt(i) - '0') * coefficient[i];
    }
    return checkCode[sum % 11] == number.charAt(17);
  }

  /**
   * 异常校验
   * <p>
   * 前六位地区码由于不断变化，不做正确性校验
   *
   * @param number 身份证号码
   */
  public static void exceptionValidate(@NonNull final String number) {
    int len = number.length();
    if (len != NUMBER_LEN_CHINA1 && len != NUMBER_LEN_CHINA2) {
      throw new IllegalArgumentException("Number: length should be 15 or 18");
    }
    // 不能是 0 开头
    if (number.charAt(0) == '0') {
      throw new IllegalArgumentException("Number: cannot start with 0");
    }
    // 18 位身份证号码
    if (len == NUMBER_LEN_CHINA2) {
      // 前 17 位必须是 0~9
      for (int i = 0; i < NUMBER_LEN_CHINA2 - 1; i++) {
        char c = number.charAt(i);
        if (c < '0' || c > '9') {
          throw new IllegalArgumentException("Number: the first 17 digits of 18 digits ID Card number should be 0~9");
        }
      }
      // // 出生年 > 1850
      // if (Integer.parseInt(number.substring(6, 10)) < 1850) {
      //   log.error("Number: year should be greater than 1850");
      //   return false;
      // }
      // 年月日校验
      if (!DateUtil.validate(number.substring(6, 14), "uuuuMMdd")) {
        throw new IllegalArgumentException("Number: the date of index 6~14 is invalid");
      }
      // 顺序码不能全为 0
      if ("000".equals(number.substring(14, 17))) {
        throw new IllegalArgumentException("Number: the order code of index 14~17 cannot be 000");
      }
      // 校验码校验
      if (!validateCheckCode(number)) {
        throw new IllegalArgumentException("Number: the check code of index 17 is invalid");
      }
    } else {
      // 必须是 0~9
      for (int i = 0; i < NUMBER_LEN_CHINA1; i++) {
        char c = number.charAt(i);
        if (c < '0' || c > '9') {
          throw new IllegalArgumentException("Number: ID Card number should be 0~9");
        }
      }
      // 年月日校验
      if (!DateUtil.validate(number.substring(6, 12), "uuMMdd")) {
        throw new IllegalArgumentException("Number: the date of index 6~12 is invalid");
      }
      // 顺序码不能全为 0
      if ("000".equals(number.substring(12))) {
        throw new IllegalArgumentException("Number: the order code of index 12~15 cannot be 000");
      }
    }
  }

  /**
   * 校验
   * <p>
   * 前六位地区码由于不断变化，不做正确性校验
   *
   * @param number 身份证号码
   * @return 是否通过校验
   */
  public static boolean validate(@NonNull final String number) {
    try {
      exceptionValidate(number);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }
  }

  /**
   * 获取省级编码
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 省级编码
   */
  public static String getProvinceCode(@NonNull final String number, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return null;
    }
    return number.substring(0, 2);
  }

  /**
   * 获取省级编码
   *
   * @param number 身份证号码
   * @return 省级编码
   */
  public static String getProvinceCode(@NonNull final String number) {
    return getProvinceCode(number, true);
  }

  /**
   * 获取市级编码
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 市级编码
   */
  public static String getCityCode(@NonNull final String number, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return null;
    }
    return number.substring(2, 4);
  }

  /**
   * 获取市级编码
   *
   * @param number 身份证号码
   * @return 市级编码
   */
  public static String getCityCode(@NonNull final String number) {
    return getCityCode(number, true);
  }

  /**
   * 获取区级编码
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 区级编码
   */
  public static String getDistrictCode(@NonNull final String number, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return null;
    }
    return number.substring(4, 6);
  }

  /**
   * 获取区级编码
   *
   * @param number 身份证号码
   * @return 区级编码
   */
  public static String getDistrictCode(@NonNull final String number) {
    return getDistrictCode(number, true);
  }

  /**
   * 获取出生日期
   *
   * @param number     身份证号码
   * @param <T>        日期类型
   * @param isValidate 是否校验
   * @param clazz      日期类型
   * @return 出生日期
   */
  public static <T> T getBirthday(@NonNull final String number, @NonNull Class<T> clazz, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return null;
    }
    String year = null;
    String month = null;
    String day = null;
    if (number.length() == NUMBER_LEN_CHINA1) {
      year = "19" + number.substring(6, 8);
      month = number.substring(8, 10);
      day = number.substring(10, 12);
    } else if (number.length() == NUMBER_LEN_CHINA2) {
      year = number.substring(6, 10);
      month = number.substring(10, 12);
      day = number.substring(12, 14);
    }
    if (clazz == LocalDateTime.class) {
      return (T) LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 0, 0, 0);
    } else if (clazz == LocalDate.class) {
      return (T) LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
    } else if (clazz == Date.class) {
      return (T) DateUtil.parseDate(year + month + day, "yyyyMMdd");
    } else if (clazz == String.class) {
      return (T) (year + "-" + month + "-" + day);
    } else if (clazz == Long.class || clazz == long.class) {
      return (T) Long.valueOf(LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 0, 0, 0).toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.now())) * 1000L);
    }
    return null;
  }

  /**
   * 获取出生日期
   *
   * @param number 身份证号码
   * @param <T>    日期类型
   * @param clazz  日期类型
   * @return 出生日期
   */
  public static <T> T getBirthday(@NonNull final String number, @NonNull Class<T> clazz) {
    return getBirthday(number, clazz, true);
  }

  /**
   * 获取年龄，如果身份证号错误，返回-1
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 年龄
   */
  public static int getAge(@NonNull final String number, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return -1;
    }
    LocalDate birthday = getBirthday(number, LocalDate.class);
    return Period.between(birthday, LocalDate.now()).getYears();
  }

  /**
   * 获取年龄，如果身份证号错误，返回-1
   *
   * @param number 身份证号码
   * @return 年龄
   */
  public static int getAge(@NonNull final String number) {
    return getAge(number, true);
  }

  /**
   * 获取性别数字
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 性别
   */
  public static Integer getGenderCode(@NonNull final String number, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return null;
    }
    if (number.length() == NUMBER_LEN_CHINA1) {
      return Integer.valueOf(number.substring(number.length() - 2, number.length() - 1));
    } else if (number.length() == NUMBER_LEN_CHINA2) {
      return Integer.valueOf(number.substring(number.length() - 2, number.length() - 1));
    }
    return null;
  }

  /**
   * 获取性别数字
   *
   * @param number 身份证号码
   * @return 性别
   */
  public static Integer getGenderCode(@NonNull final String number) {
    return getGenderCode(number, true);
  }

  /**
   * 获取性别：-1: 错误, 1: 男, 2: 女
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 性别
   */
  public static int getGender(@NonNull final String number, final boolean isValidate) {
    Integer genderCode = getGenderCode(number, isValidate);
    if (genderCode == null) {
      return -1;
    }
    return genderCode % 2 != 0 ? 1 : 2;
  }

  /**
   * 获取性别：-1: 错误, 1: 男, 2: 女
   *
   * @param number 身份证号码
   * @return 性别
   */
  public static int getGender(@NonNull final String number) {
    return getGender(number, true);
  }

  /**
   * 顺序码为奇数则为男性
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 是否为男性
   */
  public static Boolean isMale(@NonNull final String number, final boolean isValidate) {
    Integer gender = getGenderCode(number, isValidate);
    if (gender == null) {
      return null;
    }
    return gender % 2 != 0;
  }

  /**
   * 顺序码为奇数则为男性
   *
   * @param number 身份证号码
   * @return 是否为男性
   */
  public static Boolean isMale(@NonNull final String number) {
    return isMale(number, true);
  }

  /**
   * 顺序码为偶数则为女性
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 是否为女性
   */
  public static Boolean isFemale(@NonNull final String number, final boolean isValidate) {
    Integer gender = getGenderCode(number, isValidate);
    if (gender == null) {
      return null;
    }
    return gender % 2 == 0;
  }

  /**
   * 顺序码为偶数则为女性
   *
   * @param number 身份证号码
   * @return 是否为女性
   */
  public static Boolean isFemale(@NonNull final String number) {
    return isFemale(number, true);
  }

  /**
   * 获取校验码
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 校验码
   */
  public static String getCheckCode(@NonNull final String number, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return null;
    }
    if (number.length() != NUMBER_LEN_CHINA2) {
      return null;
    }
    return number.substring(number.length() - 1);
  }

  /**
   * 获取校验码
   *
   * @param number 身份证号码
   * @return 校验码
   */
  public static String getCheckCode(@NonNull final String number) {
    return getCheckCode(number, true);
  }

  /**
   * 获取身份证对象
   *
   * @param number     身份证号码
   * @param isValidate 是否校验
   * @return 身份证对象
   */
  public static IdCard get(@NonNull final String number, final boolean isValidate) {
    if (isValidate && !validate(number)) {
      return null;
    }
    IdCard idCard = new IdCard();
    idCard.setNumber(number);
    idCard.setProvinceCode(getProvinceCode(number, false));
    idCard.setCityCode(getCityCode(number, false));
    idCard.setDistrictCode(getDistrictCode(number, false));
    idCard.setBirthday(getBirthday(number, LocalDate.class, false));
    idCard.setAge(getAge(number, false));
    idCard.setGender(getGender(number, false));
    idCard.setCheckCode(getCheckCode(number, false));
    return idCard;
  }

  /**
   * 获取身份证对象
   *
   * @param number 身份证号码
   * @return 身份证对象
   */
  public static IdCard get(@NonNull final String number) {
    return get(number, true);
  }
}
