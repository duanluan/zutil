package top.csaf.junit.id;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.id.UlidUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("ULID 工具类测试")
class UlidUtilTest {

  @DisplayName("标准 ULID 生成测试")
  @Test
  void testNextUlid() {
    String ulid = UlidUtil.nextUlid();
    log.info("Generated ULID: {}", ulid);

    assertNotNull(ulid);
    assertEquals(26, ulid.length());
    // 验证是否有效
    assertTrue(UlidUtil.isValid(ulid));
  }

  @DisplayName("单调 ULID 生成测试")
  @Test
  void testNextMonotonicUlid() {
    // 连续生成两个，验证后者是否大于前者（字典序）
    String id1 = UlidUtil.nextMonotonicUlid();
    String id2 = UlidUtil.nextMonotonicUlid();

    log.info("Monotonic 1: {}", id1);
    log.info("Monotonic 2: {}", id2);

    assertTrue(id2.compareTo(id1) > 0, "后生成的单调 ID 应在字典序上大于前者");
  }

  @DisplayName("唯一性测试 (批量)")
  @Test
  void testUniqueness() {
    int count = 10000;
    Set<String> set = new HashSet<>(count);
    for (int i = 0; i < count; i++) {
      // 混合测试标准和单调 ID
      set.add(UlidUtil.nextUlid());
      set.add(UlidUtil.nextMonotonicUlid());
    }
    // 此时生成了 2 * count 个 ID
    assertEquals(count * 2, set.size(), "生成的 ULID 应该唯一");
  }

  @DisplayName("校验方法 isValid 测试")
  @Test
  void testIsValid() {
    // 1. 有效场景
    assertTrue(UlidUtil.isValid(UlidUtil.nextUlid()));
    assertTrue(UlidUtil.isValid("01AN4Z07BY79KA1307SR9X4MV3")); // 标准示例

    // 2. 容错场景 (该库支持 Crockford Base32 纠错)
    // 根据 Ulid.java 源码：ALPHABET_VALUES[76] = 1 ('L')
    String base = "01AN4Z07BY79KA1307SR9X4MV";
    assertTrue(UlidUtil.isValid(base + "L"), "该库支持容错，L 被视为 1");
    assertTrue(UlidUtil.isValid(base + "I"), "该库支持容错，I 被视为 1");
    assertTrue(UlidUtil.isValid(base + "O"), "该库支持容错，O 被视为 0");
    assertTrue(UlidUtil.isValid(base + "o"), "该库支持容错，小写 o 被视为 0");

    // 3. 无效场景
    assertFalse(UlidUtil.isValid(null));
    assertFalse(UlidUtil.isValid(""));
    assertFalse(UlidUtil.isValid("123")); // 长度不够
    assertFalse(UlidUtil.isValid("01AN4Z07BY79KA1307SR9X4MV31")); // 太长 (>26)

    // 真正非法的字符 (不在 Base32 表中且无映射)
    assertFalse(UlidUtil.isValid(base + "#"));
    assertFalse(UlidUtil.isValid(base + "-"));
  }

  @DisplayName("时间戳解析测试")
  @Test
  void testGetTimestampAndInstant() throws InterruptedException {
    long start = System.currentTimeMillis();
    String ulid = UlidUtil.nextUlid();
    long end = System.currentTimeMillis();

    // 1. 测试 long 类型时间戳
    long extractedTime = UlidUtil.getTimestamp(ulid);
    // 允许极小误差
    assertTrue(extractedTime >= start && extractedTime <= end + 100,
      "提取的时间戳应接近生成时间");

    // 2. 测试 Instant 类型对象
    Instant instant = UlidUtil.getInstant(ulid);
    assertEquals(extractedTime, instant.toEpochMilli());

    // 3. 测试解析异常
    assertThrows(IllegalArgumentException.class, () -> UlidUtil.getTimestamp("INVALID_LENGTH"));
    // 长度对但字符非法
    assertThrows(IllegalArgumentException.class, () -> UlidUtil.getTimestamp("01AN4Z07BY79KA1307SR9X4MV#"));
  }

  @DisplayName("私有构造函数覆盖")
  @Test
  void testPrivateConstructor() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Constructor<UlidUtil> constructor = UlidUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }
}
