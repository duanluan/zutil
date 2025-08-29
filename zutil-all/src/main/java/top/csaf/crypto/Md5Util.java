package top.csaf.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import top.csaf.charset.StandardCharsets;
import top.csaf.lang.StrUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * MD5 工具类
 */
public class Md5Util {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * MD5 加密
   *
   * @param in          输入，可以是 String 或 byte[]
   * @param isUpperCase 是否转大写，默认小写
   * @return 加密后的字符串（32 位十六进制格式）
   */
  private static String to(Object in, boolean isUpperCase) {
    if (StrUtil.isBlank(in)) {
      return "";
    }
    if (!(in instanceof String) && !(in instanceof byte[])) {
      throw new IllegalArgumentException("in must be String or byte[]");
    }

    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5", "BC");
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new RuntimeException(e);
    }
    // 输入数据（如果 in 是 String，则先转成 UTF-8 编码的字节数组），进行 MD5 运算，返回固定长度的 16 字节数组
    byte[] messageDigest = md.digest(in instanceof String ? ((String) in).getBytes(StandardCharsets.UTF_8) : (byte[]) in);
    // StringBuilder 预分配长度 32，因为 MD5 的结果固定是 16 字节，每个字节转成 2 位十六进制字符，总长度 32
    StringBuilder hexString = new StringBuilder(32);
    // 遍历每个字节（范围 -128 ~ 127）
    for (byte b : messageDigest) {
      // 把每个字节转成无符号的十六进制字符串（范围 00 ~ ff），%02x 表示：如果结果只有 1 位（例如 "a"），就在前面补 0（变成 "0a"）
      hexString.append(String.format("%02x", b));
    }
    // 是否转大写
    if (isUpperCase) {
      return hexString.toString().toUpperCase();
    } else {
      return hexString.toString();
    }
  }


  /**
   * MD5 加密后转大写
   *
   * @param in 需要加密的字符串
   * @return 加密后的字符串
   */
  public static String toUpperCase(String in) {
    return to(in, true);
  }

  /**
   * MD5 加密（小写）
   *
   * @param in 需要加密的字符串
   * @return 加密后的字符串
   */
  public static String toLowerCase(String in) {
    return to(in, false);
  }

  /**
   * MD5 加密后截取中间 16 位并转大写
   *
   * @param in 需要加密的字符串
   * @return 加密后的字符串
   */
  public static String toUpperCaseShort(String in) {
    return to(in, true).substring(8, 24);
  }

  /**
   * MD5 加密后截取中间 16 位（小写）
   *
   * @param in 需要加密的字符串
   * @return 加密后的字符串
   */
  public static String toLowerCaseShort(String in) {
    return to(in, false).substring(8, 24);
  }

  /**
   * MD5 加密后转大写
   *
   * @param in 需要加密的 byte 数组
   * @return 加密后的字符串
   */
  public static String toUpperCase(byte[] in) {
    return to(in, true);
  }

  /**
   * MD5 加密（小写）
   *
   * @param in 需要加密的 byte 数组
   * @return 加密后的字符串
   */
  public static String toLowerCase(byte[] in) {
    return to(in, false);
  }

  /**
   * MD5 加密后截取中间 16 位并转大写
   *
   * @param in 需要加密的 byte 数组
   * @return 加密后的字符串
   */
  public static String toUpperCaseShort(byte[] in) {
    return to(in, true).substring(8, 24);
  }

  /**
   * MD5 加密后截取中间 16 位（小写）
   *
   * @param in 需要加密的 byte 数组
   * @return 加密后的字符串
   */
  public static String toLowerCaseShort(byte[] in) {
    return to(in, false).substring(8, 24);
  }
}
