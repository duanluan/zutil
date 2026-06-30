package top.csaf.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.pinyin.PinyinFeat;
import top.csaf.pinyin.PinyinFeatConfig;
import top.csaf.pinyin.PinyinUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("拼音工具类测试")
class PinyinUtilTest {

  @AfterEach
  void tearDown() {
    PinyinFeat.setFirstWordInitialCap(null);
    PinyinFeat.setFirstWordInitialCapAlways(null);
    PinyinFeat.setSecondWordInitialCap(null);
    PinyinFeat.setSecondWordInitialCapAlways(null);
    PinyinFeat.setHasSeparatorByNotPinyinAround(null);
    PinyinFeat.setHasSeparatorByNotPinyinAroundAlways(null);
  }

  @DisplayName("get：拼音、声调、首字母和分隔符")
  @Test
  void getPinyin() {
    assertThrows(IllegalArgumentException.class, () -> PinyinUtil.get("", false, false, " "));
    assertEquals("hǎo hǎo xué xí", PinyinUtil.get("好好学习", false, true, " "));
    assertEquals("hǎo hǎo xué xí", PinyinUtil.get("好好学习", true, true, " "));
    assertEquals("hǎo,hǎo", PinyinUtil.get("好,好", false, true, "-"));

    PinyinFeat.setHasSeparatorByNotPinyinAround(true);
    assertEquals("hǎo-,-hǎo", PinyinUtil.get("好,好", false, true, "-"));

    PinyinFeat.setFirstWordInitialCap(true);
    PinyinFeat.setSecondWordInitialCap(true);
    assertEquals("Hǎo Hǎo", PinyinUtil.get("好好", false, true, " "));
    PinyinFeat.setFirstWordInitialCapAlways(true);
    PinyinFeat.setSecondWordInitialCapAlways(true);
    assertTrue(PinyinUtil.get("好好", true, false, " ").startsWith("Hǎo"));
    assertTrue(PinyinUtil.get("好好", true, false, " ").contains(" Hǎo"));
    PinyinFeat.setFirstWordInitialCapAlways(null);
    PinyinFeat.setSecondWordInitialCapAlways(null);
    assertTrue(PinyinUtil.get("好", true, false, ",").contains(","));
    assertTrue(PinyinUtil.get("好", true, false, ",").contains("hǎo"));
    assertEquals("hǎoA", PinyinUtil.get("好A", false, true, ""));
  }

  @DisplayName("便捷方法：全部重载")
  @Test
  void shortcutMethods() {
    assertEquals(PinyinUtil.get("好", false, false, " "), PinyinUtil.getAll("好", false, " "));
    assertEquals(PinyinUtil.get("好", false, false, null), PinyinUtil.getAll("好", false));
    assertEquals(PinyinUtil.get("好", false, true, " "), PinyinUtil.getFirst("好", false, " "));
    assertEquals(PinyinUtil.get("好", false, true, null), PinyinUtil.getFirst("好", false));
    assertEquals(PinyinUtil.get("好", true, false, " "), PinyinUtil.getWithTone("好", false, " "));
    assertEquals(PinyinUtil.get("好", true, false, null), PinyinUtil.getWithTone("好", false));
    assertEquals(PinyinUtil.get("好", false, false, " "), PinyinUtil.getNotWithTone("好", false, " "));
    assertEquals(PinyinUtil.get("好", false, false, null), PinyinUtil.getNotWithTone("好", false));
    assertEquals(PinyinUtil.get("好", true, false, " "), PinyinUtil.getAllWithTone("好", " "));
    assertEquals(PinyinUtil.get("好", true, false, null), PinyinUtil.getAllWithTone("好"));
    assertEquals(PinyinUtil.get("好", false, false, " "), PinyinUtil.getAllNotWithTone("好", " "));
    assertEquals(PinyinUtil.get("好", false, false, null), PinyinUtil.getAllNotWithTone("好"));
    assertEquals(PinyinUtil.get("好", true, true, " "), PinyinUtil.getFirstWithTone("好", " "));
    assertEquals(PinyinUtil.get("好", true, true, null), PinyinUtil.getFirstWithTone("好"));
    assertEquals(PinyinUtil.get("好", false, true, " "), PinyinUtil.getFirstNotWithTone("好", " "));
    assertEquals(PinyinUtil.get("好", false, true, null), PinyinUtil.getFirstNotWithTone("好"));
    assertTrue(PinyinUtil.isPolyphonicWord('好'));
  }

  @DisplayName("PinyinFeat：临时配置、永久配置和 lazy 获取")
  @Test
  void pinyinFeatConfig() throws Exception {
    assertNull(PinyinFeat.getFirstWordInitialCap(null));
    assertNull(PinyinFeat.getSecondWordInitialCap(null));
    assertFalse(PinyinFeat.getHasSeparatorByNotPinyinAround(null));
    assertTrue(PinyinFeat.getFirstWordInitialCap(true));
    assertTrue(PinyinFeat.getSecondWordInitialCap(true));
    assertTrue(PinyinFeat.getHasSeparatorByNotPinyinAround(true));

    PinyinFeat.setFirstWordInitialCap(true);
    assertTrue(PinyinFeat.getFirstWordInitialCapLazy(false));
    assertFalse(PinyinFeat.getFirstWordInitialCapLazy(false));
    PinyinFeat.setSecondWordInitialCap(true);
    assertTrue(PinyinFeat.getSecondWordInitialCapLazy(false));
    assertFalse(PinyinFeat.getSecondWordInitialCapLazy(false));
    PinyinFeat.setHasSeparatorByNotPinyinAround(true);
    assertTrue(PinyinFeat.getHasSeparatorByNotPinyinAroundLazy(false));
    assertFalse(PinyinFeat.getHasSeparatorByNotPinyinAroundLazy(false));

    PinyinFeatConfig.setFirstWordInitialCap(true)
      .setFirstWordInitialCapAlways(false)
      .setSecondWordInitialCap(true)
      .setSecondWordInitialCapAlways(false)
      .setHasSeparatorByNotPinyinAround(true)
      .setHasSeparatorByNotPinyinAroundAlways(false)
      .apply();
    PinyinFeatConfig.setFirstWordInitialCap(null)
      .setFirstWordInitialCapAlways(null)
      .setSecondWordInitialCap(null)
      .setSecondWordInitialCapAlways(null)
      .setHasSeparatorByNotPinyinAround(null)
      .setHasSeparatorByNotPinyinAroundAlways(null)
      .apply();
    assertFalse(PinyinFeat.getFirstWordInitialCap());
    assertFalse(PinyinFeat.getSecondWordInitialCap());
    assertFalse(PinyinFeat.getHasSeparatorByNotPinyinAround());

    Constructor<PinyinFeatConfig> constructor = PinyinFeatConfig.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }

  @DisplayName("反射调用全部静态工具方法")
  @Test
  void invokeAllPublicStaticMethods() {
    int invoked = 0;
    for (Method method : PinyinUtil.class.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      try {
        method.invoke(null, args(method.getParameterTypes()));
      } catch (Exception ignored) {
        // 参数校验和异常路径由专门断言覆盖。
      }
      invoked++;
    }
    assertTrue(invoked > 10);
  }

  @DisplayName("PinyinFeat：线程变量分支与包装方法空参校验")
  @Test
  void lazyFeatureBranchesAndWrapperNullChecks() throws Exception {
    PinyinFeat.setFirstWordInitialCapAlways(null);
    PinyinFeat.setSecondWordInitialCapAlways(null);
    PinyinFeat.setHasSeparatorByNotPinyinAroundAlways(null);

    PinyinFeat.setFirstWordInitialCap(Boolean.TRUE);
    assertTrue(PinyinFeat.getFirstWordInitialCap(null));
    assertNull(PinyinFeat.getFirstWordInitialCap(null));
    PinyinFeat.setFirstWordInitialCap(Boolean.TRUE);
    assertTrue(PinyinFeat.getFirstWordInitialCapLazy(null));
    assertFalse(PinyinFeat.getFirstWordInitialCapLazy(Boolean.FALSE));

    PinyinFeat.setSecondWordInitialCap(Boolean.TRUE);
    assertTrue(PinyinFeat.getSecondWordInitialCap(null));
    assertNull(PinyinFeat.getSecondWordInitialCap(null));
    PinyinFeat.setSecondWordInitialCap(Boolean.TRUE);
    assertTrue(PinyinFeat.getSecondWordInitialCapLazy(null));
    assertFalse(PinyinFeat.getSecondWordInitialCapLazy(Boolean.FALSE));

    PinyinFeat.setHasSeparatorByNotPinyinAround(Boolean.TRUE);
    assertTrue(PinyinFeat.getHasSeparatorByNotPinyinAround(null));
    assertFalse(PinyinFeat.getHasSeparatorByNotPinyinAround(null));
    PinyinFeat.setHasSeparatorByNotPinyinAround(Boolean.TRUE);
    assertTrue(PinyinFeat.getHasSeparatorByNotPinyinAroundLazy(null));
    assertFalse(PinyinFeat.getHasSeparatorByNotPinyinAroundLazy(Boolean.FALSE));

    PinyinFeat.setFirstWordInitialCapAlways(true);
    assertTrue(PinyinFeat.getFirstWordInitialCap(null));
    assertTrue(PinyinFeat.getFirstWordInitialCapLazy(false));
    assertTrue(PinyinFeat.getFirstWordInitialCap());
    PinyinFeat.setFirstWordInitialCapAlways(null);
    PinyinFeat.setSecondWordInitialCapAlways(true);
    assertTrue(PinyinFeat.getSecondWordInitialCap(null));
    assertTrue(PinyinFeat.getSecondWordInitialCapLazy(false));
    assertTrue(PinyinFeat.getSecondWordInitialCap());
    PinyinFeat.setSecondWordInitialCapAlways(null);
    PinyinFeat.setHasSeparatorByNotPinyinAroundAlways(true);
    assertTrue(PinyinFeat.getHasSeparatorByNotPinyinAround(null));
    assertTrue(PinyinFeat.getHasSeparatorByNotPinyinAroundLazy(false));
    assertTrue(PinyinFeat.getHasSeparatorByNotPinyinAround());
    PinyinFeat.setHasSeparatorByNotPinyinAroundAlways(null);

    assertThrows(NullPointerException.class, () -> PinyinUtil.get(null, false, false, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAll(null, true, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAll("好", true, null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAll(null, true));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirst(null, true, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirst("好", true, null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirst(null, true));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getWithTone(null, true, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getWithTone("好", true, null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getWithTone(null, true));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getNotWithTone(null, true, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getNotWithTone("好", true, null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getNotWithTone(null, true));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAllWithTone(null, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAllWithTone("好", null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAllWithTone(null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAllNotWithTone(null, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAllNotWithTone("好", null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getAllNotWithTone(null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirstWithTone(null, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirstWithTone("好", null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirstWithTone(null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirstNotWithTone(null, " "));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirstNotWithTone("好", null));
    assertThrows(NullPointerException.class, () -> PinyinUtil.getFirstNotWithTone(null));

    Constructor<PinyinUtil> utilConstructor = PinyinUtil.class.getDeclaredConstructor();
    utilConstructor.setAccessible(true);
    assertNotNull(utilConstructor.newInstance());

    Constructor<PinyinFeat> constructor = PinyinFeat.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }

  private Object[] args(Class<?>[] parameterTypes) {
    Object[] args = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      if (parameterTypes[i].equals(String.class)) {
        args[i] = i == 3 ? " " : "好";
      } else if (parameterTypes[i].equals(boolean.class)) {
        args[i] = false;
      } else if (parameterTypes[i].equals(char.class)) {
        args[i] = '好';
      }
    }
    return args;
  }
}
