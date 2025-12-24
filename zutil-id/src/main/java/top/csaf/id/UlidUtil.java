package top.csaf.id;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

import java.time.Instant;

/**
 * ULID (Universally Unique Lexicographically Sortable Identifier) 工具类
 * <p>
 * 封装自 f4b6a3/ulid-creator，提供高性能、有序的唯一 ID 生成。
 * 该实现符合 Crockford's Base32 标准，支持纠错（I/L 自动识别为 1，O 识别为 0）。
 *
 * @author duanluan
 */
public class UlidUtil {

  /**
   * 生成标准的 ULID
   * <p>
   * 包含 48 位时间戳和 80 位随机数。
   * 在同一毫秒内生成的 ID 无法保证严格有序（取决于随机数），但整体呈时间递增趋势。
   *
   * @return 26位 ULID 字符串 (Crockford's Base32)
   */
  public static String nextUlid() {
    return UlidCreator.getUlid().toString();
  }

  /**
   * 生成单调递增的 ULID (Monotonic ULID)
   * <p>
   * 在同一毫秒内生成的多个 ID，其随机数部分会自动 +1，保证严格的字典序排列。
   * 适用于对数据库索引性能要求极高、或需要严格排序的场景。
   *
   * @return 26位 ULID 字符串
   */
  public static String nextMonotonicUlid() {
    return UlidCreator.getMonotonicUlid().toString();
  }

  /**
   * 校验 ULID 格式是否有效
   * <p>
   * 注意：本库支持 Crockford's Base32 容错，字符 'I', 'L' 会被视为 '1'，'O' 会被视为 '0'。
   *
   * @param ulid 待校验字符串
   * @return true 有效
   */
  public static boolean isValid(String ulid) {
    return Ulid.isValid(ulid);
  }

  /**
   * 解析 ULID 中的时间戳
   *
   * @param ulid ULID 字符串
   * @return 时间戳 (毫秒)
   * @throws IllegalArgumentException 如果 ULID 格式无效
   */
  public static long getTimestamp(String ulid) {
    return Ulid.getTime(ulid);
  }

  /**
   * 解析 ULID 中的 Instant 时间对象
   *
   * @param ulid ULID 字符串
   * @return Instant 对象
   * @throws IllegalArgumentException 如果 ULID 格式无效
   */
  public static Instant getInstant(String ulid) {
    return Ulid.getInstant(ulid);
  }
}
