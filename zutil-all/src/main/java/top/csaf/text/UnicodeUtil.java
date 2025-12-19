package top.csaf.text;

/**
 * Unicode 工具类
 * <p>
 * 用于 String 字符串与 Unicode 编码之间的相互转换
 */
public class UnicodeUtil {

  /**
   * 字符串转 Unicode
   * <p>
   * 例如："你好" -> "\u4f60\u597d"
   *
   * @param str 待转换的字符串
   * @return Unicode 编码字符串，如果输入为 null 则返回 null
   */
  public static String toUnicode(String str) {
    if (str == null) {
      return null;
    }
    if (str.isEmpty()) {
      return "";
    }
    return encode(str, true);
  }

  /**
   * 字符串转 16 进制（Unicode 无前缀）
   * <p>
   * 例如："你好" -> "4f60597d"
   *
   * @param str 待转换的字符串
   * @return 16 进制字符串，如果输入为 null 则返回 null
   */
  public static String toHex(String str) {
    if (str == null) {
      return null;
    }
    if (str.isEmpty()) {
      return "";
    }
    return encode(str, false);
  }

  /**
   * 内部编码逻辑
   *
   * @param str        字符串
   * @param withPrefix 是否带 反斜杠u 前缀
   * @return 编码后的字符串
   */
  private static String encode(String str, boolean withPrefix) {
    // 预估容量：带前缀 * 6，不带前缀 * 4
    StringBuilder sb = new StringBuilder(str.length() * (withPrefix ? 6 : 4));
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (withPrefix) {
        sb.append("\\u");
      }
      String hex = Integer.toHexString(c);
      // 不足 4 位补 0
      if (hex.length() < 4) {
        sb.append("0000".substring(hex.length()));
      }
      sb.append(hex);
    }
    return sb.toString();
  }

  /**
   * Unicode 转字符串
   * <p>
   * 例如："\u4f60\u597d" -> "你好"<br>
   * 支持混合内容： "Hello\u4f60\u597d" -> "Hello你好"
   *
   * @param unicode Unicode 编码字符串
   * @return 原始字符串，如果输入为 null 则返回 null
   */
  public static String toString(String unicode) {
    // 宽容处理 null
    if (unicode == null) {
      return null;
    }
    if (unicode.isEmpty()) {
      return "";
    }

    int len = unicode.length();
    StringBuilder sb = new StringBuilder(len);
    // 遍历索引
    for (int i = 0; i < len; i++) {
      char c = unicode.charAt(i);
      // 检查是否以反斜杠u开头，且后面还有至少 4 个字符
      if (c == '\\' && i + 5 < len && unicode.charAt(i + 1) == 'u') {
        try {
          // 截取后 4 位 16 进制数
          String hex = unicode.substring(i + 2, i + 6);
          // 解析为字符
          char parsedChar = (char) Integer.parseInt(hex, 16);
          sb.append(parsedChar);
          // 跳过已处理的 5 个字符 (uXXXX，循环体 i++ 会处理 \)
          i += 5;
        } catch (NumberFormatException e) {
          // 如果解析失败（非 hex），则按原样追加
          sb.append(c);
        }
      } else {
        // 非 Unicode 序列，直接追加
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * 16 进制（Unicode 无前缀）转字符串
   * <p>
   * 例如："4f60597d" -> "你好"<br>
   * 注意：输入必须是纯 16 进制字符串，且长度必须是 4 的倍数
   *
   * @param hex 16 进制字符串
   * @return 原始字符串，如果输入为 null 则返回 null
   * @throws IllegalArgumentException 如果长度不是 4 的倍数或包含非法字符
   */
  public static String fromHex(String hex) {
    if (hex == null) {
      return null;
    }
    if (hex.isEmpty()) {
      return "";
    }
    if (hex.length() % 4 != 0) {
      throw new IllegalArgumentException("Length of hex string must be a multiple of 4");
    }

    StringBuilder sb = new StringBuilder(hex.length() / 4);
    for (int i = 0; i < hex.length(); i += 4) {
      String sub = hex.substring(i, i + 4);
      try {
        char parsedChar = (char) Integer.parseInt(sub, 16);
        sb.append(parsedChar);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid hex string: " + sub);
      }
    }
    return sb.toString();
  }
}
