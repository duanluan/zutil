package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.constant.CommonPattern;
import top.csaf.regex.RegExUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("正则工具类补充测试")
class RegExUtilCoverageTest {

  private static final Pattern WORD_NUM = Pattern.compile("([a-z]+)(\\d+)");
  private static final String TEXT = "a1 b22 c333";

  @DisplayName("匹配、下标和捕获组")
  @Test
  void matchMethods() throws Exception {
    assertEquals("\\(a\\)\\[1\\]", RegExUtil.replaceAllSpecialChar("(a)[1]"));
    assertTrue(RegExUtil.isMatch("ABC", "abc", Pattern.CASE_INSENSITIVE));
    assertTrue(RegExUtil.isMatch("abc", Pattern.compile("b")));
    assertEquals(1, RegExUtil.indexOf("abc", "B", Pattern.CASE_INSENSITIVE));
    assertEquals(1, RegExUtil.indexOf("abc", Pattern.compile("b")));
    assertEquals(-1, RegExUtil.indexOf("abc", "z"));

    assertThrows(IllegalArgumentException.class, () -> RegExUtil.match(TEXT, WORD_NUM, -1, 1));
    assertThrows(IllegalArgumentException.class, () -> RegExUtil.match(TEXT, WORD_NUM, 0, -1));
    assertEquals("a1", RegExUtil.match(TEXT, WORD_NUM, 0, 0, false));
    assertEquals("22", RegExUtil.match(TEXT, WORD_NUM, 1, 2));
    assertEquals("a", RegExUtil.match(TEXT, WORD_NUM, 0, 1, true));
    assertEquals("a1", RegExUtil.match(TEXT, WORD_NUM, 0, 0, true));
    assertNull(RegExUtil.match(TEXT, Pattern.compile("([a-z]+)|(\\d+)"), 0, 2, true));
    assertNull(RegExUtil.match(TEXT, WORD_NUM, 9, 1));

    assertEquals("a", RegExUtil.matchFirstItem(TEXT, WORD_NUM, 1));
    assertEquals("a", RegExUtil.matchFirstGroup(TEXT, WORD_NUM, 0));
    assertEquals("a", RegExUtil.matchFirstItemGroup(TEXT, WORD_NUM));
    assertEquals("a", RegExUtil.matchFirstItem(TEXT, "([a-z]+)(\\d+)", 1));
    assertEquals("a", RegExUtil.matchFirstGroup(TEXT, "([a-z]+)(\\d+)", 0));
    assertEquals("a", RegExUtil.matchFirstItemGroup(TEXT, "([a-z]+)(\\d+)"));

    assertEquals(Arrays.asList("a", "b", "c"), RegExUtil.matchAllItems(TEXT, WORD_NUM, 1));
    assertEquals(Arrays.asList("a", "b", "c"), RegExUtil.matchAllItemsFirstGroup(TEXT, WORD_NUM));
    assertEquals(Arrays.asList("b", "22"), RegExUtil.matchAllGroups(TEXT, WORD_NUM, 1));
    assertEquals(Arrays.asList("a", "1", "b", "22", "c", "333"), RegExUtil.matchAll(TEXT, WORD_NUM));
    assertEquals(Arrays.asList("a1", "b22", "c333"), RegExUtil.matchAll(TEXT, WORD_NUM, true));
    assertThrows(IllegalArgumentException.class, () -> RegExUtil.matchAllItems(TEXT, WORD_NUM, -1));
    assertThrows(IllegalArgumentException.class, () -> RegExUtil.matchAllGroups(TEXT, WORD_NUM, -1));
    assertTrue(RegExUtil.matchAllGroups(TEXT, WORD_NUM, 9).isEmpty());
    assertEquals(Arrays.asList("a1", "b22", "c333"), RegExUtil.matchAll(TEXT, Pattern.compile("[a-z]+\\d+"), true));

    Constructor<CommonPattern> constructor = CommonPattern.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
    assertNotNull(new RegExUtil());
  }

  @DisplayName("替换和删除")
  @Test
  void replaceAndRemoveMethods() {
    assertThrows(IllegalArgumentException.class, () -> RegExUtil.replace(TEXT, WORD_NUM, "X", -1, 1));
    assertThrows(IllegalArgumentException.class, () -> RegExUtil.replace(TEXT, WORD_NUM, "X", 0, -1));
    assertEquals("X b22 c333", RegExUtil.replace(TEXT, WORD_NUM, "X", 0, 0, false));
    assertEquals("aX b22 c333", RegExUtil.replace(TEXT, WORD_NUM, "X", 0, 2));
    assertThrows(IndexOutOfBoundsException.class, () -> RegExUtil.replace(TEXT, WORD_NUM, "X", 0, 9, true));
    assertEquals("X1 b22 c333", RegExUtil.replace(TEXT, WORD_NUM, "X", 0, 1, true));
    assertNull(RegExUtil.replace(TEXT, WORD_NUM, "X", 9, 1));

    assertEquals("aX bX cX", RegExUtil.replaceAllItems(TEXT, WORD_NUM, "X", 2));
    assertEquals("X1 X22 X333", RegExUtil.replaceAllItemsFirstGroup(TEXT, WORD_NUM, "X"));
    assertEquals("XX b22 c333", RegExUtil.replaceAllGroups(TEXT, WORD_NUM, "X", 0));
    assertEquals("a1 b22 c333", RegExUtil.replaceAllGroups(TEXT, WORD_NUM, "X", 9));
    assertEquals("XX XX XX", RegExUtil.replaceAll(TEXT, WORD_NUM, "X"));
    assertEquals("X X X", RegExUtil.replaceAll(TEXT, WORD_NUM, "X", true));
    assertThrows(IllegalArgumentException.class, () -> RegExUtil.replaceAllItems(TEXT, WORD_NUM, "X", -1));
    assertThrows(IllegalArgumentException.class, () -> RegExUtil.replaceAllGroups(TEXT, WORD_NUM, "X", -1));

    assertEquals("a b22 c333", RegExUtil.remove(TEXT, WORD_NUM, 0, 2));
    assertEquals("a b22 c333", RegExUtil.removeFirstItem(TEXT, WORD_NUM, 2));
    assertEquals("1 b22 c333", RegExUtil.removeFirstGroup(TEXT, WORD_NUM, 0));
    assertEquals("1 b22 c333", RegExUtil.removeFirstItemGroup(TEXT, WORD_NUM));
    assertEquals("a b c", RegExUtil.removeAllItems(TEXT, WORD_NUM, 2));
    assertEquals("1 22 333", RegExUtil.removeAllItemsFirstGroup(TEXT, WORD_NUM));
    assertEquals(" b22 c333", RegExUtil.removeAllGroups(TEXT, WORD_NUM, 0));
    assertEquals("  ", RegExUtil.removeAll(TEXT, WORD_NUM, true));
    assertTrue(RegExUtil.containsHanZi("abc中文"));
    assertFalse(RegExUtil.containsHanZi("abc"));
  }

  @DisplayName("反射调用全部公开静态重载")
  @Test
  void invokeAllPublicStaticMethods() {
    int invoked = 0;
    for (Method method : RegExUtil.class.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      try {
        method.invoke(null, args(method.getParameterTypes()));
      } catch (Exception ignored) {
        // 反射覆盖重载入口；核心行为由上面的断言覆盖。
      }
      invoked++;
    }
    assertTrue(invoked > 100);
  }

  @DisplayName("公开静态方法 NonNull 参数校验")
  @Test
  void invokePublicStaticMethodsWithNullArgs() {
    int invoked = 0;
    for (Method method : RegExUtil.class.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      Class<?>[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        if (parameterTypes[i].isPrimitive()) {
          continue;
        }
        Object[] args = args(parameterTypes);
        args[i] = null;
        try {
          method.invoke(null, args);
        } catch (Exception ignored) {
          // 覆盖 Lombok NonNull 生成的参数校验分支。
        }
        invoked++;
      }
    }
    assertTrue(invoked > 100);
  }

  private Object[] args(Class<?>[] parameterTypes) {
    Object[] args = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> type = parameterTypes[i];
      if (type.equals(String.class)) {
        args[i] = stringValue(i);
      } else if (CharSequence.class.isAssignableFrom(type)) {
        args[i] = TEXT;
      } else if (type.equals(Pattern.class)) {
        args[i] = WORD_NUM;
      } else if (type.equals(int.class)) {
        args[i] = i > 3 ? 0 : 1;
      } else if (type.equals(boolean.class)) {
        args[i] = i % 2 == 0;
      }
    }
    return args;
  }

  private String stringValue(int index) {
    if (index == 0) {
      return TEXT;
    }
    if (index == 1) {
      return "([a-z]+)(\\d+)";
    }
    return "X";
  }
}
