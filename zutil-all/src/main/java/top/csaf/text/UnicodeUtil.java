package top.csaf.text;

import lombok.NonNull;

/**
 * Unicode 工具类
 * <p>
 * 用于 String 字符串与 Unicode 编码之间的相互转换
 */
public class UnicodeUtil {

  /**
   * 构造方法私有化，防止实例化
   */
  private UnicodeUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * 字符串转 Unicode
   * <p>
   * 例如："你好" -> "\u4f60\u597d"
   *
   * @param str 待转换的字符串
   * @return Unicode 编码字符串
   */
  public static String toUnicode(@NonNull String str) {
    // 判空，如果由调用方保证非空，此处可依赖 @NonNull，但为了逻辑完整保留空串处理
    if (str.isEmpty()) {
      return "";
    }

    // 预估容量，通常 unicode 是原长度的 6 倍 (反斜杠u + 4位 hex)
    StringBuilder sb = new StringBuilder(str.length() * 6);
    // 遍历字符串中的每一个字符
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      // 添加前缀
      sb.append("\\u");
      // 转为 16 进制字符串
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
   * @return 原始字符串
   */
  public static String toString(@NonNull String unicode) {
    // 判空
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
}
