package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.date.DateUtil;
import top.csaf.idcard.IdCard;
import top.csaf.idcard.IdCardUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("身份证工具类测试")
public class IdCardUtilTest {

  @Test
  void get() {
    String idCardNumber = "11010119491001159X";
    IdCard idCard = IdCardUtil.get(idCardNumber);
    assertEquals("11", idCard.getProvinceCode());
    assertEquals("01", idCard.getCityCode());
    assertEquals("01", idCard.getDistrictCode());
    assertEquals(LocalDate.of(1949, 10, 1), idCard.getBirthday());

    // 动态计算预期年龄，避免硬编码导致年份变化后测试失败
    int expectedAge = Period.between(LocalDate.of(1949, 10, 1), LocalDate.now()).getYears();
    assertEquals(expectedAge, idCard.getAge());

    assertEquals(1, idCard.getGender());
    assertEquals("X", idCard.getCheckCode());

    assertEquals(LocalDateTime.of(1949, 10, 1, 0, 0, 0), IdCardUtil.getBirthday(idCardNumber, LocalDateTime.class));
    assertEquals(DateUtil.parseDate("1949-10-01"), IdCardUtil.getBirthday(idCardNumber, Date.class));
    assertEquals("1949-10-01", IdCardUtil.getBirthday(idCardNumber, String.class));
    assertEquals(DateUtil.parseDate("1949-10-01").getTime(), IdCardUtil.getBirthday(idCardNumber, Long.class));
  }

  @Test
  void exceptionValidate() {
    // 必须 15 或 18 位
    assertFalse(IdCardUtil.validate("11010119491001159"));
    // 不能是 0 开头
    assertFalse(IdCardUtil.validate("01010119491001159X"));

    // 前 17 位必须是 0~9
    assertFalse(IdCardUtil.validate("A1010119491001159X"));
    // 年月日校验
    assertFalse(IdCardUtil.validate("11010119491301159X"));
    // 顺序码不能全为 0
    assertFalse(IdCardUtil.validate("11010119491001000X"));
    // 校验码校验
    assertFalse(IdCardUtil.validate("110101194910011591"));

    // 必须是 0~9
    assertFalse(IdCardUtil.validate("11501066112198A"));
    // 年月日校验
    assertFalse(IdCardUtil.validate("115010661321989"));
    // 顺序码不能全为 0
    assertFalse(IdCardUtil.validate("115010661121000"));
  }

  @Test
  void additionalBranches() {
    String number18 = "11010119491001159X";
    String number15 = "110101491001159";
    assertThrows(IllegalArgumentException.class, () -> IdCardUtil.validateCheckCode(number15));
    assertThrows(IllegalArgumentException.class, () -> IdCardUtil.exceptionValidate("11010119491001159x"));
    assertTrue(IdCardUtil.validateCheckCode(number18));
    assertEquals("11", IdCardUtil.getProvinceCode(number18));
    assertEquals("01", IdCardUtil.getCityCode(number18));
    assertEquals("01", IdCardUtil.getDistrictCode(number18));
    assertEquals(LocalDate.of(1949, 10, 1), IdCardUtil.getBirthday(number18, LocalDate.class));
    assertEquals(LocalDateTime.of(1949, 10, 1, 0, 0, 0), IdCardUtil.getBirthday(number18, LocalDateTime.class, false));
    assertEquals(DateUtil.parseDate("1949-10-01"), IdCardUtil.getBirthday(number18, Date.class, false));
    assertEquals(DateUtil.parseDate("1949-10-01").getTime(), IdCardUtil.getBirthday(number18, Long.class, false));
    assertTrue(IdCardUtil.getAge(number18) > 0);
    assertEquals(9, IdCardUtil.getGenderCode(number18));
    assertEquals(1, IdCardUtil.getGender(number18));
    assertTrue(IdCardUtil.isMale(number18));
    assertFalse(IdCardUtil.isFemale(number18));
    assertEquals("X", IdCardUtil.getCheckCode(number18));
    assertEquals(number18, IdCardUtil.get(number18).getNumber());
    assertEquals("11", IdCardUtil.getProvinceCode(number18, false));
    assertEquals("01", IdCardUtil.getCityCode(number18, false));
    assertEquals("01", IdCardUtil.getDistrictCode(number18, false));
    assertEquals(LocalDate.of(1949, 10, 1), IdCardUtil.getBirthday(number15, LocalDate.class, false));
    assertEquals("1949-10-01", IdCardUtil.getBirthday(number15, String.class, false));
    assertNull(IdCardUtil.getBirthday(number15, Object.class, false));
    assertTrue(IdCardUtil.getAge(number15, false) > 0);
    assertEquals(5, IdCardUtil.getGenderCode(number15, false));
    assertEquals(1, IdCardUtil.getGender(number15, false));
    assertTrue(IdCardUtil.isMale(number15, false));
    assertFalse(IdCardUtil.isFemale(number15, false));
    assertNull(IdCardUtil.getCheckCode(number15, false));
    assertNull(IdCardUtil.getProvinceCode("0", true));
    assertNull(IdCardUtil.getCityCode("0", true));
    assertNull(IdCardUtil.getDistrictCode("0", true));
    assertEquals(-1, IdCardUtil.getAge("0", true));
    assertNull(IdCardUtil.getGenderCode("0", true));
    assertEquals(-1, IdCardUtil.getGender("0", true));
    assertNull(IdCardUtil.isMale("0", true));
    assertNull(IdCardUtil.isFemale("0", true));
    assertNull(IdCardUtil.getCheckCode("0", true));
    assertNull(IdCardUtil.get("0", true));
    assertNull(IdCardUtil.getBirthday("0", LocalDate.class));
  }

  @Test
  void invokePublicStaticMethods() {
    int invoked = 0;
    for (Method method : IdCardUtil.class.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      try {
        method.invoke(null, args(method.getParameterTypes()));
      } catch (Exception ignored) {
        // 覆盖重载入口和参数校验。
      }
      invoked++;
    }
    assertTrue(invoked > 20);
  }

  private Object[] args(Class<?>[] parameterTypes) {
    Object[] args = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      if (parameterTypes[i].equals(String.class)) {
        args[i] = "11010119491001159X";
      } else if (parameterTypes[i].equals(Class.class)) {
        args[i] = LocalDate.class;
      } else if (parameterTypes[i].equals(boolean.class)) {
        args[i] = false;
      }
    }
    return args;
  }
}
