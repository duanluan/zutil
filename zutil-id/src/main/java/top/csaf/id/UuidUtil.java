package top.csaf.id;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.codec.UrnCodec;
import com.github.f4b6a3.uuid.codec.base.Base16Codec;
import com.github.f4b6a3.uuid.codec.base.Base62Codec;
import com.github.f4b6a3.uuid.codec.base.Base64UrlCodec;
import com.github.f4b6a3.uuid.enums.UuidNamespace;
import com.github.f4b6a3.uuid.util.UuidValidator;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

/**
 * 统一 UUID 工具类 (基于 f4b6a3/uuid-creator)
 * <p>
 * 提供全版本的 UUID 生成、多种格式编解码（Base62/Base64/Hex）、校验及时间戳解析功能。
 *
 * @author duanluan
 */
public class UuidUtil {

  /**
   * UUID 版本枚举，用于配置化生成
   */
  @Getter
  public enum UuidType {
    /** v1: 基于时间 (Gregorian) + MAC 地址 */
    V1,
    /** v2: DCE 安全 (极少使用) */
    V2,
    /** v3: 基于名称 (MD5) */
    V3,
    /** v4: 完全随机 (JDK 标准) */
    V4,
    /** v5: 基于名称 (SHA1) */
    V5,
    /** v6: 重排序时间 (Gregorian) + MAC 地址 (有序) */
    V6,
    /** v7: Unix Epoch 时间戳 + 随机数 (推荐，有序) */
    V7
  }

  // ==========================================
  // 1. 生成 (Generation)
  // ==========================================

  /**
   * [V7] 生成基于 Unix 时间戳的有序 UUID (推荐)
   * <p>
   * 适用于数据库主键，单调递增，性能极高。
   */
  public static UUID v7() {
    return UuidCreator.getTimeOrderedEpoch();
  }

  /**
   * [V4] 生成完全随机的 UUID
   */
  public static UUID v4() {
    return UuidCreator.getRandomBased();
  }

  /**
   * [V1] 生成基于时间 + MAC 地址的 UUID
   */
  public static UUID v1() {
    return UuidCreator.getTimeBased();
  }

  /**
   * [V6] 生成重排序时间 UUID (兼容 V1 但有序)
   */
  public static UUID v6() {
    return UuidCreator.getTimeOrdered();
  }

  /**
   * [V3] 基于名称 (MD5) - DNS 命名空间
   */
  public static UUID v3(@NonNull String name) {
    return UuidCreator.getNameBasedMd5(UuidNamespace.NAMESPACE_DNS, name);
  }

  /**
   * [V5] 基于名称 (SHA1) - DNS 命名空间 (推荐替代 V3)
   */
  public static UUID v5(@NonNull String name) {
    return UuidCreator.getNameBasedSha1(UuidNamespace.NAMESPACE_DNS, name);
  }

  /**
   * 配置化生成入口
   *
   * @param type 类型枚举
   * @return UUID
   */
  public static UUID get(UuidType type) {
    switch (type) {
      case V7: return v7();
      case V4: return v4();
      case V1: return v1();
      case V6: return v6();
      case V2: return UuidCreator.getDceSecurity(com.github.f4b6a3.uuid.enums.UuidLocalDomain.LOCAL_DOMAIN_PERSON, 0);
      case V3:
      case V5:
        throw new IllegalArgumentException("Type " + type + " requires a 'name' parameter.");
      default:
        return v7();
    }
  }

  // ==========================================
  // 2. 格式化/编码 (Formatting)
  // ==========================================

  /**
   * 转为 32 位不带横线的字符串 (Hex)
   * <p>
   * 例: "018e6b121c2d741193d3123456789abc"
   */
  public static String toSimple(UUID uuid) {
    return uuid == null ? null : Base16Codec.INSTANCE.encode(uuid);
  }

  /**
   * 转为 22 位 Base62 字符串 (短链友好)
   * <p>
   * 例: "05W6p3l447Q5L3237E4321"
   */
  public static String toBase62(UUID uuid) {
    return uuid == null ? null : Base62Codec.INSTANCE.encode(uuid);
  }

  /**
   * 转为 22 位 Base64 URL 安全字符串
   * <p>
   * 例: "AY5rEhwtdBGT0xI0VniavA"
   */
  public static String toBase64(UUID uuid) {
    return uuid == null ? null : Base64UrlCodec.INSTANCE.encode(uuid);
  }

  /**
   * 转为 URN 格式
   * <p>
   * 例: "urn:uuid:018e6b12-1c2d-7411-93d3-123456789abc"
   */
  public static String toUrn(UUID uuid) {
    return uuid == null ? null : UrnCodec.INSTANCE.encode(uuid);
  }

  /**
   * 快捷生成：获取一个 V7 版本的 32 位 Simple String
   */
  public static String nextSimple() {
    return toSimple(v7());
  }

  // ==========================================
  // 3. 解析/解码 (Parsing)
  // ==========================================

  /**
   * 解析标准字符串 (支持带横线 36位 和 不带横线 32位)
   */
  public static UUID parse(String uuid) {
    // 库自带的 decode 支持自动识别 32/36 位
    return com.github.f4b6a3.uuid.codec.StandardStringCodec.INSTANCE.decode(uuid);
  }

  /**
   * 解析 Base62 字符串
   */
  public static UUID parseBase62(String uuid) {
    return Base62Codec.INSTANCE.decode(uuid);
  }

  /**
   * 解析 Base64 字符串
   */
  public static UUID parseBase64(String uuid) {
    return Base64UrlCodec.INSTANCE.decode(uuid);
  }

  // ==========================================
  // 4. 校验与提取 (Validation & Extraction)
  // ==========================================

  /**
   * 校验字符串是否为有效的 UUID (支持 36位带横线 或 32位Hex)
   */
  public static boolean isValid(String uuid) {
    return UuidValidator.isValid(uuid);
  }

  /**
   * 从 UUID 中提取时间 (支持 V1, V6, V7)
   * <p>
   * 这是 V7 的核心特性，可以直接从主键反解出创建时间。
   *
   * @param uuid 时间相关的 UUID
   * @return Instant 时间对象
   * @throws IllegalArgumentException 如果 UUID 不是基于时间的版本
   */
  public static Instant getInstant(UUID uuid) {
    return com.github.f4b6a3.uuid.util.UuidUtil.getInstant(uuid);
  }

  /**
   * 获取 UUID 的版本号
   */
  public static int getVersion(UUID uuid) {
    return uuid == null ? -1 : uuid.version();
  }
}
