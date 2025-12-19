package top.csaf.junit.id;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.id.SnowFlake;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SnowFlake 单元测试 (终极版)
 */
@Slf4j
@DisplayName("SnowFlake 雪花算法测试")
class SnowFlakeTest {

  // ==========================================
  // Part 1: 基础功能测试 (Basic Functionality)
  // ==========================================

  @DisplayName("构造函数参数验证")
  @Test
  void testConstructor() {
    // 1. 正常构造
    assertDoesNotThrow(() -> new SnowFlake(0, 0));
    assertDoesNotThrow(() -> new SnowFlake(31, 31));

    // 2. 自定义起始时间
    assertDoesNotThrow(() -> new SnowFlake(1, 1, System.currentTimeMillis() - 1000));

    // 3. 异常参数校验 (ID 越界)
    assertThrows(IllegalArgumentException.class, () -> new SnowFlake(32, 0));
    assertThrows(IllegalArgumentException.class, () -> new SnowFlake(-1, 0));
    assertThrows(IllegalArgumentException.class, () -> new SnowFlake(0, 32));
    assertThrows(IllegalArgumentException.class, () -> new SnowFlake(0, -1));

    // 4. 异常参数校验 (时间在未来)
    assertThrows(IllegalArgumentException.class, () -> new SnowFlake(1, 1, System.currentTimeMillis() + 1000000));
  }

  @DisplayName("基本 ID 生成与唯一性")
  @Test
  void testGenerateAndUniqueness() {
    SnowFlake snowFlake = new SnowFlake(1, 1);
    int count = 10_000;
    Set<Long> ids = new HashSet<>(count);
    for (int i = 0; i < count; i++) {
      ids.add(snowFlake.next());
    }
    assertEquals(count, ids.size(), "生成的 ID 应该唯一");
  }

  @DisplayName("单调递增性")
  @Test
  void testMonotonicity() {
    SnowFlake snowFlake = new SnowFlake(1, 1);
    long lastId = -1L;
    for (int i = 0; i < 1000; i++) {
      long currentId = snowFlake.next();
      if (lastId != -1L) {
        assertTrue(currentId > lastId, "新 ID 应该大于旧 ID");
      }
      lastId = currentId;
    }
  }

  @DisplayName("自定义起始时间逻辑验证")
  @Test
  void testCustomEpoch() {
    long yesterday = System.currentTimeMillis() - 86400000L;
    SnowFlake snowFlake = new SnowFlake(1, 1, yesterday);

    long id = snowFlake.next();
    assertTrue(id > 0);

    // 反推时间戳：ID 右移 22 位得到 offset，加上 yesterday 应该接近当前时间
    long timestampDiff = id >> 22;
    long calculatedTime = yesterday + timestampDiff;
    long now = System.currentTimeMillis();

    // 允许 1000ms 的误差
    assertTrue(Math.abs(now - calculatedTime) < 1000, "生成的 ID 时间戳计算不正确");
  }

  // ==========================================
  // Part 2: 反射边界测试 (Reflection Edge Cases)
  // ==========================================

  @DisplayName("序列号溢出测试 (同毫秒并发)")
  @Test
  void testSequenceOverflow() throws Exception {
    SnowFlake snowFlake = new SnowFlake(1, 1);
    long maxSequence = ~(-1L << 12); // 4095

    Field sequenceField = SnowFlake.class.getDeclaredField("sequence");
    sequenceField.setAccessible(true);
    Field lastTimestampField = SnowFlake.class.getDeclaredField("lastTimestamp");
    lastTimestampField.setAccessible(true);

    // 模拟：序列号已满，且时间戳固定为当前时间
    sequenceField.set(snowFlake, maxSequence);
    lastTimestampField.set(snowFlake, System.currentTimeMillis());

    // 预期：内部循环等待下一毫秒，序列号重置
    long id = snowFlake.next();
    assertTrue(id > 0);
    assertEquals(0L, sequenceField.get(snowFlake), "序列号应重置为 0");
  }

  @DisplayName("时钟回拨 - 小幅回拨 (<=5ms) 自动恢复")
  @Test
  void testSmallClockRollback() throws Exception {
    SnowFlake snowFlake = new SnowFlake(1, 1);
    Field lastTimestampField = SnowFlake.class.getDeclaredField("lastTimestamp");
    lastTimestampField.setAccessible(true);

    // 模拟：上次生成时间是“未来 2ms”
    long futureTime = System.currentTimeMillis() + 2;
    lastTimestampField.set(snowFlake, futureTime);

    // 预期：内部 wait(4ms) 后，真实时间追上，成功生成
    long id = snowFlake.next();
    assertTrue(id > 0);
  }

  @DisplayName("时钟回拨 - 大幅回拨 (>5ms) 直接报错")
  @Test
  void testLargeClockRollback() throws Exception {
    SnowFlake snowFlake = new SnowFlake(1, 1);
    Field lastTimestampField = SnowFlake.class.getDeclaredField("lastTimestamp");
    lastTimestampField.setAccessible(true);

    // 模拟：上次生成时间是“未来 1000ms”
    long futureTime = System.currentTimeMillis() + 1000;
    lastTimestampField.set(snowFlake, futureTime);

    // 预期：不等待，直接抛异常
    RuntimeException ex = assertThrows(RuntimeException.class, snowFlake::next);
    assertTrue(ex.getMessage().contains("Clock moved backwards"));
  }

  // ==========================================
  // Part 3: Mock 覆盖测试 (Coverage Only)
  // ==========================================

  /**
   * 辅助 Mock 类：允许控制 timeGen() 的返回值
   */
  static class MockSnowFlake extends SnowFlake {
    long mockTime;

    public MockSnowFlake(long datacenterId, long machineId) {
      super(datacenterId, machineId);
      this.mockTime = System.currentTimeMillis();
    }

    @Override
    protected long timeGen() {
      // 核心修复：防止父类构造函数调用时，mockTime 尚未初始化（为 0）导致报错
      if (mockTime == 0) {
        return System.currentTimeMillis();
      }
      return mockTime;
    }
  }

  @DisplayName("Mock测试：小幅回拨后依然追赶失败 (覆盖 wait 后抛异常逻辑)")
  @Test
  void testClockRollbackRetryFail() throws Exception {
    // 使用 Mock 类，锁定时间
    MockSnowFlake snowFlake = new MockSnowFlake(1, 1);
    Field lastTimestampField = SnowFlake.class.getDeclaredField("lastTimestamp");
    lastTimestampField.setAccessible(true);

    long t0 = 1000L;
    snowFlake.mockTime = t0;
    // 设置 lastTimestamp 为 1002L (回拨 2ms，满足 <= 5ms 进入 wait)
    lastTimestampField.set(snowFlake, t0 + 2);

    // 调用 next()
    // 1. 进入 wait 分支
    // 2. 醒来后再次调用 timeGen()，mockTime 依然是 1000L (模拟时间静止或持续回拨)
    // 3. 触发 if (curr < last) throw exception
    RuntimeException ex = assertThrows(RuntimeException.class, snowFlake::next);
    assertEquals("Clock moved backwards. Refusing to generate id", ex.getMessage());
  }

  @DisplayName("Mock测试：等待期间线程被中断 (覆盖 catch InterruptedException)")
  @Test
  void testInterruptedDuringWait() throws Exception {
    MockSnowFlake snowFlake = new MockSnowFlake(1, 1);
    snowFlake.mockTime = 1000L; // 锁定时间

    Field lastTimestampField = SnowFlake.class.getDeclaredField("lastTimestamp");
    lastTimestampField.setAccessible(true);
    // 满足进入 wait 条件 (offset = 2)
    lastTimestampField.set(snowFlake, 1002L);

    // ⚡️ 核心修改：不需要开启新线程，直接在当前线程“预设”中断状态
    // 当 next() 内部执行到 wait() 时，会立即检查中断状态并抛出 InterruptedException
    Thread.currentThread().interrupt();

    try {
      // 验证是否抛出了 RuntimeException
      RuntimeException e = assertThrows(RuntimeException.class, snowFlake::next);
      // 验证该 RuntimeException 是否由 InterruptedException 引起
      assertTrue(e.getCause() instanceof InterruptedException, "应捕获中断异常并包装为 RuntimeException");
    } finally {
      // 🧹 清理：测试结束后清除当前线程的中断状态，避免影响后续测试
      // (虽然 assertThrows 捕获异常后通常状态已被消耗，但显式清理是个好习惯)
      Thread.interrupted();
    }
  }
}
