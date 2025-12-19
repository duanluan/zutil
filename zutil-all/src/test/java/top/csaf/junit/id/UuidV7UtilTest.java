package top.csaf.junit.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import top.csaf.id.UuidV7Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UUID v7 工具类测试")
class UuidV7UtilTest {

  private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
  private static final Pattern SIMPLE_UUID_REGEX = Pattern.compile("^[0-9a-f]{32}$");

  @Test
  @DisplayName("UUID v7 结构验证")
  void testStructure() {
    UUID uuid = UuidV7Util.randomUUID();

    // 验证版本号必须为 7
    assertEquals(7, uuid.version(), "UUID version should be 7");
    // 验证变体必须为 2 (IETF)
    assertEquals(2, uuid.variant(), "UUID variant should be 2 (IETF)");

    // 验证 timestamp 部分是否接近当前时间
    long timestamp = uuid.getMostSignificantBits() >>> 16;
    long current = System.currentTimeMillis();
    // 允许 5秒内的误差（考虑到执行时间）
    assertTrue(Math.abs(current - timestamp) < 5000, "UUID timestamp should be close to system time");
  }

  @Test
  @DisplayName("simpleUUID 格式验证 (32位无横线)")
  void testSimpleUUID() {
    String simpleUuid = UuidV7Util.simpleUUID();
    assertNotNull(simpleUuid);
    assertEquals(32, simpleUuid.length());
    assertTrue(SIMPLE_UUID_REGEX.matcher(simpleUuid).matches(), "Should match 32-char hex pattern");
    // 验证手动 hex 转换逻辑是否正确，是否包含版本标识 '7'
    // simple string index 12 corresponds to the version char
    assertEquals('7', simpleUuid.charAt(12));
  }

  @Test
  @DisplayName("randomString 格式验证 (36位标准格式)")
  void testRandomString() {
    String randomString = UuidV7Util.randomString();
    assertNotNull(randomString);
    assertEquals(36, randomString.length());
    assertTrue(UUID_REGEX.matcher(randomString).matches(), "Should match standard UUID pattern");
  }

  @Test
  @DisplayName("唯一性测试 (单线程)")
  void testUniqueness() {
    int count = 100_000;
    Set<UUID> uuids = new java.util.HashSet<>(count);
    for (int i = 0; i < count; i++) {
      uuids.add(UuidV7Util.randomUUID());
    }
    assertEquals(count, uuids.size(), "Should generate unique UUIDs");
  }

  @Test
  @DisplayName("单调性测试 (同一线程内)")
  void testMonotonicity() {
    // 生成一组 UUID
    int count = 10_000;
    List<UUID> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      list.add(UuidV7Util.randomUUID());
    }

    // 验证列表是否已按时间顺序排序
    // UUID v7 的设计使得其字节序（lexicographical）与生成时间一致
    for (int i = 0; i < count - 1; i++) {
      UUID prev = list.get(i);
      UUID next = list.get(i + 1);
      // next 应该大于或等于 prev (考虑到 System.currentTimeMillis() 精度和序列号)
      assertTrue(prev.compareTo(next) < 0, "UUIDs should be monotonic");
    }
  }

  @Test
  @DisplayName("并发安全性测试")
  void testConcurrency() {
    int threads = 20;
    int perThread = 5000;
    Set<UUID> globalSet = ConcurrentHashMap.newKeySet();

    // 并行生成
    IntStream.range(0, threads).parallel().forEach(i -> {
      for (int j = 0; j < perThread; j++) {
        globalSet.add(UuidV7Util.randomUUID());
      }
    });

    assertEquals(threads * perThread, globalSet.size(), "Concurrent generation should produce unique IDs");
  }

  @RepeatedTest(5)
  @DisplayName("高频生成覆盖率测试 (触发序列号递增)")
  void testHighFrequency() {
    // 快速生成以尝试命中 "同一毫秒内 sequence++" 的分支
    UUID u1 = UuidV7Util.randomUUID();
    UUID u2 = UuidV7Util.randomUUID();

    // 如果恰好在同一毫秒
    if (u1.getMostSignificantBits() >>> 16 == u2.getMostSignificantBits() >>> 16) {
      // 验证 u2 确实在 u1 之后
      assertTrue(u1.compareTo(u2) < 0);
    }
  }

  @Test
  @DisplayName("FastToString 逻辑验证")
  void testFastToStringLogic() {
    // 对比 JDK 原生转换与 fastToString 的结果
    for (int i = 0; i < 1000; i++) {
      UUID uuid = UuidV7Util.randomUUID();
      String fast = UuidV7Util.simpleUUID(); // 内部调用了 fastToString(randomUUID())，这里只能间接验证

      // 我们手动获取一个 UUID，然后对比逻辑
      UUID u = UUID.randomUUID(); // 使用 v4 也可以测试转换逻辑，但我们需要测试 UuidV7Util 内部私有方法
      // 由于 fastToString 是私有的，我们只能通过 simpleUUID 验证结果正确性
      // 这里主要验证 simpleUUID() 返回的字符串还原回 UUID 后是否与原义一致（忽略格式，只看值，但 simpleUUID 是拿 randomUUID 生成的，无法获取中间态）

      // 替代方案：验证 simpleUUID 生成的字符串是否合法且能被解析
      String s = UuidV7Util.simpleUUID();
      // 补全横线以解析
      String standardFmt = s.replaceFirst(
        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
        "$1-$2-$3-$4-$5"
      );
      UUID parsed = UUID.fromString(standardFmt);
      assertEquals(7, parsed.version());
    }
  }
}
