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
   * @param isUpperCase 是否转大写
   * @return 加密后的字符串
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
    byte[] messageDigest = md.digest(in instanceof String ? ((String) in).getBytes(StandardCharsets.UTF_8) : (byte[]) in);
    StringBuilder hexString = new StringBuilder();

    for (byte b : messageDigest) {
      String hex = Integer.toHexString(0xff & b);
      // 保证生成的十六进制字符串长度为32位（每个字节转换后的十六进制是两位才行），需要在长度为1的十六进制字符串前补充0
      if (in instanceof byte[] && hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
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
