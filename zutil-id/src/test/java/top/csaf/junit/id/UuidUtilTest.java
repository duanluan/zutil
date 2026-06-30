package top.csaf.junit.id;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.id.UuidUtil;
import top.csaf.id.UuidUtil.UuidType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("统一 UUID 工具类测试")
class UuidUtilTest {

  @DisplayName("生成：标准版本测试 (V1, V4, V6, V7)")
  @Test
  void testGeneration() throws Exception {
    // V7
    UUID v7 = UuidUtil.v7();
    assertEquals(7, v7.version());
    assertEquals(2, v7.variant());

    // V4
    assertEquals(4, UuidUtil.v4().version());

    // V1
    assertEquals(1, UuidUtil.v1().version());

    // V6
    assertEquals(6, UuidUtil.v6().version());

    // Name Based (V3/V5)
    String name = "test";
    assertEquals(3, UuidUtil.v3(name).version());
    assertEquals(5, UuidUtil.v5(name).version());
    assertThrows(NullPointerException.class, () -> UuidUtil.v3(null));
    assertThrows(NullPointerException.class, () -> UuidUtil.v5(null));

    // Configurable
    assertEquals(7, UuidUtil.get(UuidType.V7).version());
    assertEquals(4, UuidUtil.get(UuidType.V4).version());
    assertEquals(1, UuidUtil.get(UuidType.V1).version());
    assertEquals(6, UuidUtil.get(UuidType.V6).version());
    assertEquals(2, UuidUtil.get(UuidType.V2).version());
    assertThrows(IllegalArgumentException.class, () -> UuidUtil.get(UuidType.V3));
    assertThrows(IllegalArgumentException.class, () -> UuidUtil.get(UuidType.V5));
    assertThrows(NullPointerException.class, () -> UuidUtil.get(null));
    for (UuidType type : UuidType.values()) {
      assertEquals(type, UuidType.valueOf(type.name()));
    }
    Class<?> switchMapClass = Class.forName("top.csaf.id.UuidUtil$1");
    Field switchMapField = switchMapClass.getDeclaredField("$SwitchMap$top$csaf$id$UuidUtil$UuidType");
    switchMapField.setAccessible(true);
    int[] switchMap = (int[]) switchMapField.get(null);
    int originalSwitchValue = switchMap[UuidType.V4.ordinal()];
    try {
      switchMap[UuidType.V4.ordinal()] = 0;
      assertEquals(7, UuidUtil.get(UuidType.V4).version());
    } finally {
      switchMap[UuidType.V4.ordinal()] = originalSwitchValue;
    }
  }

  @DisplayName("编解码：各种格式转换")
  @Test
  void testCodecs() {
    UUID original = UuidUtil.v7();
    log.info("Original: {}", original);

    // 1. Simple (32 Hex)
    String simple = UuidUtil.toSimple(original);
    log.info("Simple:   {}", simple);
    assertEquals(32, simple.length());
    assertFalse(simple.contains("-"));
    assertEquals(original, UuidUtil.parse(simple));

    // 2. Base62
    String base62 = UuidUtil.toBase62(original);
    log.info("Base62:   {}", base62);
    assertTrue(base62.length() <= 22);
    assertEquals(original, UuidUtil.parseBase62(base62));

    // 3. Base64
    String base64 = UuidUtil.toBase64(original);
    log.info("Base64:   {}", base64);
    assertEquals(original, UuidUtil.parseBase64(base64));

    // 4. URN
    String urn = UuidUtil.toUrn(original);
    log.info("URN:      {}", urn);
    assertTrue(urn.startsWith("urn:uuid:"));

    // Null checks
    assertNull(UuidUtil.toSimple(null));
    assertNull(UuidUtil.toBase62(null));
    assertNull(UuidUtil.toBase64(null));
    assertNull(UuidUtil.toUrn(null));
  }

  @DisplayName("提取：时间戳解析")
  @Test
  void testExtraction() {
    long now = System.currentTimeMillis();
    UUID v7 = UuidUtil.v7();

    // 允许少许误差
    Instant instant = UuidUtil.getInstant(v7);
    long diff = Math.abs(instant.toEpochMilli() - now);
    assertTrue(diff < 100, "V7 UUID 时间戳应接近当前时间");

    // 测试非时间 UUID 抛错
    UUID v4 = UuidUtil.v4();
    assertThrows(IllegalArgumentException.class, () -> UuidUtil.getInstant(v4));
  }

  @DisplayName("校验：格式验证")
  @Test
  void testValidation() {
    assertTrue(UuidUtil.isValid(UUID.randomUUID().toString()));
    assertTrue(UuidUtil.isValid("018e6b121c2d741193d3123456789abc")); // 32位
    assertFalse(UuidUtil.isValid("invalid-uuid"));
    assertFalse(UuidUtil.isValid(null));
  }

  @DisplayName("辅助方法")
  @Test
  void testHelpers() {
    assertNotNull(UuidUtil.nextSimple());
    assertEquals(32, UuidUtil.nextSimple().length());
    assertEquals(7, UuidUtil.getVersion(UuidUtil.v7()));
    assertEquals(-1, UuidUtil.getVersion(null));
  }

  @DisplayName("私有构造覆盖")
  @Test
  void testPrivateConstructor() throws Exception {
    Constructor<UuidUtil> constructor = UuidUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }
}
