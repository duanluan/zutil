package top.csaf.http.convert;

import cn.zhxu.data.Array;
import cn.zhxu.data.Mapper;
import cn.zhxu.data.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("自动 JSON 消息转换器测试")
class AutoJsonMsgConvertorTest {

  @DisplayName("序列化与反序列化")
  @Test
  void serializeAndDeserialize() {
    AutoJsonMsgConvertor convertor = new AutoJsonMsgConvertor();
    assertSame(AutoJsonMsgConvertor.class, convertor.getConvertor().getClass());
    assertEquals("application/json", convertor.mediaType());
    assertNull(convertor.toBean(Map.class, ""));
    assertNull(convertor.toBean(Map.class, (String) null));
    assertThrows(RuntimeException.class, () -> convertor.toMapper("bad-json"));
    assertThrows(RuntimeException.class, () -> convertor.toArray("bad-json"));

    Map<String, Object> map = new LinkedHashMap<>();
    map.put("name", "zutil");
    map.put("age", 2);
    String json = convertor.serialize(map, false);
    assertTrue(convertor.serialize(new AutoJsonMsgConvertor.UniversalMapper(map), true).contains("zutil"));
    assertTrue(convertor.serialize(new AutoJsonMsgConvertor.UniversalArray(new ArrayList<>(Arrays.asList(map))), true).contains("zutil"));
    assertTrue(json.contains("zutil"));
    assertArrayEquals(json.getBytes(StandardCharsets.UTF_8), convertor.serialize(map, StandardCharsets.UTF_8));
    assertArrayEquals(convertor.serialize(map, StandardCharsets.UTF_8, true), convertor.serialize(map, StandardCharsets.UTF_8, true));

    Mapper mapper = convertor.toMapper(new ByteArrayInputStream("{\"name\":\"zutil\",\"child\":{\"v\":1},\"arr\":[1,\"2\"]}".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    assertEquals("zutil", mapper.getString("name"));
    assertEquals(1, mapper.getMapper("child").getInt("v"));
    assertEquals("2", mapper.getArray("arr").getString(1));

    Array array = convertor.toArray(new ByteArrayInputStream("[{\"v\":1},[2],true,\"bad\"]".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    assertEquals(4, array.size());
    assertEquals(1, array.getMapper(0).getInt("v"));
    assertEquals(2, array.getArray(1).getInt(0));
    assertTrue(array.getBool(2));
    assertEquals(0, array.getInt(3));

    List<Map> list = convertor.toList(Map.class, "[{\"v\":1}]");
    assertEquals(1, list.get(0).get("v"));
  }

  @DisplayName("UniversalMapper")
  @Test
  void universalMapper() {
    Map<String, Object> nested = new LinkedHashMap<>();
    nested.put("value", 1);
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("nested", nested);
    map.put("array", Arrays.asList(1, "2"));
    map.put("bool", "true");
    map.put("boolFalse", false);
    map.put("int", "bad");
    map.put("intNumber", 1);
    map.put("long", 2L);
    map.put("longBad", "bad");
    map.put("float", "bad");
    map.put("floatNumber", 1F);
    map.put("double", 3D);
    map.put("doubleBad", "bad");
    AutoJsonMsgConvertor.UniversalMapper mapper = new AutoJsonMsgConvertor.UniversalMapper(map);

    assertEquals(map.size(), mapper.size());
    assertFalse(mapper.isEmpty());
    assertNotNull(mapper.getMapper("nested"));
    assertNull(mapper.getMapper("array"));
    assertNotNull(mapper.getArray("array"));
    assertNull(mapper.getArray("missing"));
    assertTrue(mapper.getBool("bool"));
    assertFalse(mapper.getBool("boolFalse"));
    assertEquals(0, mapper.getInt("int"));
    assertEquals(1, mapper.getInt("intNumber"));
    assertEquals(2L, mapper.getLong("long"));
    assertEquals(0L, mapper.getLong("longBad"));
    assertEquals(0F, mapper.getFloat("float"));
    assertEquals(1F, mapper.getFloat("floatNumber"));
    assertEquals(3D, mapper.getDouble("double"));
    assertEquals(0D, mapper.getDouble("doubleBad"));
    assertNull(mapper.getString("missing"));
    assertTrue(mapper.has("nested"));
    assertTrue(mapper.keySet().contains("nested"));
    assertEquals(map, mapper.toMap());
    assertTrue(mapper.toString().contains("nested"));
    assertTrue(mapper.toPretty().contains("nested"));
    assertNotNull(mapper.toBean(Map.class));
    assertNotNull(mapper.toBean((Type) Map.class));
    assertNotNull(mapper.toBean(new TypeRef<Map<String, Object>>() {}));
  }

  @DisplayName("UniversalArray")
  @Test
  void universalArray() {
    Map<String, Object> nested = new LinkedHashMap<>();
    nested.put("value", 1);
    List<Object> list = new ArrayList<>(Arrays.asList(nested, Arrays.asList(1), "true", "bad", 2L, "bad", 3D, null, false, 1, "bad", 1F, "bad"));
    AutoJsonMsgConvertor.UniversalArray array = new AutoJsonMsgConvertor.UniversalArray(list);

    assertEquals(list.size(), array.size());
    assertFalse(array.isEmpty());
    assertNotNull(array.getMapper(0));
    assertNull(array.getMapper(1));
    assertNotNull(array.getArray(1));
    assertNull(array.getArray(0));
    assertTrue(array.getBool(2));
    assertFalse(array.getBool(8));
    assertEquals(0, array.getInt(3));
    assertEquals(1, array.getInt(9));
    assertEquals(2L, array.getLong(4));
    assertEquals(0L, array.getLong(10));
    assertEquals(0F, array.getFloat(5));
    assertEquals(1F, array.getFloat(11));
    assertEquals(3D, array.getDouble(6));
    assertEquals(0D, array.getDouble(12));
    assertNull(array.getString(7));
    assertEquals(list, array.toList());
    assertNotNull(array.toList(Object.class));
    assertTrue(array.toString().contains("true"));
    assertTrue(array.toPretty().contains("true"));
  }

  @DisplayName("空反序列化和数值转换分支")
  @Test
  void nullDeserializeAndNumericParseBranches() {
    AutoJsonMsgConvertor convertor = new AutoJsonMsgConvertor();
    assertTrue(convertor.toMapper("").isEmpty());
    assertTrue(convertor.toArray("").isEmpty());

    Map<String, Object> map = new LinkedHashMap<>();
    map.put("int", "12");
    map.put("long", "13");
    map.put("float", "1.5");
    map.put("double", "2.5");
    AutoJsonMsgConvertor.UniversalMapper mapper = new AutoJsonMsgConvertor.UniversalMapper(map);
    assertEquals(12, mapper.getInt("int"));
    assertEquals(13L, mapper.getLong("long"));
    assertEquals(1.5F, mapper.getFloat("float"));
    assertEquals(2.5D, mapper.getDouble("double"));

    AutoJsonMsgConvertor.UniversalArray array = new AutoJsonMsgConvertor.UniversalArray(Arrays.asList("12", "13", "1.5", "2.5"));
    assertEquals(12, array.getInt(0));
    assertEquals(13L, array.getLong(1));
    assertEquals(1.5F, array.getFloat(2));
    assertEquals(2.5D, array.getDouble(3));
  }

  @DisplayName("异常流分支")
  @Test
  void streamFailureBranches() throws Exception {
    AutoJsonMsgConvertor convertor = new AutoJsonMsgConvertor();
    InputStream broken = new InputStream() {
      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        throw new IOException("broken");
      }

      @Override
      public int read() throws IOException {
        throw new IOException("broken");
      }
    };
    assertThrows(RuntimeException.class, () -> convertor.toMapper(broken, StandardCharsets.UTF_8));
  }

  @DisplayName("convert 和 ParameterizedTypeImpl")
  @Test
  void convertAndParameterizedType() throws Exception {
    assertNull(AutoJsonMsgConvertor.convert(null, Map.class));
    AutoJsonMsgConvertor convertor = new AutoJsonMsgConvertor();
    assertNotNull(convertor.toBean((Type) Map.class, new ByteArrayInputStream("{\"a\":1}".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
    assertEquals(1, convertor.toList(Map.class, new ByteArrayInputStream("[{\"a\":1}]".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8).size());
    assertThrows(RuntimeException.class, () -> convertor.toMapper(new ByteArrayInputStream(new byte[]{1, 2}), StandardCharsets.UTF_8));
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("a", 1);
    assertEquals(1, ((Map<?, ?>) AutoJsonMsgConvertor.convert(map, Map.class)).get("a"));

    Class<?> clazz = Class.forName("top.csaf.http.convert.AutoJsonMsgConvertor$ParameterizedTypeImpl");
    Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Type[].class);
    constructor.setAccessible(true);
    Type type = (Type) constructor.newInstance(List.class, new Type[]{String.class});
    assertArrayEquals(new Type[]{String.class}, ((java.lang.reflect.ParameterizedType) type).getActualTypeArguments());
    assertEquals(List.class, ((java.lang.reflect.ParameterizedType) type).getRawType());
    assertNull(((java.lang.reflect.ParameterizedType) type).getOwnerType());
  }

  @DisplayName("当前 JSON 提供者")
  @Test
  void currentProvider() throws Exception {
    Field provider = AutoJsonMsgConvertor.class.getDeclaredField("PROVIDER");
    provider.setAccessible(true);
    assertNotNull(provider.get(null));
  }

  @DisplayName("隔离类加载器覆盖无提供者")
  @Test
  void noneProviderByClassLoader() throws Exception {
    URLClassLoader classLoader = providerClassLoader();
    Class<?> convertorClass = Class.forName("top.csaf.http.convert.AutoJsonMsgConvertor", true, classLoader);
    Field provider = convertorClass.getDeclaredField("PROVIDER");
    provider.setAccessible(true);
    assertEquals("NONE", String.valueOf(provider.get(null)));
    Object convertor = convertorClass.getConstructor().newInstance();
    Method serialize = convertorClass.getMethod("serialize", Object.class, boolean.class);
    assertThrows(InvocationTargetException.class, () -> serialize.invoke(convertor, Collections.singletonMap("a", 1), false));
    Method toBean = convertorClass.getMethod("toBean", Type.class, String.class);
    assertThrows(InvocationTargetException.class, () -> toBean.invoke(convertor, Map.class, "{}"));
  }

  @DisplayName("隔离类加载器覆盖 JSON 提供者")
  @Test
  void jsonProvidersByClassLoader() throws Exception {
    assertProvider("JACKSON", providerClassLoader(new HashSet<>(Arrays.asList(
      "com.alibaba.fastjson2.JSON",
      "com.alibaba.fastjson.JSON")),
      com.fasterxml.jackson.databind.ObjectMapper.class,
      com.fasterxml.jackson.core.JsonFactory.class,
      com.fasterxml.jackson.annotation.JsonAutoDetect.class), true);
    assertProvider("GSON", providerClassLoader(new HashSet<>(Arrays.asList(
        "com.alibaba.fastjson2.JSON",
        "com.alibaba.fastjson.JSON",
        "com.fasterxml.jackson.databind.ObjectMapper")),
      com.google.gson.Gson.class), true);
  }

  @DisplayName("转换失败分支")
  @Test
  void convertFailureBranch() throws Exception {
    Field provider = AutoJsonMsgConvertor.class.getDeclaredField("PROVIDER");
    provider.setAccessible(true);
    Object original = provider.get(null);
    Object none = Enum.valueOf((Class<Enum>) provider.getType(), "NONE");
    sun.misc.Unsafe unsafe = unsafe();
    Object base = unsafe.staticFieldBase(provider);
    long offset = unsafe.staticFieldOffset(provider);
    unsafe.putObject(base, offset, none);
    try {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("a", 1);
      RuntimeException exception = assertThrows(RuntimeException.class,
        () -> AutoJsonMsgConvertor.convert(map, Map.class));
      assertEquals("Convert failed", exception.getMessage());
    } finally {
      unsafe.putObject(base, offset, original);
    }
  }

  @DisplayName("Fastjson v1 成功分支")
  @Test
  void fastjsonV1SuccessBranches() throws Exception {
    Field provider = AutoJsonMsgConvertor.class.getDeclaredField("PROVIDER");
    provider.setAccessible(true);
    Object original = provider.get(null);
    Object fastjsonV1 = Enum.valueOf((Class<Enum>) provider.getType(), "FASTJSON_V1");
    sun.misc.Unsafe unsafe = unsafe();
    Object base = unsafe.staticFieldBase(provider);
    long offset = unsafe.staticFieldOffset(provider);
    unsafe.putObject(base, offset, fastjsonV1);
    try {
      AutoJsonMsgConvertor convertor = new AutoJsonMsgConvertor();
      assertTrue(convertor.serialize(Collections.singletonMap("a", 1), true).contains("a"));
      assertTrue(convertor.serialize(Collections.singletonMap("a", 1), false).contains("a"));
      Map<?, ?> bean = convertor.toBean(Map.class, "{\"a\":1}");
      assertEquals(1, ((Number) bean.get("a")).intValue());
    } finally {
      unsafe.putObject(base, offset, original);
    }
  }

  @DisplayName("Fastjson v1 pretty 降级分支")
  @Test
  void fastjsonV1PrettyFallbackByClassLoader() throws Exception {
    java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("fastjson-stub");
    java.nio.file.Path source = tempDir.resolve("com/alibaba/fastjson/JSON.java");
    java.nio.file.Files.createDirectories(source.getParent());
    java.nio.file.Files.write(source, Arrays.asList(
      "package com.alibaba.fastjson;",
      "public class JSON {",
      "  public static String toJSONString(Object object) { return String.valueOf(object); }",
      "}"), StandardCharsets.UTF_8);
    javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler);
    assertEquals(0, compiler.run(null, null, null, source.toString()));

    URLClassLoader classLoader = providerClassLoader(new HashSet<>(Collections.singletonList("com.alibaba.fastjson2.JSON")),
      Collections.singletonList(tempDir.toUri().toURL()));
    Class<?> convertorClass = Class.forName("top.csaf.http.convert.AutoJsonMsgConvertor", true, classLoader);
    Field provider = convertorClass.getDeclaredField("PROVIDER");
    provider.setAccessible(true);
    assertEquals("FASTJSON_V1", String.valueOf(provider.get(null)));
    Object convertor = convertorClass.getConstructor().newInstance();
    Method serialize = convertorClass.getMethod("serialize", Object.class, boolean.class);
    assertTrue(((String) serialize.invoke(convertor, Collections.singletonMap("a", 1), true)).contains("a=1"));
  }

  private static sun.misc.Unsafe unsafe() throws Exception {
    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return (sun.misc.Unsafe) field.get(null);
  }

  private static void assertProvider(String expected, URLClassLoader classLoader, boolean pretty) throws Exception {
    Class<?> convertorClass = Class.forName("top.csaf.http.convert.AutoJsonMsgConvertor", true, classLoader);
    Field provider = convertorClass.getDeclaredField("PROVIDER");
    provider.setAccessible(true);
    assertEquals(expected, String.valueOf(provider.get(null)));
    Object convertor = convertorClass.getConstructor().newInstance();
    Method serialize = convertorClass.getMethod("serialize", Object.class, boolean.class);
    if ("FASTJSON_V1".equals(expected)) {
      assertThrows(InvocationTargetException.class,
        () -> serialize.invoke(convertor, Collections.singletonMap("a", 1), false));
      return;
    }
    assertTrue(((String) serialize.invoke(convertor, Collections.singletonMap("a", 1), false)).contains("a"));
    if (pretty) {
      assertTrue(((String) serialize.invoke(convertor, Collections.singletonMap("a", 1), true)).contains("a"));
    }
    Method toBean = convertorClass.getMethod("toBean", Type.class, String.class);
    Object bean = toBean.invoke(convertor, Map.class, "{\"a\":1}");
    assertEquals(1, ((Number) ((Map<?, ?>) bean).get("a")).intValue());
  }

  private static URLClassLoader providerClassLoader(Class<?>... includedClasses) throws Exception {
    return providerClassLoader(Collections.emptySet(), includedClasses);
  }

  private static URLClassLoader providerClassLoader(Set<String> hiddenClasses, Class<?>... includedClasses) throws Exception {
    return providerClassLoader(hiddenClasses, Collections.emptyList(), includedClasses);
  }

  private static URLClassLoader providerClassLoader(Set<String> hiddenClasses, URL... additionalUrls) throws Exception {
    return providerClassLoader(hiddenClasses, Arrays.asList(additionalUrls));
  }

  private static URLClassLoader providerClassLoader(Set<String> hiddenClasses, List<URL> additionalUrls, Class<?>... includedClasses) throws Exception {
    List<URL> urls = new ArrayList<>();
    urls.add(AutoJsonMsgConvertor.class.getProtectionDomain().getCodeSource().getLocation());
    urls.add(cn.zhxu.okhttps.MsgConvertor.class.getProtectionDomain().getCodeSource().getLocation());
    urls.add(cn.zhxu.data.Mapper.class.getProtectionDomain().getCodeSource().getLocation());
    urls.add(org.slf4j.LoggerFactory.class.getProtectionDomain().getCodeSource().getLocation());
    urls.addAll(additionalUrls);
    for (Class<?> includedClass : includedClasses) {
      urls.add(includedClass.getProtectionDomain().getCodeSource().getLocation());
    }
    return new URLClassLoader(urls.toArray(new URL[0]), null) {
      @Override
      public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (hiddenClasses.contains(name)) {
          throw new ClassNotFoundException(name);
        }
        return super.loadClass(name, resolve);
      }
    };
  }
}
