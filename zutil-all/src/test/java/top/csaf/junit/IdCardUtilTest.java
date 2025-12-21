package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.date.DateUtil;
import top.csaf.idcard.IdCard;
import top.csaf.idcard.IdCardUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
