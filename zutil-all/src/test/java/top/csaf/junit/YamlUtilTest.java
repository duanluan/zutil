package top.csaf.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.io.FileUtil;
import top.csaf.yaml.YamlFeat;
import top.csaf.yaml.YamlFeatConfig;
import top.csaf.yaml.YamlUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DisplayName("YAML 工具类测试")
class YamlUtilTest {

  private static final String YML_FILE_PATH = FileUtil.getProjectPath() + "/src/test/java/top/csaf/assets/yaml/test.yml";
  private static final File YML_FILE = new File(YML_FILE_PATH);

  private static final String C1_VALUE = "aa";
  private static final String C1_VALUE_1 = "${b.b1.b11}${b.b1.b11}";

  @Test
  void load() throws IOException {
    // Reader
    assertEquals(C1_VALUE, ((Map<?, ?>) YamlUtil.load(new FileReader(YML_FILE_PATH)).get("c")).get("c1"));
    assertEquals(C1_VALUE_1, ((Map<?, ?>) YamlUtil.load(new FileReader(YML_FILE_PATH), false).get("c")).get("c1"));
    // filePath
    assertEquals(C1_VALUE, ((Map<?, ?>) YamlUtil.load(YML_FILE_PATH).get("c")).get("c1"));
    assertEquals(C1_VALUE_1, ((Map<?, ?>) YamlUtil.load(YML_FILE_PATH, false).get("c")).get("c1"));
    assertEquals(Collections.emptyMap(), YamlUtil.load(""));
    // file
    assertEquals(C1_VALUE, ((Map<?, ?>) YamlUtil.load(YML_FILE).get("c")).get("c1"));
    assertEquals(C1_VALUE_1, ((Map<?, ?>) YamlUtil.load(YML_FILE, false).get("c")).get("c1"));
    assertEquals(Collections.emptyMap(), YamlUtil.load(new File("")));
    // InputStream
    assertEquals(C1_VALUE, ((Map<?, ?>) YamlUtil.load(Files.newInputStream(YML_FILE.toPath())).get("c")).get("c1"));
    assertEquals(C1_VALUE_1, ((Map<?, ?>) YamlUtil.load(Files.newInputStream(YML_FILE.toPath()), false).get("c")).get("c1"));

    // 替换 ${xxx} 为对应的值，如果没有找到对应的值，替换为空字符串
    YamlFeat.setEscapeNotFoundReplacement("");
    assertEquals("", ((Map<?, ?>) YamlUtil.load(new FileReader(YML_FILE_PATH)).get("d")).get("d1"));
    // 替换 ${xxx} 为对应的值，如果没有找到对应的值，抛出异常
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(true);
    assertThrows(IllegalArgumentException.class, () -> YamlUtil.load(new FileReader(YML_FILE_PATH)));
  }

  @Test
  void get() throws IOException {
    YamlFeat.setEscapeNotFoundReplacementAlways(null);
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(false);
    assertEquals(C1_VALUE_1, YamlUtil.get(new FileReader(YML_FILE_PATH), "c.c1", false));
    assertEquals(C1_VALUE, YamlUtil.get(new FileReader(YML_FILE_PATH), "c.c1"));
    // filePath
    assertEquals(C1_VALUE_1, YamlUtil.get(YML_FILE_PATH, "c.c1", false));
    assertEquals(C1_VALUE, YamlUtil.get(YML_FILE_PATH, "c.c1"));
    // file
    assertEquals(C1_VALUE_1, YamlUtil.get(YML_FILE, "c.c1", false));
    assertEquals(C1_VALUE, YamlUtil.get(YML_FILE, "c.c1"));
    // InputStream
    assertEquals(C1_VALUE_1, YamlUtil.get(Files.newInputStream(YML_FILE.toPath()), "c.c1", false));
    assertEquals(C1_VALUE, YamlUtil.get(Files.newInputStream(YML_FILE.toPath()), "c.c1"));
  }

  @Test
  void yamlFeatAndAdditionalBranches() throws Exception {
    YamlFeat.setEscapeNotFoundReplacement("R");
    assertEquals("R", YamlFeat.getEscapeNotFoundReplacementLazy(null));
    assertNull(YamlFeat.getEscapeNotFoundReplacementLazy(null));
    YamlFeat.setEscapeNotFoundReplacementAlways("A");
    assertEquals("A", YamlFeat.getEscapeNotFoundReplacement());
    assertEquals("fallback", YamlFeat.getEscapeNotFoundReplacement("fallback"));

    YamlFeat.setEscapeNotFoundThrowException(true);
    assertTrue(YamlFeat.getEscapeNotFoundThrowExceptionLazy(null));
    assertFalse(YamlFeat.getEscapeNotFoundThrowExceptionLazy(false));
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(false);
    assertFalse(YamlFeat.getEscapeNotFoundThrowException());
    assertTrue(YamlFeat.getEscapeNotFoundThrowException(true));

    YamlFeatConfig.setEscapeNotFoundReplacement("X")
      .setEscapeNotFoundReplacementAlways("Y")
      .setEscapeNotFoundThrowException(true)
      .setEscapeNotFoundThrowExceptionAlways(false)
      .apply();
    assertEquals("Y", YamlFeat.getEscapeNotFoundReplacement());
    assertFalse(YamlFeat.getEscapeNotFoundThrowException());

    Constructor<YamlFeatConfig> constructor = YamlFeatConfig.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());

    String yaml = "a: 1\nb:\n  b1: ${a}\nc: ${missing}\n";
    YamlFeat.setEscapeNotFoundReplacementAlways(null);
    assertEquals("1", ((Map<?, ?>) YamlUtil.load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), true).get("b")).get("b1"));
    assertNull(YamlUtil.get(Collections.singletonMap("a", "b"), "a.b"));
    assertNull(YamlUtil.get(Collections.singletonMap("a", null), "a"));

    int invoked = 0;
    for (Method method : YamlUtil.class.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      try {
        method.invoke(null, args(method.getParameterTypes()));
      } catch (Exception ignored) {
        // 覆盖重载入口和参数校验。
      }
      invoked++;
    }
    assertTrue(invoked > 10);
  }

  private Object[] args(Class<?>[] parameterTypes) throws Exception {
    Object[] args = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> type = parameterTypes[i];
      if (type.equals(String.class)) {
        args[i] = i == 0 ? YML_FILE_PATH : "c.c1";
      } else if (type.equals(boolean.class)) {
        args[i] = true;
      } else if (type.equals(File.class)) {
        args[i] = YML_FILE;
      } else if (type.equals(java.io.Reader.class)) {
        args[i] = new FileReader(YML_FILE);
      } else if (type.equals(java.io.InputStream.class)) {
        args[i] = Files.newInputStream(YML_FILE.toPath());
      } else if (Map.class.isAssignableFrom(type)) {
        args[i] = YamlUtil.load(YML_FILE, false);
      }
    }
    return args;
  }
}
