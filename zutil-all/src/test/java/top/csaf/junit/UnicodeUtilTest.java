package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.text.UnicodeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unicode 工具类测试
 */
@DisplayName("Unicode 工具类测试")
class UnicodeUtilTest {

  @DisplayName("测试私有构造方法")
  @Test
  void testPrivateConstructor() {
    try {
      Constructor<UnicodeUtil> constructor = UnicodeUtil.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      // 调用私有构造函数，预期抛出 InvocationTargetException
      assertThrows(InvocationTargetException.class, constructor::newInstance);
    } catch (NoSuchMethodException e) {
      fail("Constructor not found");
    }
  }

  @DisplayName("字符串转 Unicode")
  @Test
  void toUnicode() {
    // 1. 正常全中文
    assertEquals("\\u4f60\\u597d", UnicodeUtil.toUnicode("你好"));
    // 2. 英文（测试补 0 逻辑，'a'是 61，需要补 00）
    assertEquals("\\u0061", UnicodeUtil.toUnicode("a"));
    // 3. 空字符串
    assertEquals("", UnicodeUtil.toUnicode(""));
    // 4. Null 检查 (由 @NonNull 触发)
    assertThrows(NullPointerException.class, () -> UnicodeUtil.toUnicode(null));
  }

  @DisplayName("Unicode 转字符串")
  @Test
  void testToString() {
    // 1. 正常全 Unicode
    assertEquals("你好", UnicodeUtil.toString("\\u4f60\\u597d"));
    // 2. 混合内容 (Hello + 你好)
    assertEquals("Hello你好", UnicodeUtil.toString("Hello\\u4f60\\u597d"));
    // 3. 空字符串
    assertEquals("", UnicodeUtil.toString(""));
    // 4. 看起来像 Unicode 但长度不足 (例如结尾只有 \\u4f) -> 测试 "i + 5 < len" 为 false
    assertEquals("\\u4f", UnicodeUtil.toString("\\u4f"));
    // 5. 长度足够，以 \ 开头，但不是 u (例如 \notUnicode) -> 测试 "unicode.charAt(i + 1) == 'u'" 为 false
    // 这一步是为了消除覆盖率报告中的黄色警告
    assertEquals("\\notUnicode", UnicodeUtil.toString("\\notUnicode"));
    // 6. 看起来像 Unicode 但不是有效的 Hex (触发 NumberFormatException catch 块)
    // \\uZZZZ 不是有效 hex
    assertEquals("\\uZZZZ", UnicodeUtil.toString("\\uZZZZ"));
    // 7. 只有反斜杠没有 u
    assertEquals("\\a", UnicodeUtil.toString("\\a"));
    // 8. Null 检查
    assertThrows(NullPointerException.class, () -> UnicodeUtil.toString(null));
  }
}
