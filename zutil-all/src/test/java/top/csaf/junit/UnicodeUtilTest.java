package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.text.UnicodeUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unicode 工具类测试
 */
@DisplayName("Unicode 工具类测试")
class UnicodeUtilTest {

  @DisplayName("字符串转 Unicode")
  @Test
  void toUnicode() {
    // 1. 正常全中文
    assertEquals("\\u4f60\\u597d", UnicodeUtil.toUnicode("你好"));
    // 2. 英文（测试补 0 逻辑）
    assertEquals("\\u0061", UnicodeUtil.toUnicode("a"));
    // 3. 空字符串
    assertEquals("", UnicodeUtil.toUnicode(""));
    // 4. Null (宽容模式，应该返回 null)
    assertNull(UnicodeUtil.toUnicode(null));
  }

  @DisplayName("字符串转 16 进制 (toHex)")
  @Test
  void toHex() {
    // 1. 正常中文
    assertEquals("4f60597d", UnicodeUtil.toHex("你好"));
    // 2. 英文（补零）
    assertEquals("0061", UnicodeUtil.toHex("a"));
    // 3. 空串
    assertEquals("", UnicodeUtil.toHex(""));
    // 4. Null (宽容模式)
    assertNull(UnicodeUtil.toHex(null));
  }

  @DisplayName("Unicode 转字符串")
  @Test
  void testToString() {
    // 1. 正常全 Unicode
    assertEquals("你好", UnicodeUtil.toString("\\u4f60\\u597d"));
    // 2. 混合内容
    assertEquals("Hello你好", UnicodeUtil.toString("Hello\\u4f60\\u597d"));
    // 3. 空字符串
    assertEquals("", UnicodeUtil.toString(""));
    // 4. 边界和异常 Hex 测试
    assertEquals("\\u4f", UnicodeUtil.toString("\\u4f"));
    // 5. 长度足够，以 \ 开头，但不是 u (例如 \notUnicode) -> 测试 "unicode.charAt(i + 1) == 'u'" 为 false
    // 这一步是为了消除覆盖率报告中的黄色警告
    assertEquals("\\notUnicode", UnicodeUtil.toString("\\notUnicode"));
    // 6. 看起来像 Unicode 但不是有效的 Hex (触发 NumberFormatException catch 块)
    // \\uZZZZ 不是有效 hex
    assertEquals("\\uZZZZ", UnicodeUtil.toString("\\uZZZZ"));
    // 7. 只有反斜杠没有 u
    assertEquals("\\a", UnicodeUtil.toString("\\a"));
    // 8. Null (宽容模式)
    assertNull(UnicodeUtil.toString(null));
  }

  @DisplayName("16 进制转字符串 (fromHex)")
  @Test
  void fromHex() {
    // 1. 正常转换
    assertEquals("你好", UnicodeUtil.fromHex("4f60597d"));
    // 2. 英文转换
    assertEquals("a", UnicodeUtil.fromHex("0061"));
    // 3. 空串
    assertEquals("", UnicodeUtil.fromHex(""));
    // 4. 异常情况
    assertThrows(IllegalArgumentException.class, () -> UnicodeUtil.fromHex("4f605"));
    // 5. 包含非法字符 (ZZZZ)
    assertThrows(IllegalArgumentException.class, () -> UnicodeUtil.fromHex("ZZZZ"));
    // 6. Null (宽容模式)
    assertNull(UnicodeUtil.fromHex(null));
  }
}
