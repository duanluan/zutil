package top.csaf.id;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UUID Version 7 工具类
 * <p>
 * 高性能、无锁、线程安全。
 * 基于 RFC 9562 标准实现，使用 Unix 毫秒时间戳作为高位，适合数据库主键（索引友好）。
 */
public class UuidV7Util {

  /**
   * 使用 ThreadLocal 保证每个线程有独立的生成器实例
   * 避免了多线程下的锁竞争 (False Sharing / Lock Contention)
   */
  private static final ThreadLocal<V7Generator> GENERATOR_HOLDER = ThreadLocal.withInitial(V7Generator::new);

  /**
   * 获取一个 UUID v7 对象
   *
   * @return UUID v7
   */
  public static UUID randomUUID() {
    return GENERATOR_HOLDER.get().generate();
  }

  /**
   * 获取一个 UUID v7 字符串 (无横线，32位)
   * <p>
   * 性能优于 uuid.toString().replace("-", "")
   *
   * @return 32位 Hex 字符串
   */
  public static String simpleUUID() {
    return fastToString(randomUUID());
  }

  /**
   * 获取标准的 UUID v7 字符串 (带横线，36位)
   * <p>
   * 修复：原 toString() 方法与 Object.toString() 冲突，重命名为 randomString()
   *
   * @return 36位 标准 UUID 字符串
   */
  public static String randomString() {
    return randomUUID().toString();
  }

  // ================= Inner Logic =================

  /**
   * 内部生成器，每个线程独享一个实例
   */
  private static class V7Generator {
    private long lastTimestamp = -1L;
    private long sequence = 0L;
    // 12位序列号的最大值 (4095)
    private static final long MAX_SEQUENCE = 0xFFFL;

    public UUID generate() {
      long timestamp = System.currentTimeMillis();
      long seq;

      // 逻辑：同毫秒内递增序列，新毫秒重置序列
      // 因为是 ThreadLocal，这里不需要 synchronized
      if (timestamp > lastTimestamp) {
        lastTimestamp = timestamp;
        sequence = 0L;
      } else if (timestamp == lastTimestamp) {
        // 同一毫秒内
        if (sequence < MAX_SEQUENCE) {
          sequence++;
        } else {
          // 序列号溢出 (单线程一毫秒生成超过 4096 个)，借用下一毫秒的时间
          lastTimestamp++;
          sequence = 0L;
        }
        timestamp = lastTimestamp;
      } else {
        // 时钟回拨：timestamp < lastTimestamp
        // 策略：继续使用 lastTimestamp 并递增序列，直到系统时间追上
        if (sequence < MAX_SEQUENCE) {
          sequence++;
        } else {
          lastTimestamp++;
          sequence = 0L;
        }
        timestamp = lastTimestamp;
      }

      seq = sequence;

      // --- 构建 UUID 的 128 位 ---

      // 1. 高 64 位 (Most Significant Bits)
      // 0-47 位: Unix 时间戳 (48 bits)
      // 48-51 位: 版本号 (4 bits) -> 0111 (Version 7)
      // 52-63 位: 序列号 (12 bits)
      long msb = (timestamp << 16) | (0x7000L) | seq;

      // 2. 低 64 位 (Least Significant Bits)
      // 0-1 位: Variant (2 bits) -> 10 (IETF Variant)
      // 2-63 位: 随机数 (62 bits)
      long random = ThreadLocalRandom.current().nextLong();
      // 清空最高2位，然后设置 Variant 为 10 (0x8000...)
      long lsb = (random & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

      return new UUID(msb, lsb);
    }
  }

  /**
   * 快速将 UUID 转为 32位 Hex 字符串 (无横线)
   * 比 UUID.toString().replace("-", "") 快 4-5 倍
   */
  private static String fastToString(UUID uuid) {
    long msb = uuid.getMostSignificantBits();
    long lsb = uuid.getLeastSignificantBits();
    char[] buffer = new char[32];
    formatUnsignedLong(msb, buffer, 0);
    formatUnsignedLong(lsb, buffer, 16);
    return new String(buffer);
  }

  private static void formatUnsignedLong(long val, char[] buf, int offset) {
    int charPos = 16;
    int radix = 1 << 4;
    int mask = radix - 1;
    do {
      buf[offset + --charPos] = DIGITS[((int) val) & mask];
      val >>>= 4;
    } while (charPos > 0);
  }

  private static final char[] DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };
}
